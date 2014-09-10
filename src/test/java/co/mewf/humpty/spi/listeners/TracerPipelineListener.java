package co.mewf.humpty.spi.listeners;

import javax.inject.Inject;

import co.mewf.humpty.config.Configuration;

public class TracerPipelineListener implements PipelineListener {

  public Boolean active;
  public boolean onBundleProcessedCalled;

  @Override
  public String getName() {
    return "tracer";
  }

  @Override
  public void onBundleProcessed(String bundle, String name) {
    onBundleProcessedCalled = true;
  }

  @Inject
  public void configure(Configuration.Options options) {
    this.active = options.get("active", Boolean.TRUE);
  }
}
