package co.mewf.humpty.spi.resolvers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.Context;
import co.mewf.humpty.spi.resolvers.AssetFile;
import co.mewf.humpty.spi.resolvers.WebJarResolver;

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
    List<AssetFile> assetFiles = resolver.resolve("jquery.js", new Context(Configuration.Mode.PRODUCTION, new Bundle("libs.js", asList("jquery.js"))));

    assertThat(assetFiles, hasSize(1));
    assertThat(assetFiles.get(0).getPath(), endsWith("/webjars/jquery/1.8.2/jquery.js"));
  }
}
