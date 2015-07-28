package com.bielu.gpw.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bielu.gpw.GpwTray;
import com.bielu.gpw.Util;
import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;

public class TrayListener implements ChangeListener<Wallet> {

  private static final int X7 = 7;

  private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final Wallet myWallet;
  private final GpwTray gpwTray;
  private volatile Wallet checkPoint;

  public TrayListener(Wallet myWallet, GpwTray gpwTray) {
    this.myWallet = myWallet;
    this.gpwTray = gpwTray;
  }

  @Override
  public void stateChanged(Wallet current) {
    if (getCheckPoint() == null) {
      setCheckPoint(current);
    }

    StringBuilder sb = new StringBuilder();
    if (getCheckPoint() != current) {
      appendDiff(getCheckPoint(), current, sb);
      sb.append("\n---\n");
    }
    appendDiff(myWallet, getCheckPoint(), sb);

    String currentMessageTitle = String.format("%s - %s", "GPW Notifier", formatter.format(new Date()));
    String currentMessage = sb.toString();
    gpwTray.registerCurrentMessage(currentMessageTitle, currentMessage);

    if (Util.isThresholdReached(current, checkPoint) || getCheckPoint() == current) {
      setCheckPoint(current);
      gpwTray.displayMessage(currentMessage);
    }
  }

  private synchronized void setCheckPoint(Wallet checkPointWallet) {
    this.checkPoint = checkPointWallet;
  }

  private synchronized Wallet getCheckPoint() {
    return checkPoint;
  }

  private void appendDiff(Wallet original, Wallet current, StringBuilder sb) {
    for (int i = 0; i < current.size(); i++) {
      ShareInfo cur = current.getShareInfo(i);
      ShareInfo wal = original.getShareInfo(i);
      if (cur.hasError()) {
        sb.append(String.format(getErrorFormat(cur.getName()), cur.getName(), cur.getError()));
      } else {
        sb.append(String.format(getFormat(cur.getName()), cur.getName(), cur.getQuote(), Util.percentageSigned(wal, cur)));
      }
    }
    sb.append(String.format("Total:\t\t" + Util.FORMAT_MONEY + "\t(%s)", current.value(), Util.percentageSigned(original, current)));
  }

  private String getFormat(String name) {
    if (name.length() < X7) {
      return "%s\t\t" + Util.FORMAT_MONEY + "\t(%s)\n";
    }
    return "%s\t" + Util.FORMAT_MONEY + "\t(%s)\n";
  }

  private String getErrorFormat(String name) {
    if (name.length() < X7) {
      return "%s\t\t%s\n";
    }
    return "%s\t%s\n";
  }
}
