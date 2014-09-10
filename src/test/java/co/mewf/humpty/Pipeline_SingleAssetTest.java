package co.mewf.humpty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.spi.listeners.TracerPipelineListener;

import com.moandjiezana.toml.Toml;

public class Pipeline_SingleAssetTest {
  
  private final WebJarAssetLocator locator = new WebJarAssetLocator();
  private final Pipeline pipeline = new HumptyBootstrap(new Toml().parse(getClass().getResourceAsStream("should_process_asset_in_bundle.toml")).to(Configuration.class)).createPipeline();

  @Test
  public void should_process_asset_within_bundle() throws Exception {
    String result = pipeline.process("asset.js/blocks.coffee");
    
    assertEquals(read("blocks.js"), result);
  }
  
  @Test
  public void should_not_call_bundle_listeners() throws Exception {
    pipeline.process("asset.js/blocks.coffee");

    TracerPipelineListener tracer = pipeline.getPipelineListener(TracerPipelineListener.class);
    
    assertFalse("Called bundle listener", tracer.onBundleProcessedCalled);
  }
  
  @Test
  public void should_call_asset_listeners() throws Exception {
    pipeline.process("asset.js/blocks.coffee");

    TracerPipelineListener tracer = pipeline.getPipelineListener(TracerPipelineListener.class);
    
    assertTrue("Did not call asset listener", tracer.onAssetProcessedCalled);
  }
  
  private String read(String filename) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(locator.getFullPath(filename))) {
      return IOUtils.toString(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
