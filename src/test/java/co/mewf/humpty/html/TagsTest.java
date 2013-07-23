package co.mewf.humpty.html;

import static org.junit.Assert.assertEquals;
import co.mewf.humpty.config.HumptyBootstrap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TagsTest {

  private String rootPath = "/context";
  private final Tags tags = new HumptyBootstrap.Builder().build().createTags();
  long fixedMillis = new DateTime(2013, DateTimeConstants.JULY, 23, 16, 42).getMillis();

  @Before
  public void before() {
    DateTimeUtils.setCurrentMillisFixed(fixedMillis);
  }

  @Test
  public void should_unbundle_assets_in_dev_mode() {
    String jsHtml = tags.generate("tags.js", rootPath);
    String cssHtml = tags.generate("tags.css", rootPath);

    assertEquals("<script src=\"/context/webjars/jquery/1.8.2/jquery.js\"></script>\n<script src=\"/context/app.js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/context/app1.css\" />\n<link rel=\"stylesheet\" href=\"/context/app2.css\" />\n", cssHtml);
  }

  @Test
  public void should_bundle_assets_in_production_mode() {
    Tags tags = new HumptyBootstrap.Builder().humptyFile("/humpty-production.json").build().createTags();

    String jsHtml = tags.generate("tags.js", rootPath);
    String cssHtml = tags.generate("tags.css", rootPath);

    assertEquals("<script src=\"/context/tags-humpty" + fixedMillis + ".js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/context/tags-humpty" + fixedMillis + ".css\" />\n", cssHtml);
  }

  @Test
  public void should_bundle_assets_in_production_mode_with_no_context_path() {
    Tags tags = new HumptyBootstrap.Builder().humptyFile("/humpty-production.json").build().createTags();

    String jsHtml = tags.generate("tags.js", "/");
    String cssHtml = tags.generate("tags.css", "/");

    assertEquals("<script src=\"/tags-humpty" + fixedMillis + ".js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/tags-humpty" + fixedMillis + ".css\" />\n", cssHtml);
  }

  @After
  public void after() {
    DateTimeUtils.setCurrentMillisSystem();
  }
}
