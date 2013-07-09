package co.mewf.humpty.config;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

  public static class Options {

    private final Map<String, Object> options;
    private final Configuration.Mode mode;

    public Options(Map<String, Object> options, Configuration.Mode mode) {
      this.options = options;
      this.mode = mode;
    }

    public Object get(String key) {
      return options.get(key);
    }

    public boolean containsKey(String key) {
      return options.containsKey(key);
    }

    public Configuration.Mode getMode() {
      return mode;
    }

  }

  public static enum Mode {
    PRODUCTION, DEVELOPMENT;
  }

  private List<Bundle> bundles = Collections.emptyList();
  private Mode mode = Mode.PRODUCTION;
  private final Map<String, Map<String, Object>> options = new HashMap<String, Map<String,Object>>();

  public Configuration(List<Bundle> bundles, ConfigurationOptionsProvider... optionProviders) {
    this(bundles, Configuration.Mode.PRODUCTION, optionProviders);
  }

  public Configuration(List<Bundle> bundles, Configuration.Mode mode, ConfigurationOptionsProvider... optionProviders) {
    this.bundles = bundles;
    this.mode = mode;
    if (optionProviders != null) {
      for (ConfigurationOptionsProvider optionProvider : optionProviders) {
        for (Map.Entry<Class<?>, Map<String, Object>> entry : optionProvider.getOptions().entrySet()) {
          this.options.put(entry.getKey().getName(), entry.getValue());
        }
      }
    }
  }

  Configuration() {}

  public Mode getMode() {
    return mode;
  }

  public List<Bundle> getBundles() {
    return bundles;
  }

  public Configuration.Options getOptionsFor(Class<?> configurable) {
    if (configurable.isAnnotationPresent(Alias.class)) {
      String key = configurable.getAnnotation(Alias.class).value();

      if (options.containsKey(key)) {
        return new Options(options.get(key), mode);
      }
    }

    String key = configurable.getName();

    if (!options.containsKey(key)) {
      return new Options(Collections.<String, Object>emptyMap(), mode);
    }

    return new Options(options.get(key), mode);
  }
}
