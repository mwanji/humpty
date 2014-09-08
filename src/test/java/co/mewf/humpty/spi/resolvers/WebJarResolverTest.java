package co.mewf.humpty.spi.resolvers;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;

public class WebJarResolverTest {

  private final WebJarResolver resolver = new WebJarResolver();

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
    Bundle libs = Configuration.load("/co/mewf/humpty/spi/resolvers/should_expand_uri.toml").getBundles().get(0);
    
    List<String> assetFilePaths = resolver.resolve("jquery.js", new Context(Configuration.Mode.PRODUCTION, libs)).stream().map(AssetFile::getPath).collect(toList());

    assertThat(assetFilePaths, contains("/webjars/jquery/1.8.2/jquery.js"));
  }
}
