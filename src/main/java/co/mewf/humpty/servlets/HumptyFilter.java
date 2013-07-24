package co.mewf.humpty.servlets;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.HumptyBootstrap;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Builds a {@link Pipeline} configured via the default JSON file.
 *
 * For different behaviour, override {@link #createPipeline()}.
 *
 */
public class HumptyFilter implements Filter {

  private Pipeline pipeline;
  private String context;
  private Collection<String> urlPatternMappings;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    pipeline = createPipeline(filterConfig);
    context = filterConfig.getServletContext().getContextPath();
    urlPatternMappings = filterConfig.getServletContext().getFilterRegistration(filterConfig.getFilterName()).getUrlPatternMappings();
  }

  protected Pipeline createPipeline(FilterConfig filterConfig) {
    return new HumptyBootstrap.Builder().build(filterConfig.getServletContext()).createPipeline();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = ((HttpServletResponse) response);
    String assetUri = httpRequest.getServletPath();
    assetUri = assetUri.substring(assetUri.lastIndexOf('/') + 1);

    Reader processedAsset = pipeline.process(assetUri);

    httpResponse.setContentType(httpRequest.getRequestURI().endsWith(".js") ? "text/javascript" : "text/css");
    PrintWriter responseWriter = httpResponse.getWriter();
    IOUtils.copy(processedAsset, responseWriter);

    processedAsset.close();
  }

  @Override
  public void destroy() {}
}
