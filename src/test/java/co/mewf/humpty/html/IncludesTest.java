package co.mewf.humpty.html;

import static org.junit.Assert.assertEquals;
import co.mewf.humpty.config.HumptyBootstrap;

import javax.servlet.ServletContext;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class IncludesTest {

  private String rootPath = "/context";
  private ServletContext servletContext = Mockito.mock(ServletContext.class);
  private final Includes devTags = new HumptyBootstrap.Builder().build(servletContext).createTags();
  private final Includes productionTags = new HumptyBootstrap.Builder().humptyFile("/humpty-production.json").build(servletContext).createTags();
  long fixedMillis = new DateTime(2013, DateTimeConstants.JULY, 23, 16, 42).getMillis();

  @Before
  public void before() {
    DateTimeUtils.setCurrentMillisFixed(fixedMillis);
    Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return (String) invocation.getArguments()[0];
      }
    });
  }

  @Test
  public void should_include_unbundled_assets_in_dev_mode() {
    String jsHtml = devTags.generate("tags.js", rootPath);
    String cssHtml = devTags.generate("tags.css", rootPath);

    assertEquals("<script src=\"/context/webjars/jquery/1.8.2/jquery.js\"></script>\n<script src=\"/context/app.js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/context/app1.css\" />\n<link rel=\"stylesheet\" href=\"/context/app2.css\" />\n", cssHtml);
  }

  @Test
  public void should_include_bundles_in_production_mode() {
    String jsHtml = productionTags.generate("tags.js", rootPath);
    String cssHtml = productionTags.generate("tags.css", rootPath);

    assertEquals("<script src=\"/context/tags-humpty" + fixedMillis + ".js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/context/tags-humpty" + fixedMillis + ".css\" />\n", cssHtml);
  }

  @Test
  public void should_handle_root_context_path_in_production_mode() {
    String jsHtml = productionTags.generate("tags.js", "/");
    String cssHtml = productionTags.generate("tags.css", "/");

    assertEquals("<script src=\"/tags-humpty" + fixedMillis + ".js\"></script>\n", jsHtml);
    assertEquals("<link rel=\"stylesheet\" href=\"/tags-humpty" + fixedMillis + ".css\" />\n", cssHtml);
  }

  @After
  public void after() {
    DateTimeUtils.setCurrentMillisSystem();
  }
}
