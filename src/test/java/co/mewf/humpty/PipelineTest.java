package co.mewf.humpty;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.HumptyBootstrap;

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

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js")));
    assertEquals(expected, result);
  }

  @Test
  public void should_concatenate_bundle_with_multiple_assets() throws IOException {
    String result = new HumptyBootstrap("/should_concatenate_bundle_with_multiple_assets.toml").createPipeline().process("multipleAssets.js");

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js"))) + IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("web_server.js")));

    assertEquals(expected, result);
  }

  @Test
  public void should_pass_configuration_options_via_toml() throws IOException {
    Pipeline pipeline = new HumptyBootstrap("/should_pass_configuration_options_via_toml.toml").createPipeline();

    String actual = pipeline.process("singleAsset.js");

    assertEquals("configured from JSON!configured from JSON!\nconfigured from JSON!", actual);
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
  
  private String read(String filename) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(locator.getFullPath(filename))) {
      return IOUtils.toString(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
