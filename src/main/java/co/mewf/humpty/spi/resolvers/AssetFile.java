package co.mewf.humpty.spi.resolvers;

import co.mewf.humpty.config.Bundle;

public class AssetFile {

  private final String path;
  private final String contents;
  private final Bundle bundle;

  public AssetFile(Bundle bundle, String path, String contents) {
    this.bundle = bundle;
    this.path = path;
    this.contents = contents;
  }

  public String getPath() {
    return path;
  }

  public String getContents() {
    return contents;
  }

  public Bundle getBundle() {
    return bundle;
  }
}
