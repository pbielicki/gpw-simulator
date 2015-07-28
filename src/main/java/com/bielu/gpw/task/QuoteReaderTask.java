package com.bielu.gpw.task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.Util;
import com.bielu.gpw.config.Configuration;
import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;
import com.bielu.gpw.listener.ChangeListener;

public class QuoteReaderTask implements Runnable {

  private static final Log LOG = LogFactory.getLog(QuoteReaderTask.class);
  private static final int X7 = 7;
  private static final String QUOTE_DELIM = "<td>";

  private final Wallet myWallet;
  private final List<ChangeListener<Wallet>> listeners;
  private final List<ChangeListener<Object>> objectListeners;

  public QuoteReaderTask(Wallet myWallet, List<ChangeListener<Wallet>> listeners,
      List<ChangeListener<Object>> objectListeners) {

    this.myWallet = myWallet;
    this.listeners = Collections.unmodifiableList(listeners);
    this.objectListeners = Collections.unmodifiableList(objectListeners);
  }

  @Override
  public void run() {
    try {
      if (Util.isMarketOpen() == false) {
        LOG.debug("Stock exchange is closed");
        return;
      }

      Wallet current = getCurrentQuotes();
      for (ChangeListener<Wallet> cl : listeners) {
        cl.stateChanged(current);
      }
      for (ChangeListener<Object> cl : objectListeners) {
        cl.stateChanged(current);
      }
    } catch (Exception e) {
      LOG.error("Error while retrieving quotes:", e);
    }
  }

  private Wallet getCurrentQuotes() throws IOException {
    LOG.info("Reading quotes from: " + Configuration.getInstance().getQuotesUrl());
    URL url = new URL(Configuration.getInstance().getQuotesUrl());
    String quotesPage = IOUtils.toString(url.openStream());
    LOG.info("Quotes page size: " + quotesPage.length() / 1024 + " KB");
    List<ShareInfo> result = new ArrayList<>();
    for (ShareInfo share : myWallet.getShareInfoList()) {
      result.add(getQuote(share, quotesPage.toString()));
    }
    return new Wallet(result);
  }

  private ShareInfo getQuote(ShareInfo share, String quotesPage) {
    int idx = quotesPage.indexOf(String.format(">%s<", share.getName()));
    try {
      if (idx > -1) {
        int tmpIdx = idx;
        for (int i = 0; i < X7; i++) {
          tmpIdx = quotesPage.indexOf(QUOTE_DELIM, tmpIdx + 1);
        }
        int quoteIdx = tmpIdx + QUOTE_DELIM.length();
        int quoteEndIdx = quotesPage.indexOf("<", quoteIdx);

        double quote = Double.parseDouble(quotesPage.substring(quoteIdx, quoteEndIdx).replace(",", "."));
        return ShareInfo.newInstanceFromSharesCount(share.getName(), quote, share.getCount());
      }
      LOG.warn(String.format("Share quotes [%s] not found - returning null", share.getName()));
      return ShareInfo.newErrorInstance(share, "Could not retrieve quotes");
    } catch (RuntimeException e) {
      LOG.warn(String.format("Share quotes [%s] not found - returning null", share.getName()));
      return ShareInfo.newErrorInstance(share, "Could not retrieve quotes");
    }
  }
}
