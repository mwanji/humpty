package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;


public class PreProcessorContext extends Context {

  private final String assetUrl;

  public String getAssetUrl() {
    return assetUrl;
  }

  PreProcessorContext(String assetUrl, Mode mode, String bundleName) {
    super(mode, bundleName);
    this.assetUrl = assetUrl;
  }
}
