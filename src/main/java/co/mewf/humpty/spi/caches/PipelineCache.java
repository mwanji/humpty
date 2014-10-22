package co.mewf.humpty.spi.caches;

import java.util.Optional;

import co.mewf.humpty.spi.PipelineElement;
import co.mewf.humpty.spi.resolvers.AssetFile;

public interface PipelineCache extends PipelineElement {

  Optional<String> get(AssetFile asset);
  void put(AssetFile asset);
}
