package co.mewf.humpty.spi.processors;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;

import co.mewf.humpty.config.Context;
import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.processors.AssetProcessor;
import co.mewf.humpty.spi.processors.BundleProcessor;
import co.mewf.humpty.spi.processors.SourceProcessor;

public class TestProcessor implements BundleProcessor, AssetProcessor, SourceProcessor {

  @Override
  public String getName() {
    return "test";
  }
  
  @Override
  public boolean accepts(String asset) {
    return true;
  }

  @Override
  public CompilationResult compile(String assetName, Reader asset, PreProcessorContext context) {
    try {
      return new CompilationResult(assetName, new StringReader("Compiled!" + IOUtils.toString(asset)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Reader processAsset(String asset, Reader reader, PreProcessorContext context) {
    try {
      return new StringReader("Preprocessed!" + IOUtils.toString(reader));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Reader processBundle(String asset, Reader reader, Context context) {
    try {
      return new StringReader(IOUtils.toString(reader) + "Postprocessed!");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
