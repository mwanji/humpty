package co.mewf.humpty;

import co.mewf.humpty.config.HumptyBootstrap;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableHumptyBootstrap extends HumptyBootstrap {

  private final Processor[] allProcessors;

  public ConfigurableHumptyBootstrap(Processor... processors) {
    this.allProcessors = processors;
  }

  @Override
  protected List<? extends CompilingProcessor> getCompilingProcessors() {
    ArrayList<CompilingProcessor> processors = new ArrayList<CompilingProcessor>();
    for (Processor processor : allProcessors) {
      if (processor instanceof CompilingProcessor) {
        processors.add((CompilingProcessor) processor);
      }
    }
    return processors;
  };

  @Override
  protected List<? extends PreProcessor> getPreProcessors() {
    ArrayList<PreProcessor> processors = new ArrayList<PreProcessor>();
    for (Processor processor : allProcessors) {
      if (processor instanceof PreProcessor) {
        processors.add((PreProcessor) processor);
      }
    }
    return processors;
  }

  @Override
  protected List<? extends PostProcessor> getPostProcessors() {
    ArrayList<PostProcessor> processors = new ArrayList<PostProcessor>();
    for (Processor processor : allProcessors) {
      if (processor instanceof PostProcessor) {
        processors.add((PostProcessor) processor);
      }
    }
    return processors;
  }
}
