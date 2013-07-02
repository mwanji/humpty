package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Context {

  private final Configuration.Mode mode;
  private final String asset;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  public Context(Mode mode, String asset, HttpServletRequest request, HttpServletResponse response) {
    this.mode = mode;
    this.asset = asset;
    this.request = request;
    this.response = response;
  }

  public Configuration.Mode getMode() {
    return mode;
  }

  public String getAsset() {
    return asset;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }
}
