package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;

public interface PostProcessor extends Processor {

  boolean accepts(String assetName);
  Reader postProcess(String assetName, Reader asset, Context context);
}
