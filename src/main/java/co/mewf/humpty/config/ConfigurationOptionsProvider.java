package co.mewf.humpty.config;

import java.util.Map;

/**
 * Should be implemented by processor configuration helper classes.
 */
public interface ConfigurationOptionsProvider {

  Map<Class<?>, Map<String, Object>> getOptions();
}
