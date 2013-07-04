package co.mewf.humpty.resolvers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebJarResolverTest {

  private final WebJarResolver resolver = new WebJarResolver();

  @Test
  public void should_accept_uri_with_no_scheme() {
    assertTrue(resolver.accepts("jquery.js"));
  }

  @Test
  public void should_accept_uri_with_webjar_scheme() {
    assertTrue(resolver.accepts("webjar:jquery.js"));
  }

  @Test
  public void should_reject_uri_with_other_scheme() {
    assertFalse(resolver.accepts("other:jquery.js"));
  }

  @Test
  public void should_reject_uri_with_leading_slash() {
    assertFalse(resolver.accepts("/jquery.js"));
  }
}
