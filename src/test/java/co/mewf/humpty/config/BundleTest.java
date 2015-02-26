package co.mewf.humpty.config;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class BundleTest {

  @Test
  public void should_accept_asset_defined_without_extension() throws Exception {
    List<Bundle> bundles = Configuration.load("BundleTest/humpty.toml").getBundles();
    Collections.sort(bundles, (b1, b2) -> b1.getName().compareTo(b2.getName()));
    
    assertTrue(bundles.get(0).accepts("app1.js"));
    assertTrue(bundles.get(1).accepts("app2.css"));
    assertTrue(bundles.get(1).accepts("app2_1.css"));
    assertTrue(bundles.get(1).accepts("app2_2.css"));
    assertTrue(bundles.get(2).accepts("app3.js"));
  }
}
