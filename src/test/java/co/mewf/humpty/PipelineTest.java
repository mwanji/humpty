package co.mewf.humpty;

import static java.util.Arrays.asList;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.caches.FileLocator;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.resolvers.AssetFile;
import co.mewf.humpty.resolvers.Resolver;

public class PipelineTest {
  private final WebJarAssetLocator locator = new WebJarAssetLocator();
  private final FileLocator fileLocator = new FileLocator() {
    @Override
    public File locate(String path) {
      return new File(path);
    }
  };

  @Test
  public void should_process_bundle() throws IOException {
    Pipeline testPipeline = new HumptyBootstrap.Builder().build(new TestProcessor(), fileLocator).createPipeline();
    Reader reader = testPipeline.process("singleAsset.js");
    String result = IOUtils.toString(reader);

    assertTrue(result.startsWith("Preprocessed!Compiled!"));
    assertTrue(result.endsWith("Postprocessed!"));
  }

  @Test
  public void should_compile_bundle() throws IOException {
    Reader result = new HumptyBootstrap.Builder().build(new CoffeeScriptCompilingProcessor(), fileLocator).createPipeline().process("compilableAsset.js");

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js")));
    assertEquals(expected, resultString);
  }

  @Test
  public void should_concatenate_bundle_with_multiple_assets() throws IOException {
    Reader result = new HumptyBootstrap.Builder().build(fileLocator).createPipeline().process("multipleAssets.js");

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js"))) + IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("web_server.js")));

    assertEquals(expected, resultString);
  }

  @Test
  public void should_pass_configuration_options_via_java() throws IOException {
    TestConfigurable testConfigurable = new TestConfigurable();
    Pipeline configurablePipeline = new HumptyBootstrap.Builder().build(new Configuration(asList(new Bundle("singleAsset.js", asList("blocks.js"))), testConfigurable), testConfigurable, fileLocator).createPipeline();

    String actual = IOUtils.toString(configurablePipeline.process("singleAsset.js"));

    assertEquals("passed!passed!\npassed!", actual);
  }

  @Test
  public void should_pass_configuration_options_via_json() throws IOException {
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-no-alias.json").build(new TestConfigurable(), fileLocator);
    Pipeline configurablePipeline = bootstrap.createPipeline();

    String actual = IOUtils.toString(configurablePipeline.process("singleAsset.js"));

    assertEquals("configured from JSON!configured from JSON!\nconfigured from JSON!", actual);
  }

  @Test
  public void should_pass_aliased_configuration_via_json() throws IOException {
    Pipeline aliasedPipeline = new HumptyBootstrap.Builder().build(new TestConfigurable(), fileLocator).createPipeline();

    String actual = IOUtils.toString(aliasedPipeline.process("singleAsset.js"));

    assertEquals("aliased from JSON!aliased from JSON!\naliased from JSON!", actual);
  }

  @Test
  public void should_only_pass_configuration_for_current_processor() {
    TestConfigurable resource = new TestConfigurable();
    new HumptyBootstrap.Builder().humptyFile("/humpty-multiple-configs.json").build(resource, new EchoProcessor(), fileLocator).createPipeline();

    assertEquals(resource.options.get("message"), "correct");
    assertNull(resource.options.get("echoMessage"));
  }

  @Test
  public void should_take_extension_from_bundle_when_not_specified_by_asset() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.json").build(new EchoProcessor(), fileLocator).createPipeline();

    String output = IOUtils.toString(pipeline.process("no_extension.js"));

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_with_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.json").build(new EchoProcessor(), fileLocator).createPipeline();

    String output = IOUtils.toString(pipeline.process("folder_and_extension.js"));

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_without_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.json").build(new EchoProcessor(), fileLocator).createPipeline();

    String output = IOUtils.toString(pipeline.process("folder_without_extension.coffee"));

    assertThat("Did not include blocks.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.coffee")))));
    assertThat("Did not include web_server.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.coffee")))));
  }

  @Test
  public void should_cache_results() {
    CountingProcessor countingProcessor = new CountingProcessor();
    Pipeline pipeline = new HumptyBootstrap.Builder().build(new Configuration(asList(new Bundle("bundle.js", asList("blocks.js")))), fileLocator, countingProcessor).createPipeline();

    pipeline.process("bundle.js");
    pipeline.process("bundle.js");

    assertEquals("Asset processor was called more than once", 1, countingProcessor.getAssetCount());
    assertEquals("Bundle processor was called more than once", 1, countingProcessor.getBundleCount());
  }

  @Test
  public void should_handle_timestamped_bundle_name() {
    CountingProcessor countingProcessor = new CountingProcessor();
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-production.json").build(fileLocator, countingProcessor);
    Pipeline pipeline = bootstrap.createPipeline();

    String assetName = "singleAsset-humpty" + new Date().getTime() + ".js";
    pipeline.process(assetName);
    pipeline.process(assetName);

    assertEquals("Asset processor was called more than once", 1, countingProcessor.getAssetCount());
    assertEquals("Bundle processor was called more than once", 1, countingProcessor.getBundleCount());
  }

  @Test
  public void should_reprocess_when_timestamp_changes() {
    CountingProcessor countingProcessor = new CountingProcessor();
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-production.json").build(fileLocator, countingProcessor);
    Pipeline pipeline = bootstrap.createPipeline();

    pipeline.process("singleAsset-humpty" + (new Date().getTime() - 1000) + ".js");
    pipeline.process("singleAsset-humpty" + new Date().getTime() + ".js");

    assertEquals("Asset processor should have been called twice", 2, countingProcessor.getAssetCount());
    assertEquals("Bundle processor should have been called twice", 2, countingProcessor.getBundleCount());
  }

  @Test
  public void should_invalidate_cache_when_file_modified() throws Exception {
    final File parent = new File("src/test/resources");
    FileLocator fileLocator = new FileLocator() {
      @Override
      public File locate(String path) {
        return new File(parent.getAbsolutePath() + path);
      }
    };
    Resolver resolver = new Resolver() {
      @Override
      public boolean accepts(String uri) {
        return true;
      }

      @Override
      public List<AssetFile> resolve(String uri, Context context) {
        String path = parent.getAbsolutePath() + uri;
        return Collections.singletonList(new AssetFile(context.getBundle(), path , new File(path)));
      }
    };
    CountingProcessor countingProcessor = new CountingProcessor();
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-watch.json").build(fileLocator, resolver, countingProcessor);
    Pipeline pipeline = bootstrap.createPipeline();

    pipeline.process("bundle.js");

    File file = new File(parent, "asset1.js");
    file.setLastModified(System.currentTimeMillis());

    Thread.sleep(6000);

    pipeline.process("bundle.js");

    assertEquals("Asset processor should have been called three times", 3, countingProcessor.getAssetCount());
    assertEquals("Bundle processor should have been called twice", 2, countingProcessor.getBundleCount());
  }
}
