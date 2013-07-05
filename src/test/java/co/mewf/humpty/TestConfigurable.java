package co.mewf.humpty;

import co.mewf.humpty.config.Alias;
import co.mewf.humpty.config.Configurable;
import co.mewf.humpty.config.ConfigurationOptionsProvider;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

@Alias("testConfigurable")
public class TestConfigurable implements PostProcessor, PreProcessor, CompilingProcessor, Configurable, ConfigurationOptionsProvider {

  private String message = "failed!";

  @Override
  public Map<Class<?>, Map<String, Object>> getOptions() {
    HashMap<Class<?>, Map<String, Object>> configuration = new HashMap<Class<?>, Map<String,Object>>();
    HashMap<String, Object> options = new HashMap<String, Object>();
    options.put("message", "passed!");
    configuration.put(TestConfigurable.class, options);

    return configuration;
  }

  @Override
  public void configure(Map<String, Object> options) {
    message = (String) options.get("message");
  }

  @Override
  public boolean accepts(String asset) {
    return true;
  }

  @Override
  public CompilationResult compile(String assetName, Reader asset, PreProcessorContext context) {
    return new CompilationResult(assetName, new StringReader(message));
  }

  @Override
  public Reader preProcess(String asset, Reader reader, PreProcessorContext context) {
    try {
      return new StringReader(IOUtils.toString(reader) + message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Reader postProcess(String asset, Reader reader, Context context) {
    try {
      return new StringReader(IOUtils.toString(reader) + message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
