package co.mewf.humpty.spi.processors;

import co.mewf.humpty.config.Context;

import java.io.Reader;

public interface BundleProcessor extends Processor {

  Reader processBundle(String assetName, Reader asset, Context context);
}
