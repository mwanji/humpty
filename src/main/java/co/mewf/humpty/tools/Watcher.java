package co.mewf.humpty.tools;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.Pipeline.Output;
import co.mewf.humpty.config.Configuration;

public class Watcher {

  private final WatchService watchService;
  private final List<Path> sources;
  private final Pipeline pipeline;
  private Configuration config;
  private final Path destination;
  private final Map<Path, Optional<Path>> assetToSource = new HashMap<>();
  private final Appendable out;
  private final Path configPath;
  
  public Watcher(Pipeline pipeline, List<Path> sources, Path destination, Path configPath, Appendable out) {
    this.pipeline = pipeline;
    this.sources = sources;
    this.destination = destination;
    this.configPath = configPath;
    this.config = Configuration.load(configPath.toString());
    this.out = out;
    
    try {
      this.watchService = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    this.sources.stream()
      .map(source -> {
        try {
          out.append("Watching: " + source + "\n");
  
          Files.walk(source)
          .filter(path -> path.toFile().isDirectory())
          .forEach(path -> {
            try {
              path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
  
          return source;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .forEach(source -> {
        try {
          String watchedFilesMessage = Files.walk(source)
            .filter(path -> path.toFile().isFile())
            .map(path -> {
              assetToSource.put(path.toAbsolutePath(), Optional.ofNullable(source.relativize(path).getParent()));
              return source.relativize(path).toString();
            })
            .collect(Collectors.joining(", ", "Watching Files [", "]\n"));
  
          out.append(watchedFilesMessage);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    
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
      } catch (InterruptedException e) {
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
                  
                  Output output = pipeline.process(b.getName() + "/" + path.getFileName().toString());
                  Path outputPath = relative.resolve(Paths.get(output.getFileName()).getFileName()).toAbsolutePath();
                  
                  if (absolutePath.equals(outputPath)) {
                    out.append("Skip: Source and destination files are the same\n");
                  } else {
                    out.append("From: " + absolutePath + "\n");
                    out.append("To  : " + outputPath + "\n");
                    Files.write(outputPath, output.getAsset().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                  }
                  
                  return b;
                } catch (Exception e) {
                  e.printStackTrace();
                  return b;
                }
              })
              .orElseGet(() -> {
                try {
                  out.append("No bundle contains " + path + ". Check that it is defined in the humpty config file.");
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
