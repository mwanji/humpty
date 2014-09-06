package co.mewf.humpty;

import static java.util.Arrays.asList;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.caches.FileLocator;
import co.mewf.humpty.caches.WatchingAssetCache;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.processors.AssetProcessor;
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
    Pipeline testPipeline = new HumptyBootstrap.Builder().humptyFile("/should_process_bundle.toml").build().createPipeline();
    Reader reader = testPipeline.process("singleAsset.js");
    String result = IOUtils.toString(reader);

    assertTrue(result.startsWith("Preprocessed!Compiled!"));
    assertTrue(result.endsWith("Postprocessed!"));
  }

  @Test
  public void should_compile_bundle() throws IOException {
    Reader result = new HumptyBootstrap.Builder().humptyFile("/should_compile_bundle.toml").build().createPipeline().process("compilableAsset.js");

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js")));
    assertEquals(expected, resultString);
  }

  @Test
  public void should_concatenate_bundle_with_multiple_assets() throws IOException {
    Reader result = new HumptyBootstrap.Builder().humptyFile("/should_concatenate_bundle_with_multiple_assets.toml").build().createPipeline().process("multipleAssets.js");

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js"))) + IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("web_server.js")));

    assertEquals(expected, resultString);
  }

  @Test
  public void should_pass_configuration_options_via_toml() throws IOException {
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/should_pass_configuration_options_via_toml.toml").build();
    Pipeline configurablePipeline = bootstrap.createPipeline();

    String actual = IOUtils.toString(configurablePipeline.process("singleAsset.js"));

    assertEquals("configured from JSON!configured from JSON!\nconfigured from JSON!", actual);
  }

  @Test
  public void should_take_extension_from_bundle_when_not_specified_by_asset() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.toml").build().createPipeline();

    String output = IOUtils.toString(pipeline.process("no_extension.js"));

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_with_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.toml").build().createPipeline();

    String output = IOUtils.toString(pipeline.process("folder_and_extension.js"));

    assertThat("Did not include blocks.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.js")))));
    assertThat("Did not include web_server.js", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.js")))));
  }

  @Test
  public void should_expand_wildcard_for_single_folder_without_extension() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-wildcard.toml").build().createPipeline();

    String output = IOUtils.toString(pipeline.process("folder_without_extension.coffee"));

    assertThat("Did not include blocks.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("blocks.coffee")))));
    assertThat("Did not include web_server.coffee", output, containsString(IOUtils.toString(classLoader.getResourceAsStream(locator.getFullPath("web_server.coffee")))));
  }

  @Test
  public void should_cache_results() {
    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-production.toml").build().createPipeline();

    pipeline.process("singleAsset.js");
    pipeline.process("singleAsset.js");
    
    CountingProcessor countingProcessor = getCountingProcessor(pipeline);

    assertEquals("Asset processor was called more than once", 1, countingProcessor.getAssetCount());
    assertEquals("Bundle processor was called more than once", 1, countingProcessor.getBundleCount());
  }

  @Test
  public void should_handle_timestamped_bundle_name() {
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-production.toml").build();
    Pipeline pipeline = bootstrap.createPipeline();

    String assetName = "singleAsset-humpty" + new Date().getTime() + ".js";
    pipeline.process(assetName);
    pipeline.process(assetName);
    
    CountingProcessor countingProcessor = getCountingProcessor(pipeline);
    
    assertEquals("Asset processor was not called exactly once", 1, countingProcessor.getAssetCount());
    assertEquals("Bundle processor was not called exactly once", 1, countingProcessor.getBundleCount());
  }

  @Test
  public void should_reprocess_when_timestamp_changes() {
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-production.toml").build();
    Pipeline pipeline = bootstrap.createPipeline();

    long now = new Date().getTime();
    pipeline.process("singleAsset-humpty" + (now - 1000) + ".js");
    pipeline.process("singleAsset-humpty" + now + ".js");

    CountingProcessor countingProcessor = getCountingProcessor(pipeline);
    assertEquals("Asset processor should have been called twice", 2, countingProcessor.getAssetCount());
    assertEquals("Bundle processor should have been called twice", 2, countingProcessor.getBundleCount());
  }

  @Test
  public void should_invalidate_cache_when_file_modified() throws Exception {
    final File parent = new File("src/test/resources");
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
    
    HumptyBootstrap bootstrap = new HumptyBootstrap.Builder().humptyFile("/humpty-watch.toml").build();
    Pipeline pipeline = new Pipeline(bootstrap.getConfiguration(), new WatchingAssetCache(), asList(resolver), Collections.emptyList(), asList(countingProcessor), asList(countingProcessor));

    pipeline.process("bundle.js");

    File file = new File(parent, "asset1.js");
    file.setLastModified(System.currentTimeMillis());

    Thread.sleep(6000);

    pipeline.process("bundle.js");

    assertEquals("Asset processor should have been called three times", 3, countingProcessor.getAssetCount());
    assertEquals("Bundle processor should have been called twice", 2, countingProcessor.getBundleCount());
  }

  private CountingProcessor getCountingProcessor(Pipeline pipeline) {
    try {
      Field field = Pipeline.class.getDeclaredField("assetProcessors");
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      CountingProcessor countingProcessor = (CountingProcessor) ((List<AssetProcessor>) field.get(pipeline)).get(0);
      return countingProcessor;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
