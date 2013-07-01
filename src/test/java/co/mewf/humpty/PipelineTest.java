package co.mewf.humpty;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.resolvers.WebJarResolver;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class PipelineTest {
  private Configuration configuration = new Gson().fromJson(new InputStreamReader(getClass().getResourceAsStream("/humpty.json")), Configuration.class);
  private Pipeline pipeline = new Pipeline(configuration, asList((Resolver) new WebJarResolver()), Arrays.asList((PreProcessor) new CoffeeScriptPreProcessor()), Collections.<PostProcessor>emptyList());

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
}
