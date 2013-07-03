package co.mewf.humpty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

public class PipelineTest {
  private final WebJarAssetLocator locator = new WebJarAssetLocator();
  private final Pipeline pipeline = new ConfigurableHumptyBootstrap(new CoffeeScriptCompilingProcessor()).createPipeline();

  @Test
  public void should_process_bundle() throws IOException {
    Pipeline testPipeline = new ConfigurableHumptyBootstrap(new TestProcessor()).createPipeline();
    Reader reader = testPipeline.process("singleAsset.js", null, null);
    String result = IOUtils.toString(reader);

    assertTrue(result.startsWith("Preprocessed!Compiled!"));
    assertTrue(result.endsWith("Postprocessed!"));
  }

  @Test
  public void should_compile_bundle() throws IOException {
    Reader result = pipeline.process("compilableAsset.js", null, null);

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js")));
    assertEquals(expected, resultString);
  }

  @Test
  public void should_concatenate_bundle_with_multiple_assets() throws IOException {
    Reader result = pipeline.process("multipleAssets.js", null, null);

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("blocks.js"))) + IOUtils.toString(getClass().getClassLoader().getResourceAsStream(locator.getFullPath("web_server.js")));

    assertEquals(expected, resultString);
  }
}
