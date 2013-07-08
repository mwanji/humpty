package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;

public interface AssetProcessor extends Processor {
  boolean accepts(String assetName);
  Reader processAsset(String assetName, Reader asset, PreProcessorContext context);
}
