package co.mewf.humpty.processors;

import co.mewf.humpty.config.Context;

import java.io.Reader;

public interface BundleProcessor extends Processor {

  boolean accepts(String assetName);
  Reader processBundle(String assetName, Reader asset, Context context);
}
