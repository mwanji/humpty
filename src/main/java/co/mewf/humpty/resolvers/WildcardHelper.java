package co.mewf.humpty.resolvers;

import co.mewf.humpty.config.Context;

import org.apache.commons.io.FilenameUtils;

class WildcardHelper {

  private final String uri;
  private final Context context;

  WildcardHelper(String uri, Context context) {
    this.uri = uri;
    this.context = context;
  }

  public boolean hasWildcard() {
    return uri.contains("*");
  }

  /**
   * @return uri with an extension, taken from bundle name if necessary
   */
  public String getFull() {
    return isExtensionMissing() ? uri + "." + FilenameUtils.getExtension(context.getBundleName()) : uri;
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
    return FilenameUtils.getExtension(fileName).equals(getExtension());
  }

  private String getExtension() {
    return isExtensionMissing() ? FilenameUtils.getExtension(context.getBundleName()) : getUriExtension();
  }

  private boolean isExtensionMissing() {
    return getUriExtension().isEmpty();
  }

  private String getUriExtension() {
    return FilenameUtils.getExtension(uri);
  }
}
