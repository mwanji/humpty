package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;

public class Context {

  private final Configuration.Mode mode;
  private final Bundle bundle;

  public Context(Mode mode, Bundle bundle) {
    this.mode = mode;
    this.bundle = bundle;
  }

  public Configuration.Mode getMode() {
    return mode;
  }

  public Bundle getBundle() {
    return bundle;
  }

  public String getBundleName() {
    return bundle.getName();
  }

  public PreProcessorContext getPreprocessorContext(String assetUrl) {
    return new PreProcessorContext(assetUrl, mode, bundle);
  }
}
