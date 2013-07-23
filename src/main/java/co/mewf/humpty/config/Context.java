package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;

public class Context {

  private final Configuration.Mode mode;
  private final String bundleName;

  public Context(Mode mode, String bundleName) {
    this.mode = mode;
    this.bundleName = bundleName;
  }

  public Configuration.Mode getMode() {
    return mode;
  }

  public String getBundleName() {
    return bundleName;
  }

  public PreProcessorContext getPreprocessorContext(String assetUrl) {
    return new PreProcessorContext(assetUrl, mode, bundleName);
  }
}
