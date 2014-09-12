package co.mewf.humpty.spi.processors;

import co.mewf.humpty.spi.PipelineElement;

public interface Processor extends PipelineElement {
  boolean accepts(String assetName);
}
