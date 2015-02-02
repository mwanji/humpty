package co.mewf.humpty.spi.resolvers;

import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;

/**
 * Finds assets in WebJar format, either in a JAR or in the application's META-INF/resources.
 * 
 * Configuration name: webjar
 * Available options:
 * 
 * rootDir: The root of where to look for assets. Relative to project root. Defaults to src/main/resources.
 * preferMin: By default, in production mode uses an equivalent asset with .min in its extension if it exists and in development mode does nothing. Can be set to true
 * to always use the minified equivalent, or to false to never use it.
 */
public class WebJarResolver implements Resolver {
  private WebJarAssetLocator webJarAssetLocator;
  private Optional<Boolean> preferMin;
  
  @Override
  public String getName() {
    return "webjars";
  }
  
  @Inject
  public void configure(WebJarAssetLocator webJarAssetLocator, Configuration.Options options) {
    this.webJarAssetLocator = webJarAssetLocator;
    this.preferMin = options.get("preferMin");
  }

  @Override
  public boolean accepts(String uri) {
    return !uri.contains(":") && !uri.startsWith("/");
  }

  @Override
  public List<AssetFile> resolve(String uri, Context context) {
    boolean useMin = preferMin.orElse(context.getMode() == Configuration.Mode.PRODUCTION) && !uri.contains(".min.");
    try {
      WildcardHelper helper = new WildcardHelper(uri);

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      
      if (!helper.hasWildcard()) {
        String minifiedUri = FilenameUtils.getBaseName(uri) + ".min." + FilenameUtils.getExtension(uri);
        Path uriPath = Paths.get(uri);
        Path minUriPath = uriPath.getParent() != null ? uriPath.getParent().resolve(minifiedUri) : Paths.get(minifiedUri);
        
        String contents;
        String fullPath = webJarAssetLocator.getFullPath(uri);
        if (useMin) {
          try {
            fullPath = webJarAssetLocator.getFullPath(minUriPath.toString());
          } catch (IllegalArgumentException e) {
            // minified resource does not exist
          }
        }
        contents = IOUtils.toString(new InputStreamReader(classLoader.getResourceAsStream(fullPath)));
        
        AssetFile assetFile = new AssetFile(context.getBundle(), fullPath, contents);

        return Collections.singletonList(assetFile);
      }

      List<AssetFile> assetFiles = new ArrayList<AssetFile>();
      Set<String> assetPaths = webJarAssetLocator.listAssets(helper.getRootDir());
      for (String assetPath : assetPaths) {
        if (helper.matches(assetPath)) {
          assetFiles.add(new AssetFile(context.getBundle(), assetPath, IOUtils.toString(new InputStreamReader(classLoader.getResourceAsStream(assetPath)))));
        }
      }

      return assetFiles;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
