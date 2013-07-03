package co.mewf.humpty.config;

import co.mewf.humpty.config.Configuration.Mode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Context {

  private final Configuration.Mode mode;
  private final String bundleName;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  public Context(Mode mode, String bundleName, HttpServletRequest request, HttpServletResponse response) {
    this.mode = mode;
    this.bundleName = bundleName;
    this.request = request;
    this.response = response;
  }

  public Configuration.Mode getMode() {
    return mode;
  }

  public String getBundleName() {
    return bundleName;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public PreProcessorContext getPreprocessorContext(String assetUrl) {
    return new PreProcessorContext(assetUrl, mode, bundleName, request, response);
  }
}
