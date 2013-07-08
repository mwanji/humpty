package co.mewf.humpty;

import co.mewf.humpty.config.PreProcessorContext;

import java.io.Reader;

public class EchoProcessor implements AssetProcessor {

  @Override
  public boolean accepts(String asset) {
    return true;
  }

  @Override
  public Reader processAsset(String asset, Reader reader, PreProcessorContext context) {
    return reader;
  }
}
