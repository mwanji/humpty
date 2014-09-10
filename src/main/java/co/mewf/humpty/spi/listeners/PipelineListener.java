package co.mewf.humpty.spi.listeners;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.spi.PipelineElement;

public interface PipelineListener extends PipelineElement {

  void onBundleProcessed(String bundle, String name);
  void onAssetProcessed(String asset, String name, String assetPath, Bundle bundle);
}
