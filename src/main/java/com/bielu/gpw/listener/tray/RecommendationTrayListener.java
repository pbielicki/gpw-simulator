package com.bielu.gpw.listener.tray;

import java.util.List;

import com.bielu.gpw.GpwTray;
import com.bielu.gpw.domain.Recommendation;
import com.bielu.gpw.listener.ChangeListener;

public class RecommendationTrayListener implements ChangeListener<List<Recommendation>> {

  private final GpwTray gpwTray;

  public RecommendationTrayListener(GpwTray gpwTray) {
    this.gpwTray = gpwTray;
  }

  @Override
  public void stateChanged(List<Recommendation> source) {
    StringBuilder sb = new StringBuilder();
    for (Recommendation rec : source) {
      sb.append(rec.toString()).append("\n");
    }
    if (sb.length() > 0) {
      gpwTray.displayRecommendation(sb.toString());
    }
  }
}
