package co.mewf.humpty.caches;

import java.io.File;

public interface FileLocator {

  File locate(String path);
}
