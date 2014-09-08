package co.mewf.humpty.spi.processors;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

public class AppendingProcessor implements SourceProcessor, AssetProcessor, BundleProcessor {
  
  private String message;

  @Override
  public boolean accepts(String assetName) {
    return true;
  }

  @Override
  public String getName() {
    return "appender";
  }

  @Override
  public Reader processBundle(String assetName, Reader asset, Context context) {
    return new StringReader(toString(asset) + message);
  }

  @Override
  public Reader processAsset(String assetName, Reader asset, PreProcessorContext context) {
    return new StringReader(toString(asset) + message);
  }

  @Override
  public CompilationResult compile(String assetName, Reader asset, PreProcessorContext context) {
    return new CompilationResult(assetName, new StringReader(toString(asset) + message));
  }
  
  @Inject
  public void configure(Configuration.Options options) {
    message = options.get("message", null);
  }
  
  private String toString(Reader r) {
    try {
      return IOUtils.toString(r);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
