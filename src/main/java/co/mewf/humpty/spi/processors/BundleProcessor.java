package co.mewf.humpty.spi.processors;

import co.mewf.humpty.config.Context;

public interface BundleProcessor extends Processor {
  String processBundle(String assetName, String asset, Context context);
}
