package co.mewf.humpty.spi.resolvers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;

public class FileResolver implements Resolver {
  
  private Path assetsDir;

  @Override
  public String getName() {
    return "files";
  }

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("/");
  }

  @Override
  public List<AssetFile> resolve(String uri, Context context) {
    Path uriPath = Paths.get(uri.substring(1));
    Path assetPath = assetsDir.resolve(uriPath);
    String fullPath = assetPath.toString();

    try {
      String contents = new String(Files.readAllBytes(assetPath));
      AssetFile assetFile = new AssetFile(context.getBundle(), fullPath, contents);
      
      return Collections.singletonList(assetFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Inject
  public void configure(Configuration.GlobalOptions globalOptions) {
    this.assetsDir = globalOptions.getAssetsDir();
  }

}
