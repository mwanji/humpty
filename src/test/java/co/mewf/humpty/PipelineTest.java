package co.mewf.humpty;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.spi.caches.WebjarsPipelineCache;
import co.mewf.humpty.spi.listeners.TracerPipelineListener;
import co.mewf.humpty.spi.resolvers.AssetFile;

public class PipelineTest {
  private final WebJarAssetLocator locator = new WebJarAssetLocator();

  @Test
  public void should_process_bundle() throws IOException {
    String result = new HumptyBootstrap("/should_process_bundle.toml").createPipeline().process("singleAsset.js");

    assertTrue(result.startsWith("Preprocessed!Compiled!"));
    assertTrue(result.endsWith("Postprocessed!"));
  }
  
  @Test
  public void should_default_to_humpty_toml() throws Exception {
    new HumptyBootstrap().createPipeline().process("asset.js");
  }

  @Test
  public void should_compile_bundle() throws IOException {
    String result = new HumptyBootstrap("/should_compile_bundle.toml").createPipeline().process("compilableAsset.js");

    String expected = read("blocks.js") + "\n";
    assertEquals(expected, result);
  }

  @Test
  public void should_concatenate_bundle_with_multiple_assets() throws IOException {
    String result = new HumptyBootstrap("/should_concatenate_bundle_with_multiple_assets.toml").createPipeline().process("multipleAssets.js");

    String expected = read("blocks.js") + "\n" + read("web_server.js") + "\n";

    assertEquals(expected, result);
  }

  @Test
  public void should_pass_configuration_options_via_toml() throws IOException {
    Pipeline pipeline = new HumptyBootstrap("/should_pass_configuration_options_via_toml.toml").createPipeline();

    String actual = pipeline.process("singleAsset.js");

    assertEquals("configured from TOML!configured from TOML!\nconfigured from TOML!", actual);
  }

  @Test
  public void should_take_extension_from_bundle_when_not_specified_by_asset() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap("/humpty-wildcard.toml").createPipeline();

    String output = pipeline.process("no_extension.js");

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_with_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap("/humpty-wildcard.toml").createPipeline();

    String output = pipeline.process("folder_and_extension.js");

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_without_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap("/humpty-wildcard.toml").createPipeline();

    String output = pipeline.process("folder_without_extension.coffee");

    assertThat("Did not include blocks.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.coffee")))));
    assertThat("Did not include web_server.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.coffee")))));
  }
  
  @Test
  public void should_sort_processors_in_configured_order() throws Exception {
    Pipeline pipeline = new HumptyBootstrap("/should_sort_processors_in_configured_order.toml").createPipeline();
    
    String result = pipeline.process("bundle.js");
    
    assertEquals(read("alert.js") + "appender2appender1appender2appender1\nappender2appender1", result);
  }
  
  @Test
  public void should_not_use_processors_that_do_not_accept_asset() {
    Pipeline pipeline = new HumptyBootstrap("/should_not_use_processors_that_do_not_accept_asset.toml").createPipeline();
    
    String result = pipeline.process("bundle.js");
    
    assertEquals(read("alert.js").trim(), result.trim());
  }
  
  @Test
  public void should_not_retrieve_listener_not_specified_in_explicit_configuration() {
    Pipeline pipeline = new HumptyBootstrap("/should_sort_processors_in_configured_order.toml").createPipeline();
    
    try {
      pipeline.getPipelineListener(TracerPipelineListener.class).ifPresent((p) -> Assert.fail());
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
  
  @Test
  public void should_not_inject_element_not_specified_in_explicit_configuration() throws Exception {
    try {
      new HumptyBootstrap("/should_not_inject_element_not_specified_in_explicit_configuration.toml").createPipeline();
    } catch (RuntimeException e) {
      throw (Exception) e.getCause().getCause();
    }
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void should_fail_when_unknown_bundle_requested() throws Exception {
    Pipeline pipeline = new HumptyBootstrap().createPipeline();
    
    pipeline.process("unknownBundle");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void should_fail_when_asset_in_bundle_cannot_be_resolved() throws Exception {
    Pipeline pipeline = new HumptyBootstrap().createPipeline();
    
    pipeline.process("unresolvable.css");
  }
  
  @Test
  public void bootstrap_can_override_mode() throws Exception {
    TracerPipelineListener tracerPipelineListener = new HumptyBootstrap("/humpty.toml", Configuration.Mode.EXTERNAL).createPipeline().getPipelineListener(TracerPipelineListener.class).get();
    
    assertEquals(Configuration.Mode.EXTERNAL, tracerPipelineListener.mode);
  }
  
  @Test
  public void should_not_cache_project_assets_in_development_mode() throws Exception {
    Configuration configuration = Configuration.load("/should_not_cache_project_assets.toml");
    Pipeline pipeline = new HumptyBootstrap(configuration, Configuration.Mode.DEVELOPMENT).createPipeline();
    WebjarsPipelineCache cache = pipeline.getPipelineElement(WebjarsPipelineCache.class).get();
    
    pipeline.process("caching.js");
    
    Bundle bundle = configuration.getBundles().get(0);
    assertTrue(cache.get(new AssetFile(bundle, locator.getFullPath("jquery.js"), "")).isPresent());
    assertFalse(cache.get(new AssetFile(bundle, locator.getFullPath("blocks.js"), "")).isPresent());
  }
  
  @Test
  public void should_cache_project_assets_in_production_mode() throws Exception {
    Configuration configuration = Configuration.load("/should_not_cache_project_assets.toml");
    Pipeline pipeline = new HumptyBootstrap(configuration).createPipeline();
    WebjarsPipelineCache cache = pipeline.getPipelineElement(WebjarsPipelineCache.class).get();
    
    pipeline.process("caching.js");
    
    Bundle bundle = configuration.getBundles().get(0);
    assertTrue(cache.get(new AssetFile(bundle, locator.getFullPath("jquery.min.js"), "")).isPresent());
    assertTrue(cache.get(new AssetFile(bundle, locator.getFullPath("blocks.js"), "")).isPresent());
  }
  
  private String read(String filename) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(locator.getFullPath(filename))) {
      return IOUtils.toString(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
