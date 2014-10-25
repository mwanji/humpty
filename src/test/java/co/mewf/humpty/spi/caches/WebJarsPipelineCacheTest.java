package co.mewf.humpty.spi.caches;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.spi.listeners.TracerPipelineListener;

public class WebJarsPipelineCacheTest {

  @Test
  public void should_process_cached_asset_only_once() throws Exception {
    Pipeline pipeline = new HumptyBootstrap("/humpty.toml").createPipeline();
    
    TracerPipelineListener tracer = pipeline.getPipelineListener(TracerPipelineListener.class).get();
    
    pipeline.process("asset.js");
    pipeline.process("asset.js");
    
    assertEquals(1, tracer.onAssetProcessedCalledCount);
  }
}
