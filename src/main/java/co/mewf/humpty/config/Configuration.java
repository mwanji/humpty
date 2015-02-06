package co.mewf.humpty.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.mewf.humpty.spi.PipelineElement;

import com.moandjiezana.toml.Toml;

public class Configuration {

  public static enum Mode {
    PRODUCTION, DEVELOPMENT, EXTERNAL;
  }

  public static class Options {
    
    public static final Options EMPTY = new Options(Collections.emptyMap());

    private final Map<String, Object> options;

    public Options(Map<String, Object> options) {
      this.options = options;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
      return options.containsKey(key) ? (T) options.get(key) : defaultValue;
    }
    
    public <T> Optional<T> get(String key) {
      return Optional.ofNullable(get(key, null));
    }

    public boolean containsKey(String key) {
      return options.containsKey(key);
    }
    
    public Map<String, Object> toMap() {
      return new HashMap<>(options);
    }
  }
  
  public static class GlobalOptions {
    private String assetsDir;
    private String buildDir;
    private String digestFile;
    
    public Path getAssetsDir() {
      return Paths.get(assetsDir != null ? assetsDir : "src/main/resources/assets");
    }

    public Path getBuildDir() {
      return Paths.get(buildDir != null ? buildDir : "src/main/resources/META-INF/resources");
    }

    public Path getDigestFile() {
      return Paths.get(digestFile != null ? digestFile : "src/main/resources/humpty-digest.toml");
    }
  }

  private List<Bundle> bundle = new ArrayList<>();
  private Map<String, Object> options;
  private GlobalOptions globalOptions;
  
  public static Configuration load(String tomlPath) {
    if (tomlPath.startsWith("/")) {
      tomlPath = tomlPath.substring(1);
    }

    return load(new Toml().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(tomlPath)));
  }
  
  public static Configuration load(Path tomlPath) {
    return load(new Toml().parse(tomlPath.toFile()));
  }
  
  private static Configuration load(Toml toml) {
    Configuration configuration = toml.to(Configuration.class);
    configuration.globalOptions = toml.getTable("options").to(GlobalOptions.class);

    @SuppressWarnings("unchecked")
    Map<String, List<String>> map = toml.to(Map.class);
    map.entrySet().stream()
      .filter(e -> !e.getKey().toString().equals("options"))
      .filter(e -> !e.getKey().toString().equals("bundle"))
      .map(e -> new Bundle(e.getKey().toString(), (List<String>) e.getValue()))
      .forEach(configuration.bundle::add);
    
    return configuration;
  }

  public List<Bundle> getBundles() {
    return bundle;
  }
  
  @SuppressWarnings("unchecked")
  public Configuration.Options getOptionsFor(PipelineElement pipelineElement) {
    String name = pipelineElement.getName();
    
    if (options == null || !options.containsKey(name)) {
      return Options.EMPTY;
    }
    
    return new Options((Map<String, Object>) options.get(name));
  }

  public Configuration.GlobalOptions getGlobalOptions() {
    return globalOptions;
  }
}
