package com.bielu.gpw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.domain.Recommendation;
import com.bielu.gpw.domain.Wallet;
import com.bielu.gpw.listener.ChangeListener;
import com.bielu.gpw.listener.DataWriterListener;
import com.bielu.gpw.listener.DatabaseWriterListener;
import com.bielu.gpw.listener.LoggingListener;
import com.bielu.gpw.listener.RecommendationFileWriterListener;
import com.bielu.gpw.listener.TrayListener;
import com.bielu.gpw.listener.gui.SwingDataViewerListener;
import com.bielu.gpw.listener.gui.plugin.TextOutputStream;
import com.bielu.gpw.listener.gui.plugin.TextPrintStream;
import com.bielu.gpw.listener.tray.RecommendationTrayListener;
import com.bielu.gpw.task.QuoteReaderTask;
import com.bielu.gpw.task.RecommendationReaderTask;

public final class Runner {

  static {
    System.setOut(new TextPrintStream(new TextOutputStream(System.out)));
    System.setErr(new TextPrintStream(new TextOutputStream(System.err)));
  }

  private static final long TASK_PERIOD = 30L * 1000L;
  private static final int HOUR = 3600;
  private static final int TEN_SECONDS = 10;
  private static final Log LOG = LogFactory.getLog(Runner.class);
  private static final String WINDOWS_LOOK_AND_FEEL = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

  private static GpwMonitor gpwMon;
  private static long lastModification;

  public static void main(String[] args) {
    try {
      Class.forName(WINDOWS_LOOK_AND_FEEL);
      UIManager.setLookAndFeel(WINDOWS_LOOK_AND_FEEL);
    } catch (Exception e) {
      // ignore
    }

    if (isRunning()) {
      LOG.error("Another instance is already running - only one instance can be up.");
      JOptionPane.showMessageDialog(null, "Another instance of 'GPW Monitor' "
          + "is already running - only one instance can be up.", "GPW Monitor", JOptionPane.ERROR_MESSAGE);

      return;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    gpwMon = initialize(latch);
    lastModification = gpwMon.getLastDbModification();
    if (gpwMon == null) {
      return;
    }

    Timer timer = new Timer("Shares DB Modification Monitor");
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        if (lastModification < gpwMon.getLastDbModification()) {
          gpwMon.close();

          gpwMon = initialize(latch);
          lastModification = gpwMon.getLastDbModification();
          if (gpwMon == null) {
            cancel();
          }
        }
      }
    };
    timer.scheduleAtFixedRate(task, 0, TASK_PERIOD); // each minute
    try {
      latch.await();
    } catch (InterruptedException e) {
      // ignore
    }
    timer.cancel();
    System.exit(0);
  }

  private static GpwMonitor initialize(CountDownLatch latch) {
    GpwMonitor monitor = new GpwMonitor(latch);
    final Wallet myWallet = monitor.createWallet();
    if (myWallet == null || myWallet.size() == 0) {
      LOG.error("Wallet is empty - exiting application");
      return null;
    }

    final List<ChangeListener<Wallet>> quoteListeners = new ArrayList<ChangeListener<Wallet>>();
    quoteListeners.add(new DataWriterListener(myWallet));
    quoteListeners.add(new LoggingListener(myWallet));
    quoteListeners.add(new TrayListener(myWallet, monitor.getGpwTray()));
    quoteListeners.add(new DatabaseWriterListener());

    final List<ChangeListener<Object>> objectListeners = new ArrayList<ChangeListener<Object>>();
    objectListeners.add(SwingDataViewerListener.getInstance(myWallet));

    final List<ChangeListener<List<Recommendation>>> recListeners = new ArrayList<ChangeListener<List<Recommendation>>>();
    recListeners.add(new RecommendationTrayListener(monitor.getGpwTray()));
    recListeners.add(new RecommendationFileWriterListener());

    monitor.scheduleAtFixedRate(new QuoteReaderTask(myWallet, quoteListeners, objectListeners), 0, 1, TimeUnit.MINUTES);
    monitor.scheduleAtFixedRate(new RecommendationReaderTask(myWallet, recListeners, objectListeners), TEN_SECONDS,
        HOUR, TimeUnit.SECONDS);

    return monitor;
  }

  private static boolean isRunning() {
    try {
      return new RandomAccessFile(".lock", "rw").getChannel().tryLock() == null;
    } catch (IOException e) {
      throw new IllegalStateException("Could not lock the file '.lock'", e);
    }
  }

  private Runner() {
  }
}
