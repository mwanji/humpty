package co.mewf.humpty.caches;

import co.mewf.humpty.config.Bundle;

import java.io.Reader;


public interface AssetCache {

  boolean contains(String assetName);
  Reader get(String assetName);
  void put(Bundle bundle, String assetName, String asset);
}
