package co.mewf.humpty.spi.processors;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

public class ConfigurableProcessor implements BundleProcessor, AssetProcessor, SourceProcessor {

  private String message = "failed!";
  public Configuration.Options options;
  
  @Override
  public String getName() {
    return "configurable";
  }

  @Inject
  public void init(Configuration.Options options) {
    this.options = options;
    message = options.get("message", null);
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
