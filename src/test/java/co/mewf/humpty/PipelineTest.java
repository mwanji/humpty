package co.mewf.humpty;

import static java.util.Arrays.asList;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

public class PipelineTest {
  private final WebJarAssetLocator locator = new WebJarAssetLocator();

  @Test
  public void should_process_bundle() throws IOException {
    Pipeline testPipeline = new HumptyBootstrap(new TestProcessor()).createPipeline();
    Reader reader = testPipeline.process("singleAsset.js", null, null);
    String result = IOUtils.toString(reader);

    assertTrue(result.startsWith("Preprocessed!Compiled!"));
    assertTrue(result.endsWith("Postprocessed!"));
  }

  @Test
  public void should_compile_bundle() throws IOException {
    Reader result = new HumptyBootstrap(new CoffeeScriptCompilingProcessor()).createPipeline().process("compilableAsset.js", null, null);

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js")));
    assertEquals(expected, resultString);
  }

  @Test
  public void should_concatenate_bundle_with_multiple_assets() throws IOException {
    Reader result = new HumptyBootstrap().createPipeline().process("multipleAssets.js", null, null);

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js"))) + IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("web_server.js")));

    assertEquals(expected, resultString);
  }

  @Test
  public void should_pass_configuration_options_via_java() throws IOException {
    TestConfigurable testConfigurable = new TestConfigurable();
    Pipeline configurablePipeline = new HumptyBootstrap(new Configuration(asList(new Bundle("singleAsset.js", asList("blocks.js"))), testConfigurable), testConfigurable).createPipeline();

    String actual = IOUtils.toString(configurablePipeline.process("singleAsset.js", null, null));

    assertEquals("passed!passed!\npassed!", actual);
  }

  @Test
  public void should_pass_configuration_options_via_json() throws IOException {
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-no-alias.json").build(new TestConfigurable());
    Pipeline configurablePipeline = bootstrap.createPipeline();

    String actual = IOUtils.toString(configurablePipeline.process("singleAsset.js", null, null));

    assertEquals("configured from JSON!configured from JSON!\nconfigured from JSON!", actual);
  }

  @Test
  public void should_pass_aliased_configuration_via_json() throws IOException {
    Pipeline aliasedPipeline = new HumptyBootstrap(new TestConfigurable()).createPipeline();

    String actual = IOUtils.toString(aliasedPipeline.process("singleAsset.js", null, null));

    assertEquals("aliased from JSON!aliased from JSON!\naliased from JSON!", actual);
  }

  @Test
  public void should_take_extension_from_bundle_when_not_specified_by_asset() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.json").build(new EchoPreProcessor()).createPipeline();

    String output = IOUtils.toString(pipeline.process("no_extension.js", null, null));

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_with_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.json").build(new EchoPreProcessor()).createPipeline();

    String output = IOUtils.toString(pipeline.process("folder_and_extension.js", null, null));

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_without_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.json").build(new EchoPreProcessor()).createPipeline();

    String output = IOUtils.toString(pipeline.process("folder_without_extension.coffee", null, null));

    assertThat("Did not include blocks.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.coffee")))));
    assertThat("Did not include web_server.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.coffee")))));
  }
}
