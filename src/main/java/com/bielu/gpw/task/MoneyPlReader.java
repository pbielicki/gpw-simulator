package com.bielu.gpw.task;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.bielu.gpw.domain.Recommendation;
import com.bielu.gpw.domain.Wallet;

public class MoneyPlReader implements Callable<List<Recommendation>> {

  private static final String TD_BEGIN = "<td class=\"ar\">";
  private static final String RECCOMENDATION_START = "<a class=\"link\" href=\"http://www.money.pl/gielda/spolki-gpw/";
  private static final String START = "<div class=\"hd inbox\">Rekomendacje</div>";
  private static final String END = "Następna strona";
  private static final int X3 = 3;
  private static final int MAX_LOOP_COUNT = 20;

  private final Wallet myWallet;

  public MoneyPlReader(Wallet myWallet) {
    this.myWallet = myWallet;
  }

  @Override
  public List<Recommendation> call() throws Exception {
    URL url = new URL("http://www.money.pl/gielda/rekomendacje/");
    LineNumberReader in = new LineNumberReader(new InputStreamReader(url.openStream()));
    StringBuilder webPage = new StringBuilder();
    String line = null;
    boolean process = false;
    while ((line = in.readLine()) != null) {
      if (START.equals(line.trim())) {
        process = true;
      }
      if (END.equals(line.trim())) {
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
    int idx = 0;
    int loopCount = 0;

    main: while (idx != -1 && loopCount < MAX_LOOP_COUNT) {
      idx = webPage.indexOf(RECCOMENDATION_START, idx + 1);
      if (idx == -1) {
        break;
      }
      idx = webPage.indexOf(">", idx) + 1;
      String name = webPage.substring(idx, webPage.indexOf("<", idx + 1)).trim();

      for (int i = 0; i < X3; i++) {
        idx = webPage.indexOf(TD_BEGIN, idx + 1);
        if (idx == -1) {
          break main;
        }
      }
      String price = webPage.substring(idx + TD_BEGIN.length(), webPage.indexOf("</td>", idx)).trim();
      idx = webPage.indexOf(TD_BEGIN, idx + 1);
      if (idx == -1) {
        break;
      }
      idx = webPage.indexOf(">", idx + TD_BEGIN.length()) + 1;
      String recText = webPage.substring(idx, webPage.indexOf("<", idx)).trim();

      idx = webPage.indexOf("</tr>", idx);

      if (myWallet.contains(name) || "Kupuj".equals(recText) || "Akumuluj".equals(recText)) {
        result.add(new Recommendation(name, recText, price.replace(",", ".")));
      }

      loopCount++;
    }
    return result;
  }
}
