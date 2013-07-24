package co.mewf.humpty.resolvers;

import co.mewf.humpty.config.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

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

  public Reader getReader() {
    try {
      return file != null ? new FileReader(file) : new StringReader(contents);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public Bundle getBundle() {
    return bundle;
  }
}
