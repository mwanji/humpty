package co.mewf.humpty.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a friendly name that can be used in the configuration file
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Alias {

  /**
   * The friendly name
   */
  String value();
}
