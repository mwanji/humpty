package co.mewf.humpty.spi.processors;

import java.io.Reader;

import co.mewf.humpty.config.PreProcessorContext;
import co.mewf.humpty.spi.processors.AssetProcessor;

public class EchoProcessor implements AssetProcessor {

  
  @Override
  public String getName() {
    return "echo";
  }
  
  @Override
  public boolean accepts(String asset) {
    return true;
  }

  @Override
  public Reader processAsset(String asset, Reader reader, PreProcessorContext context) {
    return reader;
  }
}
