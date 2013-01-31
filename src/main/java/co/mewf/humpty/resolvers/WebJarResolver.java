package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.InputStreamReader;
import java.io.Reader;

import org.webjars.AssetLocator;

public class WebJarResolver implements Resolver {

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("webjar:");
  }

  @Override
  public Reader resolve(String uri, Context context) {
    return new InputStreamReader(getClass().getResourceAsStream(AssetLocator.getFullPath(stripPrefix(uri))));
  }

  @Override
  public String expand(String uri) {
    String webJarPath = AssetLocator.getWebJarPath(stripPrefix(uri));

    return "/" + webJarPath;
  }

  private String stripPrefix(String uri) {
    return uri.substring("webjar:".length());
  }
}
