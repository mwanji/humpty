package co.mewf.humpty.config;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public class Bundle implements Iterable<String> {

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
  public Bundle getBundleFor(String uri) {
    return new Bundle(name + "/" + uri, Collections.singletonList(uri));
  }

  public String getName() {
    return name;
  }
  
  @Override
  public Iterator<String> iterator() {
    return stream().iterator();
  }
  
  public Stream<String> stream() {
    return assets.stream();
  }
  
  void normaliseAssets() {
    this.assets = assets.stream().map(this::normaliseName).collect(toList());
  }

  private String normaliseName(String assetName) {
    if (FilenameUtils.getExtension(assetName).isEmpty()) {
      return assetName + "." + FilenameUtils.getExtension(name);
    }
    
    return assetName;
  }
}
