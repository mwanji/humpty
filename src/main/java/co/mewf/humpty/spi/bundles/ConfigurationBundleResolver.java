package co.mewf.humpty.spi.bundles;

import java.util.List;

import javax.inject.Inject;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

public class ConfigurationBundleResolver implements BundleResolver {

  private List<Bundle> bundles;

  @Override
  public String getName() {
    return "configurationBundleResolver";
  }

  @Override
  public boolean accepts(String originalAssetName) {
    String bundleName;
    
    if (originalAssetName.indexOf('/') > -1) {
      String[] split = originalAssetName.split("/", 2);
      bundleName = split[0];
    } else {
      bundleName = originalAssetName;
    }
    
    return bundles.stream().filter(b -> b.accepts(bundleName)).findFirst().isPresent();
  }

  @Override
  public Bundle resolve(String originalAssetName) {
    final String bundleName;
    final String assetInBundleName;
    
    if (originalAssetName.indexOf('/') > -1) {
      String[] split = originalAssetName.split("/", 2);
      bundleName = split[0];
      assetInBundleName = split[1];
    } else {
      bundleName = originalAssetName;
      assetInBundleName = null;
    }
    
    Bundle bundle = bundles.stream().filter(b -> b.accepts(bundleName)).findFirst().get();

    if (assetInBundleName != null) {
      bundle = bundle.getBundleFor(assetInBundleName);
    }
    
    return bundle;
  }
  
  @Inject
  public void configure(Configuration configuration) {
    this.bundles = configuration.getBundles();
  }
}
