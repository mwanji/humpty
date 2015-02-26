package co.mewf.humpty.config;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import co.mewf.humpty.config.Configuration.GlobalOptions;

public class ConfigurationTest {

  @Test
  public void should_set_custom_global_options() throws Exception {
    Configuration configuration = Configuration.load("ConfigurationTest/humpty-custom.toml");
    GlobalOptions globalOptions = configuration.getGlobalOptions();
    
    assertEquals(Paths.get("def"), globalOptions.getAssetsDir());
    assertEquals(Paths.get("abc"), globalOptions.getBuildDir());
  }

  @Test
  public void should_set_default_global_options() throws Exception {
    Configuration configuration = Configuration.load("ConfigurationTest/humpty.toml");
    GlobalOptions globalOptions = configuration.getGlobalOptions();
    
    assertEquals(Paths.get("assets"), globalOptions.getAssetsDir());
    assertEquals(Paths.get("src/main/resources/META-INF/resources"), globalOptions.getBuildDir());
    assertEquals(Paths.get("humpty-digest.toml"), globalOptions.getDigestFile());
    assertEquals(Paths.get("humpty-watch.toml"), globalOptions.getWatchFile());
  }
  
  @Test
  public void should_get_bundle_shorthands_and_longhands() throws Exception {
    List<Bundle> bundles = Configuration.load("ConfigurationTest/humpty-bundle-shorthand-and-longhand.toml").getBundles();
    
    List<String> names = bundles.stream().map(Bundle::getName).sorted().collect(toList());
    List<String> assets = bundles.stream().map(Bundle::stream).flatMap(s -> s).sorted().collect(toList());
    
    assertThat(names, contains("bundle1.js", "bundle2.css", "bundle3.js", "bundle4.js", "bundle5.js"));
    assertThat(assets, contains("app1.css", "app1.js", "app2.css", "app2.js", "app3.js", "app4.js", "app5.js", "app6.js"));
  }
  
  @Test
  public void should_get_bundle_short_hands() throws Exception {
    List<Bundle> bundles = Configuration.load("ConfigurationTest/humpty-bundle-shorthand.toml").getBundles();
    
    List<String> names = bundles.stream().map(Bundle::getName).sorted().collect(toList());
    List<String> assets = bundles.stream().map(Bundle::stream).flatMap(s -> s).sorted().collect(toList());
    
    assertThat(names, contains("bundle1.js", "bundle2.css"));
    assertThat(assets, contains("app1.css", "app1.js", "app2.css", "app2.js"));
  }
  
  @Test
  public void should_get_bundle_shorthands_and_longhands_from_path() throws Exception {
    List<Bundle> bundles = Configuration.load(Paths.get("src/test/resources/ConfigurationTest/humpty-bundle-shorthand-and-longhand.toml")).getBundles();
    
    List<String> names = bundles.stream().map(Bundle::getName).sorted().collect(toList());
    List<String> assets = bundles.stream().map(Bundle::stream).flatMap(s -> s).sorted().collect(toList());
    
    assertThat(names, contains("bundle1.js", "bundle2.css", "bundle3.js", "bundle4.js", "bundle5.js"));
    assertThat(assets, contains("app1.css", "app1.js", "app2.css", "app2.js", "app3.js", "app4.js", "app5.js", "app6.js"));
  }
  
  @Test
  public void should_get_bundle_short_hands_from_path() throws Exception {
    List<Bundle> bundles = Configuration.load(Paths.get("src/test/resources/ConfigurationTest/humpty-bundle-shorthand.toml")).getBundles();
    
    List<String> names = bundles.stream().map(Bundle::getName).sorted().collect(toList());
    List<String> assets = bundles.stream().map(Bundle::stream).flatMap(s -> s).sorted().collect(toList());
    
    assertThat(names, contains("bundle1.js", "bundle2.css"));
    assertThat(assets, contains("app1.css", "app1.js", "app2.css", "app2.js"));
  }
}
