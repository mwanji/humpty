package co.mewf.humpty.spi.caches;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.spi.resolvers.AssetFile;

public class WebjarsPipelineCache implements PipelineCache {
  
  private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
  private List<String> devWebjars = emptyList();
  
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
    boolean doNotCache = devWebjars.stream().filter(devWebjar -> asset.getPath().startsWith(devWebjar)).findAny().isPresent();
    
    if (doNotCache) {
      return;
    }
    
    cache.put(asset.getPath(), asset.getContents());
  }
  
  @Inject
  public void configure(Configuration.Mode mode, Configuration.Options options) {
    if (mode == Configuration.Mode.DEVELOPMENT) {
      this.devWebjars = options.get("devWebJars", emptyList()).stream().map(s -> WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/" + s).collect(toList());
    }
  }
}
