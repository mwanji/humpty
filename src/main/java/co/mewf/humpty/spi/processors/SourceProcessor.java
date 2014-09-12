package co.mewf.humpty.spi.processors;

import java.util.function.BiFunction;

import co.mewf.humpty.config.PreProcessorContext;

public interface SourceProcessor extends Processor {

  SourceProcessor.CompilationResult compile(CompilationResult compilationResult, PreProcessorContext context);
  
  static BiFunction<SourceProcessor.CompilationResult, SourceProcessor, SourceProcessor.CompilationResult> maybe(String name, PreProcessorContext context) {
    return (compilationResult, processor) -> processor.accepts(name) ? processor.compile(compilationResult, context) : compilationResult;
  }

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
