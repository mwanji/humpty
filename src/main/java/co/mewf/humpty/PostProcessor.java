package co.mewf.humpty;

import co.mewf.humpty.config.Context;

import java.io.Reader;

public interface PostProcessor extends Processor {

  boolean accepts(String asset);
  Reader postProcess(String assetName, Reader asset, Context context);
}
