package com.bielu.gpw.listener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;

public class DatabaseWriterListener extends PropertiesConfig<Wallet> implements ChangeListener<Wallet> {

  private static final Log LOG = LogFactory.getLog(DatabaseWriterListener.class);
  private static final int COUNT = 4;
  private static final int QUOTE = 3;
  private static final int NAME = 2;
  private static final int ACTION_TIME = 1;
  private Connection conn;
  private String driverClass;
  private String jdbcUrl;
  private String username;
  private char[] password;

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password.toCharArray();
  }

  public DatabaseWriterListener() {
    if (initialized == false) {
      return;
    }

    try {
      Class.forName(driverClass);
      connect();
    } catch (Exception e) {
      LOG.error("Unable to initialize DB access. This listener will try to reconnect later "
          + "but will be inactive until DB is available.", e);
      conn = null;
    }
  }

  @Override
  protected void internalStateChanged(Wallet source) {
    try {
      if (conn == null || conn.isClosed()) {
        connect();
      }
    } catch (SQLException e) {
      LOG.error("Unable to connect to DB.", e);
      return;
    }

    try {
      PreparedStatement st = conn.prepareStatement("insert into ShareInfo values (?, ?, ?, ?)");
      for (ShareInfo share : source.getShareInfoList()) {
        st.setTimestamp(ACTION_TIME, new Timestamp(new Date().getTime()));
        st.setString(NAME, share.getName());
        st.setDouble(QUOTE, share.getQuote().doubleValue());
        st.setDouble(COUNT, share.getCount());
        st.addBatch();
      }
      st.executeBatch();
    } catch (SQLException e) {
      LOG.error("Unable to store data in the database.", e);
    }
  }

  @Override
  protected String getPropertiesFileName() {
    return "/jdbc.properties";
  }

  @Override
  protected boolean checkProperties() {
    if (empty(driverClass)) {
      log.error(String.format("Driver class is null - check [%s] file in the classpath", getPropertiesFileName()));
      return false;
    }
    if (empty(jdbcUrl)) {
      log.error(String.format("JDBC URL is null - check [%s] file in the classpath", getPropertiesFileName()));
      return false;
    }
    if (empty(username)) {
      log.error(String.format("JDBC URL is null - check [%s] file in the classpath", getPropertiesFileName()));
      return false;
    }
    if (password == null) {
      password = "".toCharArray();
    }
    return true;
  }

  private void connect() throws SQLException {
    conn = DriverManager.getConnection(jdbcUrl, username, String.valueOf(password));
    conn.setAutoCommit(true);
  }
}
