package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * Looks up assets using the {@link ServletContext}. It accepts assets starting with <code>/</code>, e.g. /scripts/app.js
 */
public class ServletContextPathResolver implements Resolver {

  private ServletContext servletContext;

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("/");
  }

  @Override
  public List<AssetFile> resolve(String uri, Context context) {
    final WildcardHelper helper = new WildcardHelper(uri, context);

    if (!helper.hasWildcard()) {
      String realPath = servletContext.getRealPath(uri);
      return Collections.singletonList(new AssetFile(context.getBundle(), realPath, new File(realPath)));
    }

    List<AssetFile> assetFiles = new ArrayList<AssetFile>();
    String rootDirPath = helper.getRootDir();
    File rootDir = new File(servletContext.getRealPath(rootDirPath));

    File[] files = rootDir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return helper.matches(name);
      }
    });

    for (File file : files) {
      assetFiles.add(new AssetFile(context.getBundle(), file.getPath(), file));
    }

    return assetFiles;
  }

  @Inject
  public void configure(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
