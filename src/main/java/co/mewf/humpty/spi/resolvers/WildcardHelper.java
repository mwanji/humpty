package co.mewf.humpty.spi.resolvers;

import org.apache.commons.io.FilenameUtils;

class WildcardHelper {

  private final String uri;
  private final String extension;

  WildcardHelper(String uri) {
    this.uri = uri;
    this.extension = FilenameUtils.getExtension(uri);
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
   * @return true if fileName is acceptable within the context of this uri
   */
  public boolean matches(String fileName) {
    return FilenameUtils.isExtension(fileName, extension);
  }
}
