package co.mewf.humpty.spi.listeners;

import javax.inject.Inject;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

public class TracerPipelineListener implements PipelineListener {

  public Boolean active;
  public String processedBundleName;
  public boolean onAssetProcessedCalled;
  public int onAssetProcessedCalledCount;
  public Pipeline pipeline;
  public Configuration.GlobalOptions globalOptions;

  @Override
  public String getName() {
    return "tracer";
  }
  
  @Override
  public void onAssetProcessed(String asset, String name, String assetPath, Bundle bundle) {
    this.onAssetProcessedCalled = true;
    this.onAssetProcessedCalledCount++;
  }
  
  @Override
  public void onBundleProcessed(String bundle, String name) {
    processedBundleName = name;
  }

  @Inject
  public void configure(Configuration.Options options, Pipeline pipeline, Configuration.GlobalOptions globalOptions) {
    this.pipeline = pipeline;
    this.globalOptions = globalOptions;
    this.active = options.get("active", Boolean.TRUE);
    
    if (options.get("fail", Boolean.FALSE)) {
      throw new UnsupportedOperationException("Should not have been injected!");
    }
  }
}
