package co.mewf.humpty.spi.processors;

import co.mewf.humpty.config.PreProcessorContext;

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
  public String processAsset(String assetName, String asset, PreProcessorContext context) {
    return asset;
  }
}
