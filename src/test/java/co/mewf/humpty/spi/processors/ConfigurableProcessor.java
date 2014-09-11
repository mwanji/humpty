package co.mewf.humpty.spi.processors;

import javax.inject.Inject;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;

public class ConfigurableProcessor implements BundleProcessor, AssetProcessor, SourceProcessor {

  private String message = "failed!";
  private boolean accepts;
  public Configuration.Options options;
  
  @Override
  public String getName() {
    return "configurable";
  }

  @Inject
  public void init(Configuration.Options options) {
    this.options = options;
    message = options.get("message", null);
    accepts = options.get("accepts", Boolean.TRUE);
  }

  @Override
  public boolean accepts(String asset) {
    return accepts;
  }

  @Override
  public CompilationResult compile(String assetName, String asset, PreProcessorContext context) {
    return new CompilationResult(assetName, message);
  }

  @Override
  public String processAsset(String assetName, String asset, PreProcessorContext context) {
      return asset + message;
  }

  @Override
  public String processBundle(String assetName, String asset, Context context) {
      return asset + message;
  }
}
