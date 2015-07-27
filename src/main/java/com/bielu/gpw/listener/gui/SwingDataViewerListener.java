package com.bielu.gpw.listener.gui;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.Util;
import com.bielu.gpw.domain.Recommendation;
import com.bielu.gpw.domain.Wallet;
import com.bielu.gpw.listener.ChangeListener;

public final class SwingDataViewerListener implements ChangeListener<Object> {

  private static final Log LOG = LogFactory.getLog(SwingDataViewerListener.class);
  private static SwingDataViewerListener instance;

  private final DataViewerWindow dataViewerWindow;
  private static Wallet wallet;

  public static synchronized SwingDataViewerListener getInstance(Wallet myWallet) {
    if (instance == null || wallet != myWallet) {
      instance = new SwingDataViewerListener(myWallet);
    }
    return instance;
  }

  public static synchronized SwingDataViewerListener getInstance() {
    return instance;
  }

  private SwingDataViewerListener(Wallet myWallet) {
    wallet = myWallet;
    dataViewerWindow = new DataViewerWindow(myWallet);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void stateChanged(Object source) {
    if (source instanceof Wallet) {
      updateWalletData(Wallet.class.cast(source));
    } else if (source instanceof List<?>) {
      updateRecommendationData((List<Recommendation>) source);
    } else {
      LOG.warn(String.format("Unsupported source's class [%s]", source));
    }
  }

  private void updateRecommendationData(List<Recommendation> source) {
  }

  private void updateWalletData(Wallet current) {
    if (dataViewerWindow.getCheckPoint() == null) {
      dataViewerWindow.setCheckPoint(current);
    }
    dataViewerWindow.setCurrentWallet(current);
    dataViewerWindow.refresh();

    if (Util.isThresholdReached(current, dataViewerWindow.getCheckPoint()) || dataViewerWindow.getCheckPoint() == current) {
      dataViewerWindow.setCheckPoint(current);
      dataViewerWindow.refresh();
    }
  }

  public DataViewerWindow getDataViewerWindow() {
    return dataViewerWindow;
  }
}
