package com.bielu.gpw;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.config.Configuration;
import com.bielu.gpw.config.MenuItemInfo;

public class GpwTray {

  private static final Log LOG = LogFactory.getLog(GpwTray.class);
  private static final String NO_MESSAGE_APPEARED_BEFORE = "No message appeared before.";

  private final TrayIcon trayIcon;
  private final SystemTray tray;
  private final Closeable closableObj;
  private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private boolean exiting = false;

  private volatile String lastMessage = NO_MESSAGE_APPEARED_BEFORE;
  private volatile String lastMessageTitle = NO_MESSAGE_APPEARED_BEFORE;
  private volatile String currentMessage = NO_MESSAGE_APPEARED_BEFORE;
  private volatile String currentMessageTitle = NO_MESSAGE_APPEARED_BEFORE;
  private volatile String lastRecommendation = NO_MESSAGE_APPEARED_BEFORE;
  private volatile String lastRecommendationTitle = NO_MESSAGE_APPEARED_BEFORE;
  private final CountDownLatch latch;

  protected GpwTray(Closeable closable, CountDownLatch latch) {
    this.latch = latch;
    closableObj = closable;
    boolean init = false;

    if (SystemTray.isSupported()) {
      PopupMenu popup = setupPopupMenu();
      MenuItem exitItem = new MenuItem("Exit");
      exitItem.addActionListener(new ExitListener());
      popup.add(exitItem);

      tray = SystemTray.getSystemTray();
      Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/web.png"));
      trayIcon = new TrayIcon(image, "GPW Monitor", popup);

      ActionListener actionListener = getActionListener();
      if (actionListener != null) {
        trayIcon.addActionListener(actionListener);
      }

      try {
        tray.add(trayIcon);
        init = true;
      } catch (AWTException e) {
        LOG.error("Unable to create Tray Icon", e);
      }
    } else {
      tray = null;
      trayIcon = null;
    }

    if (init == false) {
      throw new IllegalStateException("Could not initialize system tray");
    }
  }

  private ActionListener getActionListener() {
    for (MenuItemInfo info : Configuration.getInstance().getMenuItemList()) {
      if ("action".equals(info.getType())) {
        try {
          Class<?> clazz = Class.forName(info.getClassName());
          return ActionListener.class.cast(clazz.getConstructor(getClass()).newInstance(this));
        } catch (Exception e) {
          LOG.warn(String.format("Unable to instantiate class [%s]", info.getClassName()));
        }
      }
    }
    return null;
  }

  private PopupMenu setupPopupMenu() {
    PopupMenu popup = new PopupMenu();
    for (MenuItemInfo info : Configuration.getInstance().getMenuItemList()) {
      if ("separator".equals(info.getType())) {
        popup.addSeparator();
        continue;
      }
      MenuItem item = new MenuItem(info.getLabel());
      try {
        Class<?> clazz = Class.forName(info.getClassName());
        item.addActionListener(ActionListener.class.cast(clazz.getConstructor(getClass()).newInstance(this)));
        popup.add(item);
      } catch (Exception e) {
        LOG.error(
            String.format("Class [%s] does not exist in the classpath " + "or could not be instantiated",
                info.getClassName()), e);
      }
    }
    return popup;
  }

  public void displayRecommendation(String string) {
    lastRecommendationTitle = String.format("%s - %s", "GPW Notifier", formatter.format(new Date()));
    lastRecommendation = string;
    trayIcon.displayMessage(lastRecommendationTitle, lastRecommendation, MessageType.WARNING);
  }

  public void displayLastMessage() {
    trayIcon.displayMessage(lastMessageTitle, lastMessage, MessageType.INFO);
  }

  public void displayLastRecommendation() {
    trayIcon.displayMessage(lastRecommendationTitle, lastRecommendation, MessageType.WARNING);
  }

  public void displayMessage(String string) {
    lastMessageTitle = String.format("%s - %s", "GPW Notifier", formatter.format(new Date()));
    lastMessage = string;
    trayIcon.displayMessage(lastMessageTitle, lastMessage, MessageType.INFO);
  }

  public void displayCurrentMessage() {
    trayIcon.displayMessage(currentMessageTitle, currentMessage, MessageType.INFO);
  }

  public void registerCurrentMessage(String title, String message) {
    currentMessage = message;
    currentMessageTitle = title;
  }

  public void close() {
    if (exiting == true) {
      return;
    }

    exiting = true;
    tray.remove(trayIcon);
    for (Frame frame : Frame.getFrames()) {
      frame.dispose();
    }
    closableObj.close();
  }

  private class ExitListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      close();
      latch.countDown();
    }
  }
}
