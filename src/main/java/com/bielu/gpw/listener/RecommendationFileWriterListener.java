package com.bielu.gpw.listener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import com.bielu.gpw.config.Configuration;
import com.bielu.gpw.domain.Recommendation;

public class RecommendationFileWriterListener implements ChangeListener<List<Recommendation>> {

    private FileOutputStream out;

    public RecommendationFileWriterListener() {
        try {
            out = new FileOutputStream(Configuration.getInstance().getRecommendationsFile());
        } catch (FileNotFoundException e) {
        }
    }

    @Override
    public void stateChanged(List<Recommendation> source) {
        if (out == null) {
            if (reOpenFile() == false) {
                return;
            }
        }
    }

    private boolean reOpenFile() {
        // TODO Auto-generated method stub
        return false;
    }

}
