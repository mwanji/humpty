package co.mewf.humpty.spi.processors;

import co.mewf.humpty.config.PreProcessorContext;

public interface SourceProcessor extends Processor {

  SourceProcessor.CompilationResult compile(String assetName, String asset, PreProcessorContext context);

  public static class CompilationResult {
    private final String asset;
    private final String assetName;

    public CompilationResult(String assetName, String asset) {
      this.asset = asset;
      this.assetName = assetName;
    }

    public String getAsset() {
      return asset;
    }

    public String getAssetName() {
      return assetName;
    }
  }
}
