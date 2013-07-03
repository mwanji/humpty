package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;
import java.util.Map;

public interface CompilingProcessor extends Processor {

  boolean accepts(String assetName);
  CompilingProcessor.CompilationResult compile(String assetName, Reader asset, Map<String, Object> options, PreProcessorContext context);

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
