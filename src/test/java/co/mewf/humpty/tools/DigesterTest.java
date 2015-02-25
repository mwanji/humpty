package co.mewf.humpty.tools;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;

public class DigesterTest {
  
  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();
  private Path digestTomlPath;
  private Path buildDir;
  
  private final Configuration configuration = Configuration.load("DigesterTest/humpty.toml");
  private final Pipeline pipeline = new HumptyBootstrap(configuration).createPipeline();
  private final Digester digest = new Digester();
  
  @Before
  public void before() {
    digestTomlPath = tmpDir.getRoot().toPath().resolve("humpty-digest.toml");
    buildDir = tmpDir.getRoot().toPath().resolve("build");
  }
  
  @Test
  public void should_provide_bundle_digest() throws Exception {
    Map<String, Path> digestToml = digest.processBundles(pipeline, configuration.getBundles(), buildDir);
    
    String digestValue = digestToml.get("app.js").toString();
    assertThat(digestValue, allOf(startsWith("app-humpty"), endsWith(".js")));
    assertThat(digestValue.length(), greaterThan(13));
  }
  
  @Test
  public void should_return_null_for_unknown_asset() throws Exception {
    Map<String, Path> digestToml = digest.processBundles(pipeline, configuration.getBundles(), buildDir);
    
    assertNull(digestToml.get("unknown"));
  }
  
  @Test
  public void should_write_bundles_to_build_directory() throws Exception {
    Map<String, Path> digestToml = digest.processBundles(pipeline, configuration.getBundles(), buildDir);
    
    List<String> compiledAssets = Files.list(buildDir).map(Path::getFileName).map(Path::toString).sorted().collect(Collectors.toList());
    
    assertThat(compiledAssets, contains(digestToml.get("app.js").toString(), digestToml.get("app.js").toString() + ".gz", digestToml.get("bapp.js").toString(), digestToml.get("bapp.js").toString() + ".gz"));
  }
  
  @Test
  public void should_always_process_bundles_in_production_mode() throws Exception {
    Pipeline pipeline = mock(Pipeline.class);
    when(pipeline.process("app.js")).thenReturn(new Pipeline.Output("", "abc"));
    when(pipeline.process("bapp.js")).thenReturn(new Pipeline.Output("", "abc"));
    
    digest.processBundles(pipeline, configuration.getBundles(), buildDir);
  }
}
