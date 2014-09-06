package co.mewf.humpty;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.ConfigurationOptionsProvider;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.processors.AssetProcessor;
import co.mewf.humpty.processors.BundleProcessor;
import co.mewf.humpty.processors.CompilingProcessor;

public class ConfigurableProcessor implements BundleProcessor, AssetProcessor, CompilingProcessor, ConfigurationOptionsProvider {

  private String message = "failed!";
  public Configuration.Options options;
  
  @Override
  public String getAlias() {
    return "configurable";
  }

  @Override
  public Map<Class<?>, Map<String, Object>> getOptions() {
    HashMap<Class<?>, Map<String, Object>> configuration = new HashMap<Class<?>, Map<String,Object>>();
    HashMap<String, Object> options = new HashMap<String, Object>();
    options.put("message", "passed!");
    configuration.put(ConfigurableProcessor.class, options);

    return configuration;
  }

  @Inject
  public void init(Configuration.Options options) {
    this.options = options;
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
  public Reader processAsset(String asset, Reader reader, PreProcessorContext context) {
    try {
      return new StringReader(IOUtils.toString(reader) + message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Reader processBundle(String asset, Reader reader, Context context) {
    try {
      return new StringReader(IOUtils.toString(reader) + message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
