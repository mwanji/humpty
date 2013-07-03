package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PreProcessorContext extends Context {

  private final String assetUrl;

  public String getAssetUrl() {
    return assetUrl;
  }

  PreProcessorContext(String assetUrl, Mode mode, String bundleName, HttpServletRequest request, HttpServletResponse response) {
    super(mode, bundleName, request, response);
    this.assetUrl = assetUrl;
  }
}
