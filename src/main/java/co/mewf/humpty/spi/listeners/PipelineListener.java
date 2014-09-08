package co.mewf.humpty.spi.listeners;

import co.mewf.humpty.spi.PipelineElement;

public interface PipelineListener extends PipelineElement {

  void onPipelineEnd(String asset, String name);
}
