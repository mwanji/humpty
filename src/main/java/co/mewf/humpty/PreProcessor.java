package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;

public interface PreProcessor extends Processor {
  boolean accepts(String assetName);
  Reader preProcess(String assetName, Reader asset, PreProcessorContext context);
}
