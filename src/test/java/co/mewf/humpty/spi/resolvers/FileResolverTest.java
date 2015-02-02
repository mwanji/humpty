package co.mewf.humpty.spi.resolvers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Configuration.GlobalOptions;
import co.mewf.humpty.config.Context;

public class FileResolverTest {

  @Test
  public void should_accept_asset_starting_with_slash() throws Exception {
    FileResolver resolver = new FileResolver();
    
    assertTrue(resolver.accepts("/file.js"));
    assertTrue(resolver.accepts("/sub/file.js"));
    assertFalse(resolver.accepts("file.js"));
  }
  
  @Test
  public void should_find_assets_in_folder() throws Exception {
    Configuration configuration = Configuration.load("FileResolverTest/humpty.toml");
    GlobalOptions globalOptions = configuration.getGlobalOptions();
    
    FileResolver resolver = new FileResolver();
    resolver.configure(globalOptions);

    List<AssetFile> assetFiles = resolver.resolve("/asset1.js", new Context(Configuration.Mode.PRODUCTION, configuration.getBundles().get(0)));
    
    assertThat(assetFiles, hasSize(1));
    assertEquals(assetFiles.get(0).getPath(), "src/test/resources/asset1.js");
    assertEquals(assetFiles.get(0).getContents(), "alert(\"asset1\");");
  }
  
  @Test
  public void should_find_assets_in_sub_folder() throws Exception {
    Configuration configuration = Configuration.load("FileResolverTest/humpty.toml");
    GlobalOptions globalOptions = configuration.getGlobalOptions();
    
    FileResolver resolver = new FileResolver();
    resolver.configure(globalOptions);

    List<AssetFile> assetFiles = resolver.resolve("/FileResolverTest/fileResolverAsset.js", new Context(Configuration.Mode.PRODUCTION, configuration.getBundles().get(0)));
    
    assertThat(assetFiles, hasSize(1));
    assertEquals(assetFiles.get(0).getPath(), "src/test/resources/FileResolverTest/fileResolverAsset.js");
    assertEquals(assetFiles.get(0).getContents(), "alert(\"fileResolverAsset\");");
  }
}
