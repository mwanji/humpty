package co.mewf.humpty.spi.processors;

import javax.inject.Inject;

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
  public CompilationResult compile(SourceProcessor.CompilationResult compilationResult, PreProcessorContext context) {
    return new CompilationResult(compilationResult.getAssetName(), compilationResult.getAsset() + message);
  }
  
  @Inject
  public void configure(Configuration.Options options) {
    message = options.get("message", null);
  }
}
