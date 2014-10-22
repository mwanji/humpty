package co.mewf.humpty.spi.bundles;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;

/**
 * <p>
 * Handles assets found in a webjar that are requested without reference to a bundle.
 * This may be the case for automatically-generated URLs such as source maps.
 * </p>
 * 
 * <p>
 * For example: /humpty/jquery.min.map
 * </p>
 *
 */
public class WebJarAssetBundleResolver implements BundleResolver {

  private WebJarAssetLocator locator;
  private List<Bundle> bundles;

  @Override
  public String getName() {
    return "webJarAssetBundleResolver";
  }

  /**
   * @return true if bundleName does not match an existing bundle and can be found in a webjar
   */
  @Override
  public boolean accepts(String bundleName) {
    if (bundles.stream().filter(b -> b.getName().equals(bundleName)).findFirst().isPresent()) {
      return false;
    }
    
    try {
      locator.getFullPath(bundleName);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public Bundle resolve(String bundleName) {
    return new Bundle(bundleName, Collections.singletonList(bundleName));
  }
  
  @Inject
  public void configure(Configuration configuration, WebJarAssetLocator locator) {
    this.locator = locator;
    this.bundles = configuration.getBundles();
  }

}
