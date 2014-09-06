package co.mewf.humpty.caches;

import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import co.mewf.humpty.config.Bundle;

public class InMemoryAssetCache implements AssetCache {

  private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

  @Override
  public boolean contains(String assetName) {
    return cache.containsKey(assetName);
  }

  @Override
  public Reader get(String assetName) {
    return new StringReader(cache.get(assetName));
  }

  @Override
  public void put(Bundle bundle, String assetName, String asset) {
    cache.put(assetName, asset);
  }

  @Override
  public String getAlias() {
    return "memoryCache";
  };
}
