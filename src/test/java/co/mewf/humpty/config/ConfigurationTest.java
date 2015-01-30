package co.mewf.humpty.config;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Test;

import co.mewf.humpty.config.Configuration.GlobalOptions;

public class ConfigurationTest {

  @Test
  public void should_set_custon_global_options() throws Exception {
    Configuration configuration = Configuration.load("ConfigurationTest/humpty-custom.toml");
    GlobalOptions globalOptions = configuration.getGlobalOptions();
    
    assertEquals(Configuration.Mode.DEVELOPMENT, globalOptions.getMode());
    assertEquals(Paths.get("def"), globalOptions.getAssetsDir());
    assertEquals(Paths.get("abc"), globalOptions.getBuildDir());
    assertEquals(Paths.get("ghi.toml"), globalOptions.getDigestFile());
  }

  @Test
  public void should_set_default_global_options() throws Exception {
    Configuration configuration = Configuration.load("ConfigurationTest/humpty.toml");
    GlobalOptions globalOptions = configuration.getGlobalOptions();
    
    assertEquals(Paths.get("src/main/resources/assets"), globalOptions.getAssetsDir());
    assertEquals(Paths.get("src/main/resources/META-INF/resources"), globalOptions.getBuildDir());
    assertEquals(Configuration.Mode.PRODUCTION, globalOptions.getMode());
    assertEquals(Paths.get("src/main/resources/humpty-digest.toml"), globalOptions.getDigestFile());
  }
}
