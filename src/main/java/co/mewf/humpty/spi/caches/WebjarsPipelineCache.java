package co.mewf.humpty.spi.caches;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import co.mewf.humpty.spi.resolvers.AssetFile;

public class WebjarsPipelineCache implements PipelineCache {
  
  private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
  
  @Override
  public String getName() {
    return "cache";
  }

  @Override
  public Optional<String> get(AssetFile asset) {
    return Optional.ofNullable(cache.get(asset.getPath()));
  }

  @Override
  public void put(AssetFile asset) {
    cache.put(asset.getPath(), asset.getContents());
  }

}
