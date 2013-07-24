
/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA  02111-1307, USA.
 */
package co.mewf.humpty.caches;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapted from http://geosoft.no/software/filemonitor/FileMonitor.java.html
 *
 * Class for monitoring changes in disk files. Usage:
 *
 * 1. Implement the FileListener interface. 2. Create a FileMonitor instance. 3.
 * Add the file(s)/directory(ies) to listen for.
 *
 * fileChanged() will be called when a monitored file is created, deleted or its
 * modified time changes.
 *
 * @author <a href="mailto:info@geosoft.no">GeoSoft</a>
 */
class AssetWatcher {
  private Timer timer;
  private ConcurrentHashMap<File, Long> fileModificationTimes;
  private ArrayList<WeakReference<AssetChangeListener>> assetChangeListeners;

  /**
   * @param pollingInterval Polling interval in milliseconds.
   */
  public AssetWatcher(long pollingInterval, AssetChangeListener assetChangeListener) {
    fileModificationTimes = new ConcurrentHashMap<File, Long>();
    assetChangeListeners = new ArrayList<WeakReference<AssetChangeListener>>();
    assetChangeListeners.add(new WeakReference<AssetChangeListener>(assetChangeListener));

    timer = new Timer(true);
    timer.schedule(new AssetWatcherTask(), 0, pollingInterval);
  }

  /**
   * File may be any java.io.File (including a
   * directory) and may well be a non-existing file in the case where the
   * creating of the file is to be trepped.
   * <p>
   * More than one file can be listened for. When the specified file is created,
   * modified or deleted, listeners are notified.
   */
  public void watch(File file) {
    if (!fileModificationTimes.containsKey(file)) {
      long modifiedTime = file.exists() ? file.lastModified() : -1;
      fileModificationTimes.put(file, Long.valueOf(modifiedTime));
    }
  }

  /**
   * This is the timer thread which is executed every n milliseconds according
   * to the setting of the file monitor. It investigates the file in question
   * and notify listeners if changed.
   */
  private class AssetWatcherTask extends TimerTask {
    @Override
    public void run() {
      // Loop over the registered files and see which have changed.
      // Use a copy of the list in case listener wants to alter the
      // list within its fileChanged method.
      for (Map.Entry<File, Long> entry : fileModificationTimes.entrySet()) {
        File file = entry.getKey();
        long newModifiedTime = file.exists() ? file.lastModified() : -1;
        long lastModifiedTime = entry.getValue().longValue();

        // Check if file has changed
        if (newModifiedTime != lastModifiedTime) {

          // Register new modified time
          fileModificationTimes.put(file, Long.valueOf(newModifiedTime));


          // Notify listeners
          for (Iterator<WeakReference<AssetChangeListener>> iterator = assetChangeListeners.iterator(); iterator.hasNext();) {
            WeakReference<AssetChangeListener> reference = iterator.next();
            AssetChangeListener listener = reference.get();

            // Remove from list if the back-end object has been GC'd
            if (listener == null) {
              iterator.remove();
            } else {
              listener.fileChanged(file);
            }
          }
        }
      }
    }
  }
}
