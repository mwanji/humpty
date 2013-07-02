package co.mewf.humpty;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import co.mewf.humpty.config.HumptyBootstrap;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class PipelineTest {
  private final HumptyBootstrap bootstrap = new HumptyBootstrap() {
    @Override
    protected List<? extends PreProcessor> getPreProcessors() {
      return asList(new CoffeeScriptPreProcessor());
    };
  };

  private final Pipeline pipeline = bootstrap.createPipeline();

  @Test
  public void should_pre_process_asset_in_bundle() throws IOException {
    Reader result = pipeline.process("singleAsset.js", null, null);

    String resultString = IOUtils.toString(result);

    assertEquals(IOUtils.toString(getClass().getResourceAsStream("/co/mewf/humpty/blocks.js")), resultString);
  }

  @Test
  public void should_concatenate_bundle() throws IOException {
    Reader result = pipeline.process("asset1.js", null, null);

    String resultString = IOUtils.toString(result);

    String expected = IOUtils.toString(getClass().getResourceAsStream("/co/mewf/humpty/blocks.js")) + IOUtils.toString(getClass().getResourceAsStream("/co/mewf/humpty/web_server.js"));

    assertEquals(expected, resultString);
  }

  @Test
  public void should_post_process() throws IOException {
    HumptyBootstrap appendingBootstrap = new HumptyBootstrap() {
      @Override
      protected List<? extends PreProcessor> getPreProcessors() {
        return asList(new CoffeeScriptPreProcessor());
      }

      @Override
      protected List<? extends PostProcessor> getPostProcessors() {
        return asList((new AppendingPostProcessor()));
      }
    };
    Pipeline appendingPipeline = appendingBootstrap.createPipeline();
    Reader reader = appendingPipeline.process("asset1.js", null, null);
    String result = IOUtils.toString(reader);

    String expected = IOUtils.toString(pipeline.process("asset1.js", null, null));

    assertEquals(expected + "Appended!", result);
  }
}
