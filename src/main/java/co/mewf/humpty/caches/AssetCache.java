package co.mewf.humpty.caches;

import java.io.Reader;

import co.mewf.humpty.config.Aliasable;
import co.mewf.humpty.config.Bundle;

public interface AssetCache extends Aliasable {

  boolean contains(String assetName);
  Reader get(String assetName);
  void put(Bundle bundle, String assetName, String asset);
}
