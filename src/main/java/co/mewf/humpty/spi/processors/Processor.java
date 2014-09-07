package co.mewf.humpty.spi.processors;

import co.mewf.humpty.spi.PipelineElement;

/**
 * A super-interface that exists so all processors can be registered as services in one file.
 */
public interface Processor extends PipelineElement {
  boolean accepts(String assetName);
}
