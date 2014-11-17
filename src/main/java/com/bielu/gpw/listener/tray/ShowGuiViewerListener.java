package com.bielu.gpw.listener.tray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.GpwTray;
import com.bielu.gpw.listener.gui.SwingDataViewerListener;

public class ShowGuiViewerListener implements ActionListener {

    private static final Log LOG = LogFactory.getLog(ShowGuiViewerListener.class);

    public ShowGuiViewerListener(GpwTray gpwTray) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (SwingDataViewerListener.getInstance() != null) {
            SwingDataViewerListener.getInstance().getDataViewerWindow().setVisible(true);
        } else {
            LOG.warn("GUI component is not initialized correctly - window's instance is null");
        }
    }
}
