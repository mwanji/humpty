package co.mewf.humpty.spi.processors;

import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;

import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.processors.AssetProcessor;
import co.mewf.humpty.spi.processors.BundleProcessor;

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
  public Reader processBundle(String assetName, Reader asset, Context context) {
    bundleCounter.incrementAndGet();
    return asset;
  }

  @Override
  public Reader processAsset(String assetName, Reader asset, PreProcessorContext context) {
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
