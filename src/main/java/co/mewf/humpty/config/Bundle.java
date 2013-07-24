package co.mewf.humpty.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class Bundle {

  private String name;
  private List<String> assets;

  public Bundle(String name, List<String> assets) {
    this.name = name;
    this.assets = assets;
  }

  public Bundle() {}

  public boolean accepts(String uri) {
    return name.equals(uri) || assets.contains(uri);
  }

  /**
   * @return a list of asset names. Each asset name has an extension (taken from the bundle's name if none was provided), but wildcards are not expanded.
   */
  public List<String> getBundleFor(String uri) {
    ArrayList<String> normalisedAssets = new ArrayList<String>();
    if (name.equals(uri)) {
      for (String assetName : assets) {
        normalisedAssets.add(normaliseName(assetName));
      }
      return normalisedAssets;
    }

    return Collections.singletonList(uri);
  }

  public String getName() {
    return name;
  }

  private String normaliseName(String assetName) {
    if (!FilenameUtils.getExtension(assetName).isEmpty()) {
      return assetName;
    } else {
      return assetName + "." + FilenameUtils.getExtension(name);
    }
  }
}
