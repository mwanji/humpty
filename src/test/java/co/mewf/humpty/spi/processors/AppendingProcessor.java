package co.mewf.humpty.spi.processors;

import java.io.IOException;
import java.io.Reader;

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
  public String processBundle(String assetName, String asset, Context context) {
    return asset + message;
  }

  @Override
  public String processAsset(String assetName, String asset, PreProcessorContext context) {
    return asset + message;
  }

  @Override
  public CompilationResult compile(String assetName, String asset, PreProcessorContext context) {
    return new CompilationResult(assetName, asset + message);
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
