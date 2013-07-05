package co.mewf.humpty.resolvers;

import co.mewf.humpty.config.Context;

import org.apache.commons.io.FilenameUtils;

public class WildcardHelper {

  private final String uri;
  private final Context context;

  public WildcardHelper(String uri, Context context) {
    this.uri = uri;
    this.context = context;
  }

  public boolean hasWildcard() {
    return uri.contains("*");
  }

  public String getFull() {
    return isExtensionMissing() ? uri + "." + FilenameUtils.getExtension(context.getBundleName()) : uri;
  }

  public String getRootDir() {
    String rootDir = uri.substring(0, uri.indexOf('*'));
    if (!rootDir.startsWith("/")) {
      rootDir = "/" + rootDir;
    }

    return rootDir;
  }

  public boolean matches(String fileName) {
    return FilenameUtils.getExtension(fileName).equals(getFullExtension());
  }

  private String getFullExtension() {
    return isExtensionMissing() ? FilenameUtils.getExtension(context.getBundleName()) : getExtension();
  }

  private boolean isExtensionMissing() {
    return getExtension().isEmpty();
  }

  private String getExtension() {
    return FilenameUtils.getExtension(uri);
  }
}
