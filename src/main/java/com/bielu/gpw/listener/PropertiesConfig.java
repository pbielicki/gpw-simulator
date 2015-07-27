package com.bielu.gpw.listener;

import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PropertiesConfig<T> implements ChangeListener<T> {

  protected final Log log = LogFactory.getLog(getClass());
  protected final boolean initialized;

  protected PropertiesConfig() {
    Properties props = new Properties();
    boolean init = true;
    try {
      props.load(getClass().getResourceAsStream(getPropertiesFileName()));
      for (Entry<Object, Object> e : props.entrySet()) {
        PropertyUtils.setProperty(this, e.getKey().toString(), e.getValue());
      }
    } catch (Exception e) {
      log.error(String.format("Error while reading properties file [%s]", getPropertiesFileName()), e);
      init = false;
    }

    if (init == true) {
      init = checkProperties();
    }
    initialized = init;
  }

  public final void stateChanged(T source) {
    if (initialized == false) {
      log.warn("Object is not initialized correctly - processing is ignored.");
    } else {
      internalStateChanged(source);
    }
  };

  protected boolean empty(String string) {
    return string == null || string.trim().length() == 0;
  }

  protected abstract void internalStateChanged(T source);

  protected abstract boolean checkProperties();

  protected abstract String getPropertiesFileName();
}
