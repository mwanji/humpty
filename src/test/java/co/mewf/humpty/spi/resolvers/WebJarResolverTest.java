package co.mewf.humpty.spi.resolvers;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;

public class WebJarResolverTest {

  private final WebJarResolver resolver = new WebJarResolver();
  private final HashMap<String, Object> options = new HashMap<String, Object>();
  private final Bundle libs = Configuration.load("/co/mewf/humpty/spi/resolvers/should_expand_uri.toml").getBundles().get(0);
  
  @Before
  public void before() {
    options.put("rootDir", "src/test/resources");
    resolver.configure(new Configuration.Options(options));
  }

  @Test
  public void should_accept_uri_without_scheme() {
    assertTrue(resolver.accepts("jquery.js"));
  }

  @Test
  public void should_reject_uri_with_scheme() {
    assertFalse(resolver.accepts("other:jquery.js"));
  }

  @Test
  public void should_reject_uri_with_leading_slash() {
    assertFalse(resolver.accepts("/jquery.js"));
  }

  @Test
  public void should_expand_uri() {
    List<String> assetFilePaths = resolver.resolve("jquery.js", new Context(Configuration.Mode.DEVELOPMENT, libs)).stream().map(AssetFile::getPath).collect(toList());

    assertThat(assetFilePaths, contains("META-INF/resources/webjars/jquery/2.1.1/jquery.js"));
  }
  
  @Test
  public void should_provide_minified_assets_in_production_mode() throws Exception {
    Context context = new Context(Configuration.Mode.PRODUCTION, libs);
    List<String> assetFilePaths = resolver.resolve("jquery.js", context).stream().map(AssetFile::getPath).collect(toList());
    assetFilePaths.addAll(resolver.resolve("web_server.js", context).stream().map(AssetFile::getPath).collect(toList()));

    assertThat(assetFilePaths, contains("META-INF/resources/webjars/jquery/2.1.1/jquery.min.js", "META-INF/resources/webjars/humpty/1.0.0/web_server.min.js"));
  }
  
  @Test
  public void should_not_provide_minified_asset_in_production_mode_when_not_available() throws Exception {
    List<String> assetFilePaths = resolver.resolve("alert.js", new Context(Configuration.Mode.PRODUCTION, libs)).stream().map(AssetFile::getPath).collect(toList());

    assertThat(assetFilePaths, contains("META-INF/resources/webjars/humpty/1.0.0/alert.js"));
  }
  
  @Test
  public void should_not_provide_minified_asset_when_preferMin_is_false() throws Exception {
    options.put("preferMin", Boolean.FALSE);
    resolver.configure(new Configuration.Options(options));
    
    Context context = new Context(Configuration.Mode.PRODUCTION, libs);
    List<String> assetFilePaths = resolver.resolve("jquery.js", context).stream().map(AssetFile::getPath).collect(toList());
    assetFilePaths.addAll(resolver.resolve("web_server.js", context).stream().map(AssetFile::getPath).collect(toList()));

    assertThat(assetFilePaths, contains("META-INF/resources/webjars/jquery/2.1.1/jquery.js", "META-INF/resources/webjars/humpty/1.0.0/web_server.js"));
  }
  
  @Test
  public void should_provide_minified_assets_in_development_mode_when_preferMin_is_true() throws Exception {
    options.put("preferMin", Boolean.TRUE);
    resolver.configure(new Configuration.Options(options));

    Context context = new Context(Configuration.Mode.DEVELOPMENT, libs);
    List<String> assetFilePaths = resolver.resolve("jquery.js", context).stream().map(AssetFile::getPath).collect(toList());
    assetFilePaths.addAll(resolver.resolve("web_server.js", context).stream().map(AssetFile::getPath).collect(toList()));

    assertThat(assetFilePaths, contains("META-INF/resources/webjars/jquery/2.1.1/jquery.min.js", "META-INF/resources/webjars/humpty/1.0.0/web_server.min.js"));
  }
}
