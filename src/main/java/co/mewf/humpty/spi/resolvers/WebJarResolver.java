package co.mewf.humpty.spi.resolvers;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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
 */
public class WebJarResolver implements Resolver {
  private final WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator();
  private Path rootDir;
  
  @Override
  public String getName() {
    return "webjars";
  }
  
  @Inject
  public void configure(Configuration.Options options) {
    this.rootDir = Paths.get(options.get("rootDir", "src/main/resources"));
  }

  @Override
  public boolean accepts(String uri) {
    return !uri.contains(":") && !uri.startsWith("/");
  }

  @Override
  public List<AssetFile> resolve(String uri, Context context) {
    try {
      WildcardHelper helper = new WildcardHelper(uri);

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (!helper.hasWildcard()) {
        String fullPath = webJarAssetLocator.getFullPath(uri);
        Path assetPath = rootDir.resolve(fullPath);
        
        String contents;
        if (assetPath.toFile().exists()) {
          contents = new String(Files.readAllBytes(rootDir.resolve(fullPath)));
        } else {
          contents = IOUtils.toString(new InputStreamReader(classLoader.getResourceAsStream(fullPath)));
        }
        
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
