package com.bielu.gpw.listener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.config.Configuration;
import com.bielu.gpw.domain.Recommendation;

public class RecommendationFileWriterListener implements ChangeListener<List<Recommendation>> {

  private static final Log LOG = LogFactory.getLog(RecommendationFileWriterListener.class);
  private FileOutputStream out;

  public RecommendationFileWriterListener() {
    try {
      out = new FileOutputStream(Configuration.getInstance().getRecommendationsFile(), true);
    } catch (FileNotFoundException e) {
    }
  }

  @Override
  public void stateChanged(List<Recommendation> source) {
    if (out == null) {
      if (reopenFile() == false) {
        return;
      }
    }

    try {
      out.write(new SimpleDateFormat("yyyy-MM-dd HH:ss").format(new Date()).getBytes());
      out.write(System.lineSeparator().getBytes());
      for (Recommendation rec : source) {
        out.write(rec.toString().getBytes());
        out.write(System.lineSeparator().getBytes());
      }
      out.flush();
    } catch (IOException e) {
      LOG.warn("Could not write recommendations to file", e);
    }
  }

  private boolean reopenFile() {
    // TODO Auto-generated method stub
    return false;
  }

}
