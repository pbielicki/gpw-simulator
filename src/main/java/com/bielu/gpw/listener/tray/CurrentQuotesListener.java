package com.bielu.gpw.listener.tray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.bielu.gpw.GpwTray;

public class CurrentQuotesListener implements ActionListener {

  private final GpwTray gpwTray;

  public CurrentQuotesListener(GpwTray gpwTray) {
    this.gpwTray = gpwTray;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    gpwTray.displayCurrentMessage();
  }
}
