package co.mewf.humpty.servlets;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.HumptyBootstrap;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class HumptyFilter implements Filter {

  private Pipeline pipeline;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    HumptyBootstrap bootstrap = createBootstrap();
    pipeline = bootstrap.createPipeline();
  }

  protected HumptyBootstrap createBootstrap() {
    return new HumptyBootstrap();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String assetUri = httpRequest.getServletPath();
    HttpServletResponse httpResponse = ((HttpServletResponse) response);
    httpResponse.setContentType(httpRequest.getRequestURI().endsWith(".js") ? "text/javascript" : "text/css");
    assetUri = assetUri.substring(assetUri.lastIndexOf('/') + 1);
    Reader processedAsset = pipeline.process(assetUri, httpRequest, httpResponse);

    PrintWriter responseWriter = httpResponse.getWriter();
    IOUtils.copy(processedAsset, responseWriter);

    processedAsset.close();
  }

  @Override
  public void destroy() {}
}
