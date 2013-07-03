package co.mewf.humpty.config;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

  public static enum Mode {
    PRODUCTION, DEVELOPMENT;
  }

  private List<Bundle> bundles = Collections.emptyList();
  private Mode mode = Mode.PRODUCTION;
  private final Map<Class<?>, Map<String, Object>> options = new HashMap<Class<?>, Map<String,Object>>();

  public Configuration(List<Bundle> bundles, ConfigurationOptionsProvider... optionProviders) {
    this(bundles, Configuration.Mode.PRODUCTION, optionProviders);
  }

  public Configuration(List<Bundle> bundles, Configuration.Mode mode, ConfigurationOptionsProvider... optionProviders) {
    this.bundles = bundles;
    this.mode = mode;
    if (optionProviders != null) {
      for (ConfigurationOptionsProvider optionProvider : optionProviders) {
        for (Map.Entry<Class<?>, Map<String, Object>> entry : optionProvider.getOptions().entrySet()) {
          this.options.put(entry.getKey(), entry.getValue());
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

  public Map<String, Object> getOptionsFor(Class<?> processorClass) {
    if (!options.containsKey(processorClass)) {
      return Collections.emptyMap();
    }

    return options.get(processorClass);
  }
}
