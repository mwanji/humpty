package co.mewf.humpty.resolvers;

import co.mewf.humpty.Resolver;
import co.mewf.humpty.config.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.Reader;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;

/**
 * Looks up assets using the {@link ServletContext}. It accepts assets starting with <code>/</code>, e.g. /scripts/app.js
 */
public class ServletContextPathResolver implements Resolver {

  @Override
  public boolean accepts(String uri) {
    return uri.startsWith("/");
  }

  @Override
  public LinkedHashMap<String, ? extends Reader> resolve(String uri, Context context) {
    try {
      LinkedHashMap<String, Reader> readers = new LinkedHashMap<String, Reader>();
      final WildcardHelper helper = new WildcardHelper(uri, context);

      if (!helper.hasWildcard()) {
        uri = helper.getFull();
        String expandedUri = expand(uri, context.getBundleName());
        readers.put(expandedUri, new FileReader(new File(context.getRequest().getServletContext().getRealPath(expandedUri))));

        return readers;
      }

      String rootDirPath = helper.getRootDir();
      File rootDir = new File(context.getRequest().getServletContext().getRealPath(rootDirPath));

      File[] fileNames = rootDir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return helper.matches(name);
        }
      });

      for (File file : fileNames) {
        readers.put(file.getPath(), new FileReader(file));
      }

      return readers;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String expand(String uri, String bundleName) {
    String extension = FilenameUtils.getExtension(uri);
    return !extension.isEmpty() ? uri : uri + "." + FilenameUtils.getExtension(bundleName);
  }
}
