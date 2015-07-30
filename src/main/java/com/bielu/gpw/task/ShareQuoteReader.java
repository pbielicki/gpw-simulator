package com.bielu.gpw.task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.config.Configuration;
import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;
import com.bielu.gpw.listener.ChangeListener;

public class ShareQuoteReader implements Runnable {

  private static final Log LOG = LogFactory.getLog(ShareQuoteReader.class);
  private static final int TWO = 2;
  private static final int FIVE = 5;
  private static final String SHARE_QUOTE_DELIM = "<td>";
  private static final String FUND_QUOTE_DELIM = "\";\"";

  private final Wallet myWallet;
  private final List<ChangeListener<Wallet>> listeners;
  private final List<ChangeListener<Object>> objectListeners;

  public ShareQuoteReader(Wallet myWallet, List<ChangeListener<Wallet>> listeners,
      List<ChangeListener<Object>> objectListeners) {

    this.myWallet = myWallet;
    this.listeners = Collections.unmodifiableList(listeners);
    this.objectListeners = Collections.unmodifiableList(objectListeners);
  }

  @Override
  public void run() {
    try {
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
    String sharesPage = null;
    String fundsPage = null;
    List<ShareInfo> result = new ArrayList<>();
    for (ShareInfo share : myWallet.getShareInfoList()) {
      switch (share.getShareType()) {
        case SHARE:
          if (sharesPage == null) {
            sharesPage = readUrl(Configuration.getInstance().getQuotesUrl());
          }
          result.add(getShareQuote(share, sharesPage));
          break;
        case FUND:
          if (fundsPage == null) {
            fundsPage = readUrl(Configuration.getInstance().getMbankFundsUrl());
          }
          result.add(getFundQuote(share, fundsPage));
      }
    }
    return new Wallet(result);
  }
  
  private String readUrl(String url) throws IOException {
    LOG.info("Reading: " + url);
    String content = IOUtils.toString(new URL(url).openStream());
    LOG.info("Content length: " + content.length() / 1024 + " KB");
    return content;
  }

  private ShareInfo getShareQuote(ShareInfo share, String quotesPage) {
    int idx = quotesPage.indexOf(String.format(">%s<", share.getName()));
    Optional<ShareInfo> shareInfo = Optional.empty();
    int tmpIdx = idx;
    try {
      if (idx > -1) {
        // get the reference quote (always present even if market is closed)
        for (int i = 0; i < TWO; i++) {
          tmpIdx = quotesPage.indexOf(SHARE_QUOTE_DELIM, tmpIdx + 1);
        }
        
        int quoteIdx = tmpIdx + SHARE_QUOTE_DELIM.length();
        int quoteEndIdx = quotesPage.indexOf("<", quoteIdx);
        double quote = Double.parseDouble(escapeHtml(quotesPage.substring(quoteIdx, quoteEndIdx)));
        shareInfo = Optional.of(share.newInstanceForQuote(quote));
      }
    } catch (RuntimeException e) {
      LOG.info(String.format("Share reference quotes [%s] not found", share.getName()));
    }
      
    try {
      if (idx > -1) {
        // get the current quote (present only if market is open)
        for (int i = 0; i < FIVE; i++) {
          tmpIdx = quotesPage.indexOf(SHARE_QUOTE_DELIM, tmpIdx + 1);
        }
        
        int quoteIdx = tmpIdx + SHARE_QUOTE_DELIM.length();
        int quoteEndIdx = quotesPage.indexOf("<", quoteIdx);
        double quote = Double.parseDouble(escapeHtml(quotesPage.substring(quoteIdx, quoteEndIdx)));
        return share.newInstanceForQuote(quote);
      }
    } catch (RuntimeException e) {
      return shareInfo.orElseGet(() -> {
        LOG.warn(String.format("Share quotes [%s] not found", share.getName()), e);
        return ShareInfo.newErrorInstance(share, "Could not retrieve share quotes"); 
      });
    }

    LOG.warn(String.format("Share quotes [%s] not found", share.getName()));
    return shareInfo.orElse(ShareInfo.newErrorInstance(share, "Could not retrieve share quotes"));
  }

  private ShareInfo getFundQuote(ShareInfo share, String quotesPage) {
    int idx = quotesPage.indexOf(String.format("\"%s", share.getName()));
    Optional<ShareInfo> shareInfo = Optional.empty();
    int tmpIdx = idx;
    try {
      if (idx > -1) {
        tmpIdx = quotesPage.indexOf(FUND_QUOTE_DELIM, tmpIdx + 1);
        
        int quoteIdx = tmpIdx + FUND_QUOTE_DELIM.length();
        int quoteEndIdx = quotesPage.indexOf("\";", quoteIdx);
        double quote = Double.parseDouble(escapeHtml(quotesPage.substring(quoteIdx, quoteEndIdx)));
        return share.newInstanceForQuote(quote);
      }
    } catch (RuntimeException e) {
      return shareInfo.orElseGet(() -> {
        LOG.warn(String.format("Fund quotes [%s] not found", share.getName()), e);
        return ShareInfo.newErrorInstance(share, "Could not retrieve fund quotes"); 
      });
    }

    LOG.warn(String.format("Fund quotes [%s] not found", share.getName()));
    return shareInfo.orElse(ShareInfo.newErrorInstance(share, "Could not retrieve fund quotes"));
  }
  
  private String escapeHtml(String html) {
    return html.replace(",", ".")
               .replace("&nbsp;", "");
  }
}
