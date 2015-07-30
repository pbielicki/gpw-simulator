package com.bielu.gpw.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.GpwThreadFactory;
import com.bielu.gpw.domain.Recommendation;
import com.bielu.gpw.domain.Wallet;
import com.bielu.gpw.listener.ChangeListener;

public class RecommendationReader implements Runnable {

  private static final Log LOG = LogFactory.getLog(RecommendationReader.class);
  private static final ThreadFactory THREAD_FACTORY = new GpwThreadFactory("RecommendationReader");

  private final List<ChangeListener<List<Recommendation>>> listeners;
  private final List<ChangeListener<Object>> objectListeners;
  private final List<Callable<List<Recommendation>>> readers;

  @SuppressWarnings("serial")
  public RecommendationReader(final Wallet myWallet, List<ChangeListener<List<Recommendation>>> listeners,
      List<ChangeListener<Object>> objectListeners) {

    this.listeners = Collections.unmodifiableList(listeners);
    this.objectListeners = Collections.unmodifiableList(objectListeners);
    this.readers = Collections.unmodifiableList(new ArrayList<Callable<List<Recommendation>>>() {
      {
        add(new OnetPlReader(myWallet));
        add(new MoneyPlReader(myWallet));
      }
    });
  }

  @Override
  public void run() {
    try {
      List<Recommendation> current = getCurrentRecommendations();
      for (ChangeListener<List<Recommendation>> cl : listeners) {
        cl.stateChanged(current);
      }
      for (ChangeListener<Object> cl : objectListeners) {
        cl.stateChanged(current);
      }
    } catch (Exception e) {
      LOG.error("Error while retrieving recommendations.", e);
    }
  }

  private List<Recommendation> getCurrentRecommendations() throws IOException {
    final List<Future<List<Recommendation>>> futures = new ArrayList<>(readers.size());
    final ExecutorService service = Executors.newFixedThreadPool(2, THREAD_FACTORY);
    final List<Recommendation> result = new ArrayList<>();

    try {
      for (Callable<List<Recommendation>> reader : readers) {
        futures.add(service.submit(reader));
      }

      for (Future<List<Recommendation>> future : futures) {
        result.addAll(future.get());
      }
    } catch (Exception e) {
      LOG.error("Could not retrieve recommendations", e);
    } finally {
      service.shutdown();
    }

    return result;
  }

}
