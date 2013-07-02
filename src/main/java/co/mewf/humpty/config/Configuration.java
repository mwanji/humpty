package co.mewf.humpty.config;


import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Configuration {

  public static enum Mode {
    PRODUCTION, DEVELOPMENT;
  }

  private List<Bundle> bundles = Collections.emptyList();
  private Mode mode = Mode.PRODUCTION;
  private Map<Class<?>, Map<String, Object>> options = Collections.emptyMap();

  public Configuration() {}

  public Configuration(List<Bundle> bundles) {
    this.bundles = bundles;
  }

  public Configuration(List<Bundle> bundles, Map<Class<?>, Map<String, Object>> options) {
    this.bundles = bundles;
    this.options = options;
  }

  public Configuration(List<Bundle> bundles, Configuration.Mode mode) {
    this.bundles = bundles;
    this.mode = mode;
  }

  public Configuration(List<Bundle> bundles, Map<Class<?>, Map<String, Object>> options, Configuration.Mode mode) {
    this.bundles = bundles;
    this.mode = mode;
    this.options = options;
  }

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
