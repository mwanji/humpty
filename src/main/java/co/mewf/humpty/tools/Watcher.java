package co.mewf.humpty.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Configuration;

public class Watcher {

  private final WatchService watchService;
  private final Pipeline pipeline;
  private Configuration config;
  private final Path destination;
  private final Map<Path, Optional<Path>> assetToSource = new HashMap<>();
  private final Appendable out;
  private final BiConsumer<Path, String> afterProcessingHandler;
  
  public Watcher(Pipeline pipeline, Path assetsDir, Configuration config, Appendable out, BiConsumer<Path, String> afterProcessingHandler) {
    this.pipeline = pipeline;
    this.config = config;
    this.afterProcessingHandler = afterProcessingHandler;
    this.out = out;
    
    try {
      this.destination = Files.createTempDirectory(null);
      this.watchService = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    try (Stream<Path> paths = Files.walk(assetsDir)) {
      paths.map(path -> {
        if (path.toFile().isDirectory()) {
          try {
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
        
        return path;
      })
      .filter(path -> path.toFile().isFile())
      .forEach(path -> {
        assetToSource.put(path.toAbsolutePath(), Optional.ofNullable(assetsDir.relativize(path).getParent()));
      });
      
      out.append(assetToSource.keySet().stream()
        .map(assetsDir::relativize)
        .map(Path::toString)
        .sorted()
        .collect(Collectors.joining(", ", "Watching " + new File(".").getAbsoluteFile().getParentFile().toPath().relativize(assetsDir).normalize() + "\n\t[", "]\n")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          out.append("humpty-watch shutting down");
          out.append('\n');
          watchService.close();
        } catch (IOException e) {}
      }
    }));
  }
  
  public void start() {
    while (true) {
      WatchKey watchKey;
      try {
        watchKey = watchService.take();
      } catch (InterruptedException | ClosedWatchServiceException e) {
        return;
      }
      
      try {
        watchKey.pollEvents().stream()
          .filter(event -> event.kind() != StandardWatchEventKinds.OVERFLOW)
          .forEach(watchEvent -> {
            Path path = (Path) watchEvent.context();
            Path absolutePath = ((Path) watchKey.watchable()).resolve(path).toAbsolutePath();
            
            if (!assetToSource.containsKey(absolutePath)) {
              try {
                out.append("Unknown file: " + ((Path) watchKey.watchable()).resolve(path));
              } catch (Exception e) {}
              return;
            }
            
            Path relative = assetToSource.get(absolutePath).map(rel -> destination.resolve(rel)).orElse(destination);

            config.getBundles().stream()
              .filter(b -> b.accepts(path.getFileName().toString()))
              .findFirst()
              .map(b -> {
                try {
                  out.append("Process: " + b.getName() + "/" + path.getFileName().toString() + "\n");
                  
                  Pipeline.Output output = pipeline.process(b.getName() + "/" + path.getFileName().toString());
                  Path outputPath = relative.resolve(Paths.get(output.getFileName()).getFileName()).toAbsolutePath();
                  
                  if (absolutePath.equals(outputPath)) {
                    out.append("Skip: Source and destination files are the same\n");
                  } else {
                    afterProcessingHandler.accept(path, output.getAsset());
                  }
                  
                  return b;
                } catch (Exception e) {
                  e.printStackTrace();
                  return b;
                }
              })
              .orElseGet(() -> {
                try {
                  out.append("No bundle contains " + path.getFileName() + ". Check that it is defined in the humpty config file.");
                } catch (Exception e) {}
                return null;
              });
          });
      } finally {
        watchKey.reset();
      }
    }
  }
}
