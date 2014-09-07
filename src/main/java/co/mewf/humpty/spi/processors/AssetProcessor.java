package co.mewf.humpty.spi.processors;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;

public interface AssetProcessor extends Processor {
  Reader processAsset(String assetName, Reader asset, PreProcessorContext context);
}
