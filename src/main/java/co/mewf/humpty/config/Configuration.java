package co.mewf.humpty.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import co.mewf.humpty.spi.PipelineElement;

import com.moandjiezana.toml.Toml;

public class Configuration {

  public static enum Mode {
    PRODUCTION, DEVELOPMENT;
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

    public boolean containsKey(String key) {
      return options.containsKey(key);
    }
  }

  private List<Bundle> bundles;
  private Map<String, Object> options;
  
  public static Configuration load(String tomlPath) {
    return new Toml().parse(Configuration.class.getResourceAsStream(tomlPath)).to(Configuration.class);
  }

  public List<Bundle> getBundles() {
    return bundles;
  }
  
  @SuppressWarnings("unchecked")
  public Configuration.Options getOptionsFor(PipelineElement pipelineElement) {
    String name = pipelineElement.getName();
    
    if (options == null || !options.containsKey(name)) {
      return Options.EMPTY;
    }
    
    return new Options((Map<String, Object>) options.get(name));
  }
}
