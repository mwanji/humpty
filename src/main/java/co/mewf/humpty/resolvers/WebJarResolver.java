package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.InputStreamReader;
import java.io.Reader;

import org.webjars.WebJarAssetLocator;

public class WebJarResolver implements Resolver {
  private final WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator();

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("webjar:") || (!uri.contains(":") && !uri.startsWith("/"));
  }

  @Override
  public Reader resolve(String uri, Context context) {
    return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(webJarAssetLocator.getFullPath(stripPrefix(uri))));
  }

  @Override
  public String expand(String uri) {
    return webJarAssetLocator.getFullPath(stripPrefix(uri)).substring("META-INF/resources".length());
  }

  private String stripPrefix(String uri) {
    return uri.substring("webjar:".length());
  }
}
