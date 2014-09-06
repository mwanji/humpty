package co.mewf.humpty.caches;

import java.io.Reader;

import co.mewf.humpty.config.Bundle;

public class NoopAssetCache implements AssetCache {

  @Override
  public String getAlias() {
    return "noopCache";
  }

  @Override
  public boolean contains(String assetName) {
    return false;
  }

  @Override
  public Reader get(String assetName) {
    return null;
  }

  @Override
  public void put(Bundle bundle, String assetName, String asset) {}
}
