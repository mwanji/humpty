package co.mewf.humpty.config;

import java.util.Collections;
import java.util.List;

public class Bundle {

  private String name;
  private List<String> assets;

  public Bundle() {}

  public Bundle(String name, List<String> assets) {
    this.name = name;
    this.assets = assets;
  }

  public boolean accepts(String uri) {
    return name.equals(uri) || assets.contains(uri);
  }

  public List<String> getBundleFor(String uri) {
    if (name.equals(uri)) {
      return assets;
    }

    return Collections.singletonList(uri);
  }
}
