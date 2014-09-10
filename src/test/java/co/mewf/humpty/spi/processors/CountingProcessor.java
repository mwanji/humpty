package co.mewf.humpty.spi.processors;

import java.util.concurrent.atomic.AtomicInteger;

import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

public class CountingProcessor implements AssetProcessor, BundleProcessor {

  private final AtomicInteger bundleCounter = new AtomicInteger();
  private final AtomicInteger assetCounter = new AtomicInteger();

  @Override
  public String getName() {
    return "counting";
  }
  
  @Override
  public boolean accepts(String assetName) {
    return true;
  }

  @Override
  public String processBundle(String assetName, String asset, Context context) {
    bundleCounter.incrementAndGet();
    return asset;
  }

  @Override
  public String processAsset(String assetName, String asset, PreProcessorContext context) {
    assetCounter.incrementAndGet();
    return asset;
  }

  public int getBundleCount() {
    return bundleCounter.get();
  }

  public int getAssetCount() {
    return assetCounter.get();
  }
}
