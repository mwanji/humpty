package co.mewf.humpty.spi.listeners;

import javax.inject.Inject;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

public class TracerPipelineListener implements PipelineListener {

  public Boolean active;
  public String processedBundleName;
  public boolean onAssetProcessedCalled;

  @Override
  public String getName() {
    return "tracer";
  }
  
  @Override
  public void onAssetProcessed(String asset, String name, String assetPath, Bundle bundle) {
    this.onAssetProcessedCalled = true;
  }
  
  @Override
  public void onBundleProcessed(String bundle, String name) {
    processedBundleName = name;
  }

  @Inject
  public void configure(Configuration.Options options, Pipeline pipeline) {
    this.active = options.get("active", Boolean.TRUE);
    
    if (options.get("fail", Boolean.FALSE)) {
      throw new UnsupportedOperationException("Should not have been injected!");
    }
  }
}
