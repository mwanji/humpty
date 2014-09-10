package co.mewf.humpty.spi.processors;

import co.mewf.humpty.config.PreProcessorContext;

public interface AssetProcessor extends Processor {
  String processAsset(String assetName, String asset, PreProcessorContext context);
}
