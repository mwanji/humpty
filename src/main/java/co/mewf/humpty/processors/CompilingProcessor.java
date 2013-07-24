package co.mewf.humpty.processors;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;

public interface CompilingProcessor extends Processor {

  boolean accepts(String assetName);
  CompilingProcessor.CompilationResult compile(String assetName, Reader asset, PreProcessorContext context);

  public static class CompilationResult {
    private final Reader asset;
    private final String assetName;

    public CompilationResult(String assetName, Reader asset) {
      this.asset = asset;
      this.assetName = assetName;
    }

    public Reader getAsset() {
      return asset;
    }

    public String getAssetName() {
      return assetName;
    }
  }
}
