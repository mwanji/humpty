package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.webjars.WebJarAssetLocator;

public class WebJarResolver implements Resolver {
  private final WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator();

  @Override
  public boolean accepts(String uri) {
    return !uri.contains(":") && !uri.startsWith("/");
  }

  @Override
  public LinkedHashMap<String, ? extends Reader> resolve(String uri, Context context) {
    LinkedHashMap<String, Reader> readers = new LinkedHashMap<String, Reader>();
    WildcardHelper helper = new WildcardHelper(uri, context);

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (!helper.hasWildcard()) {
      String fullUri = helper.getFull();
      readers.put(expand(fullUri, context.getBundleName()), new InputStreamReader(classLoader.getResourceAsStream(webJarAssetLocator.getFullPath(fullUri))));

      return readers;
    }

    Set<String> assets = webJarAssetLocator.listAssets(helper.getRootDir());
    for (String asset : assets) {
      if (helper.matches(asset)) {
        readers.put(stripPrefix(asset), new InputStreamReader(classLoader.getResourceAsStream(asset)));
      }
    }

    return readers;
  }

  @Override
  public String expand(String uri, String bundleName) {
    if (FilenameUtils.getExtension(uri).isEmpty()) {
      uri += "." + FilenameUtils.getExtension(bundleName);
    }

    return stripPrefix(webJarAssetLocator.getFullPath(uri));
  }

  private String stripPrefix(String uri) {
    if (uri.startsWith("META-INF/resources")) {
      uri = uri.substring("META-INF/resources".length());
    }

    return uri;
  }
}
