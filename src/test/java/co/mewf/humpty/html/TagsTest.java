package co.mewf.humpty.html;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.resolvers.ServletContextPathResolver;
import co.mewf.humpty.resolvers.WebJarResolver;

import org.junit.Test;

public class TagsTest {

  private String rootPath = "/context";

  @Test
  public void should_unbundle_js_assets_in_dev_mode() {
    Configuration configuration = new Configuration(asList(new Bundle("bundle1.js", asList("jquery.js", "/asset2.js"))), Configuration.Mode.DEVELOPMENT);
    String html = new Tags(configuration, asList(new WebJarResolver(), new ServletContextPathResolver())).generate("bundle1.js", rootPath);

    assertEquals("<script src=\"/context/webjars/jquery/1.8.2/jquery.js\"></script>\n<script src=\"/context/asset2.js\"></script>\n", html);
  }

  @Test
  public void should_unbundle_css_assets_in_dev_mode() {
    Configuration configuration = new Configuration(asList(new Bundle("bundle1.css", asList("/asset1.css", "/asset2.css"))), Configuration.Mode.DEVELOPMENT);
    String html = new Tags(configuration, asList(new ServletContextPathResolver())).generate("bundle1.css", rootPath);

    assertEquals("<link rel=\"stylesheet\" href=\"/context/asset1.css\" />\n<link rel=\"stylesheet\" href=\"/context/asset2.css\" />\n", html);
  }

  @Test
  public void should_bundle_assets_in_production_mode() {
    Bundle jsBundle = new Bundle("bundle1.js", asList("webjar:jquery", "/asset2.js"));
    Bundle cssBundle = new Bundle("bundle1.css", asList("/asset1.css", "/asset2.css"));

    Configuration configuration = new Configuration(asList(jsBundle, cssBundle));
    Tags tags = new Tags(configuration, asList(new WebJarResolver(), new ServletContextPathResolver()));

    String jsHtml = tags.generate("bundle1.js", rootPath);
    String cssHtml = tags.generate("bundle1.css", rootPath);

    assertEquals("<script src=\"/context/bundle1.js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/context/bundle1.css\" />\n", cssHtml);
  }
}
