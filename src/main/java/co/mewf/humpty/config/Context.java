package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;

public class Context {

  private final Configuration.Mode mode;
  private final Bundle bundle;
  private final String bundleName;

  public Context(Configuration.Mode mode, Bundle bundle) {
    this.mode = mode;
    this.bundle = bundle;
    this.bundleName = bundle.getName();
  }

  private Context(Mode mode, Bundle bundle, String childName) {
    this.mode = mode;
    this.bundle = bundle;
    this.bundleName = bundle.getName() + "/" + childName;
  }

  public Configuration.Mode getMode() {
    return mode;
  }

  public Bundle getBundle() {
    return bundle;
  }

  public String getBundleName() {
    return bundleName;
  }
  
  public Context getChild(String childName) {
    return new Context(mode, bundle, childName);
  }

  public PreProcessorContext getPreprocessorContext(String assetUrl) {
    return new PreProcessorContext(assetUrl, mode, bundle);
  }
}
