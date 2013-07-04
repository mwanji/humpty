package co.mewf.humpty.config;

import java.util.Map;

public interface Configurable {

  void configure(Map<String, Object> options);
}
