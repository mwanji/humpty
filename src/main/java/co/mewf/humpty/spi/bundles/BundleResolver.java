package co.mewf.humpty.spi.bundles;

import co.mewf.humpty.config.Bundle;
import co.mewf.humpty.spi.PipelineElement;

public interface BundleResolver extends PipelineElement {

  boolean accepts(String bundleName);
  Bundle resolve(String bundleName);
}
