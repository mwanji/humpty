package co.mewf.humpty.config;

public class PreProcessorContext extends Context {

  private final String assetUrl;

  public String getAssetUrl() {
    return assetUrl;
  }

  PreProcessorContext(String assetUrl, Configuration.Mode mode, Bundle bundle) {
    super(mode, bundle);
    this.assetUrl = assetUrl;
  }
}
