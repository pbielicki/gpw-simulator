package com.bielu.gpw.task;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.bielu.gpw.domain.Recommendation;
import com.bielu.gpw.domain.Wallet;

public class OnetPlReader implements Callable<List<Recommendation>> {

  private static final String START = "<table id=\"recommendTable\">";
  private static final String END = "</table>";
  private static final int MAX_LOOP_COUNT = 20;
  private static final int X3 = 3;

  private final Wallet myWallet;

  public OnetPlReader(Wallet myWallet) {
    this.myWallet = myWallet;
  }

  @Override
  public List<Recommendation> call() throws Exception {
    URL url = new URL("http://gielda.onet.pl/18888,rekomendacje");
    LineNumberReader in = new LineNumberReader(new InputStreamReader(url.openStream()));
    StringBuilder webPage = new StringBuilder();
    String line = null;
    boolean process = false;
    while ((line = in.readLine()) != null) {
      if (START.equals(line)) {
        process = true;
      }
      if (END.equals(line)) {
        break;
      }
      if (process) {
        webPage.append(line);
      }
    }
    in.close();

    return getRecommendations(webPage.toString());
  }

  private List<Recommendation> getRecommendations(String webPage) {
    List<Recommendation> result = new ArrayList<>();
    int idx = webPage.indexOf("<tbody>");
    int loopCount = 0;

    main: while (idx != -1 && loopCount < MAX_LOOP_COUNT) {
      idx = webPage.indexOf("<a title=", idx + 1);
      if (idx != -1) {
        idx = webPage.indexOf(">", idx);
        String name = webPage.substring(idx + 1, webPage.indexOf("<", idx + 1)).trim();

        for (int i = 0; i < X3; i++) {
          idx = webPage.indexOf("<td>", idx + 1);
          if (idx == -1) {
            break main;
          }
        }
        String recText = webPage.substring(idx + "<td>".length(), webPage.indexOf("</td>", idx)).trim();
        idx = webPage.indexOf("<td>", idx + 1);
        if (idx == -1) {
          break;
        }
        String price = webPage.substring(idx + "<td>".length(), webPage.indexOf("</td>", idx)).trim();

        idx = webPage.indexOf("</tr>", idx);

        if (myWallet.contains(name) || "Kupuj".equals(recText) || "Akumuluj".equals(recText)) {
          result.add(new Recommendation(name, recText, price.replace(",", ".")));
        }

        loopCount++;
      }
    }
    return result;
  }
}
