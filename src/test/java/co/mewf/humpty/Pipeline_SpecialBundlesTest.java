package co.mewf.humpty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.spi.bundles.WebJarAssetBundleResolver;

/**
 * These tests request files that are not in the user's configuration, but may be requested from 3rd-party libraries.
 */
public class Pipeline_SpecialBundlesTest {
  private final Pipeline pipeline = new HumptyBootstrap("/humpty-special-bundles.toml").createPipeline();

  @Test
  public void should_find_source_maps() throws Exception {
    String sourceMap = pipeline.process("jquery.min.map").getAsset();
    
    assertEquals(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(new WebJarAssetLocator().getFullPath("jquery.min.map"))), sourceMap.trim());
  }
  
  @Test
  public void should_find_loose_webjar() throws Exception {
    String jquery = pipeline.process("jquery.min.js").getAsset();
    
    assertEquals(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(new WebJarAssetLocator().getFullPath("jquery.min.js"))).trim(), jquery.trim());
  }
  
  public void should_reject_existing_bundle() {
    WebJarAssetBundleResolver resolver = new WebJarAssetBundleResolver();
    resolver.configure(Configuration.load("/humpty-special-bundles.toml"), new WebJarAssetLocator());
    
    assertFalse(resolver.accepts("asset.js"));
  }
}
