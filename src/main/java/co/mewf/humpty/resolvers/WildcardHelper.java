package co.mewf.humpty.resolvers;

import co.mewf.humpty.config.Context;

import org.apache.commons.io.FilenameUtils;

class WildcardHelper {

  private final String uri;
  private final Context context;
  private final String extension;

  WildcardHelper(String uri, Context context) {
    this.uri = uri;
    this.context = context;
    this.extension = FilenameUtils.getExtension(uri).isEmpty() ? FilenameUtils.getExtension(uri) : FilenameUtils.getExtension(context.getBundleName());
  }

  public boolean hasWildcard() {
    return uri.contains("*");
  }

  /**
   * @return path of the directory before the first wildcard
   */
  public String getRootDir() {
    String rootDir = uri.substring(0, uri.indexOf('*'));
    if (!rootDir.startsWith("/")) {
      rootDir = "/" + rootDir;
    }

    return rootDir;
  }

  /**
   * @return true if fileName is acceptable within the context of this uri and bundle
   */
  public boolean matches(String fileName) {
    return FilenameUtils.isExtension(fileName, extension);
  }
}
