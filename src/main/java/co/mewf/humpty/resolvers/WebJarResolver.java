package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.webjars.WebJarAssetLocator;

public class WebJarResolver implements Resolver {
  private final WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator();

  @Override
  public boolean accepts(String uri) {
    return !uri.contains(":") && !uri.startsWith("/");
  }

  @Override
  public List<AssetFile> resolve(String uri, Context context) {
    try {
      WildcardHelper helper = new WildcardHelper(uri, context);

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (!helper.hasWildcard()) {
        String fullPath = webJarAssetLocator.getFullPath(uri);
        AssetFile assetFile = new AssetFile(context.getBundle(), stripPrefix(fullPath), IOUtils.toString(new InputStreamReader(classLoader.getResourceAsStream(fullPath))));

        return Collections.singletonList(assetFile);
      }

      List<AssetFile> assetFiles = new ArrayList<AssetFile>();
      Set<String> assetPaths = webJarAssetLocator.listAssets(helper.getRootDir());
      for (String assetPath : assetPaths) {
        if (helper.matches(assetPath)) {
          assetFiles.add(new AssetFile(context.getBundle(), stripPrefix(assetPath), IOUtils.toString(new InputStreamReader(classLoader.getResourceAsStream(assetPath)))));
        }
      }

      return assetFiles;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String stripPrefix(String uri) {
    if (uri.startsWith("META-INF/resources")) {
      uri = uri.substring("META-INF/resources".length());
    }

    return uri;
  }
}
