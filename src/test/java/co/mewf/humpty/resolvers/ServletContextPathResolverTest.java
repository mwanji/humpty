package co.mewf.humpty.resolvers;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.HumptyBootstrap;

import java.io.File;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.mockito.Mockito;

public class ServletContextPathResolverTest {

  @Test
  public void should_inject_ServletContext() {
    ServletContext servletContext = Mockito.mock(ServletContext.class);
    Mockito.when(servletContext.getRealPath("/asset.js")).thenReturn(new File("src/test/resources/META-INF/resources/webjars/humpty/1.0.0/blocks.js").getAbsolutePath());

    Pipeline pipeline = new HumptyBootstrap.Builder().humptyFile("/humpty-servlet-context.json").build(servletContext).createPipeline();

    pipeline.process("single.js"); // fails if real path returned by ServletContext does not exist
  }
}
