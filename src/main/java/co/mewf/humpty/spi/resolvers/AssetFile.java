package co.mewf.humpty.spi.resolvers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import co.mewf.humpty.config.Bundle;

public class AssetFile {

  private final String path;
  private final File file;
  private final String contents;
  private final Bundle bundle;

  public AssetFile(Bundle bundle, String path, File file) {
    this.bundle = bundle;
    this.path = path;
    this.file = file;
    this.contents = null;
  }

  public AssetFile(Bundle bundle, String path, String contents) {
    this.bundle = bundle;
    this.path = path;
    this.contents = contents;
    this.file = null;
  }

  public String getPath() {
    return path;
  }

  public File getFile() {
    return file;
  }

  public String getContents() {
    try {
      return file != null ? new String(Files.readAllBytes(file.toPath())) : contents;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Bundle getBundle() {
    return bundle;
  }
}
