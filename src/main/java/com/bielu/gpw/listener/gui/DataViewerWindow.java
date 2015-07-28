package com.bielu.gpw.listener.gui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.Util;
import com.bielu.gpw.config.Configuration;
import com.bielu.gpw.config.GuiPluginInfo;
import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;
import com.bielu.gpw.listener.gui.plugin.GuiPlugin;

public class DataViewerWindow extends JFrame {

  private static final long serialVersionUID = 1L;
  private static final Log LOG = LogFactory.getLog(DataViewerWindow.class);
  private static final int START_HEIGHT = 600;
  private static final int START_WIDTH = 800;
  private static final int MAIN_DIVIDER_LOCATION = 300;
  private static final int X10 = 10;
  private static final int X5 = 5;
  private static final String WINDOW_TITLE = "GPW Monitor Viewer";

  private final JPanel mainPanel;
  private final JTable sharesTable;
  private final Wallet myWallet;
  private final WalletTableModel walletTableModel;

  private volatile Optional<Wallet> currentWallet = Optional.empty();
  private volatile Optional<Wallet> checkPoint = Optional.empty();

  public DataViewerWindow(Wallet myWallet) {
    this.myWallet = myWallet;
    walletTableModel = new WalletTableModel();
    sharesTable = new JTable(walletTableModel);

    this.setMinimumSize(new Dimension(START_WIDTH, START_HEIGHT));
    this.setSize(START_WIDTH, START_HEIGHT);
    this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/web.png")));
    mainPanel = new JPanel();
    setCenterLocation();
    this.setContentPane(getMainPanel());
    this.setTitle(WINDOW_TITLE);
  }

  protected synchronized void setCurrentWallet(Wallet wallet) {
    currentWallet = Optional.ofNullable(wallet);
  }

  protected synchronized void setCheckPoint(Wallet wallet) {
    checkPoint = Optional.ofNullable(wallet);
  }
  
  public Wallet getCheckPoint() {
    return checkPoint.orElse(null);
  }

  protected void refresh() {
    walletTableModel.fireTableStructureChanged();
  }

  private void setCenterLocation() {
    int x, y;
    if (getOwner() != null) {
      x = getOwner().getLocation().x + getOwner().getSize().width / 2;
      y = getOwner().getLocation().y + getOwner().getSize().height / 2;
    } else {
      Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

      x = point.x;
      y = point.y;
    }
    x -= getSize().width / 2;
    y -= getSize().height / 2;

    this.setLocation(new Point(x, y));
  }

  private JPanel getMainPanel() {
    mainPanel.setLayout(new GridBagLayout());
    // Contractors table & filter tree
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = X10;
    constraints.insets = new Insets(X5, X5, X5, 0);
    constraints.gridx = 0;
    mainPanel.add(getDataSplitPane(), constraints);
    return mainPanel;
  }

  private JComponent getDataSplitPane() {
    JTabbedPane tabbedPane = new JTabbedPane();

    JSplitPane dataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    dataSplitPane.setTopComponent(new JScrollPane(sharesTable));
    dataSplitPane.setBottomComponent(new JScrollPane());
    dataSplitPane.setDividerLocation(MAIN_DIVIDER_LOCATION);
    tabbedPane.addTab("Tabular data", dataSplitPane);

    addPlugins(tabbedPane);

    return tabbedPane;
  }

  private void addPlugins(JTabbedPane tabbedPane) {
    for (GuiPluginInfo info : Configuration.getInstance().getGuiPluginList()) {
      try {
        Class<?> clazz = Class.forName(info.getClassName());
        JComponent component = GuiPlugin.class.cast(clazz.newInstance()).getComponent();
        if (component != null) {
          tabbedPane.addTab(info.getLabel(), component);
        }
      } catch (Exception e) {
        LOG.error(String.format("Could not create plugin [%s]", info.getClassName()), e);
      }
    }
  }

  private class WalletTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final int COMPANY_SYMBOL = 0;
    private static final int INVESTMENT_DATE = 1;
    private static final int SHARES_COUNT = 2;
    private static final int START_QUOTE = 3;
    private static final int START_VALUE = 4;
    private static final int CURRENT_QUOTE = 5;
    private static final int CURRENT_VALUE = 6;
    private static final int CURRENT_PROFIT = 7;
    private static final int YEARLY_RATE = 8;

    private static final int COLUMNS_COUNT = 5;
    private static final int COLUMNS_COUNT_WITH_CURRENT = COLUMNS_COUNT + 4;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case COMPANY_SYMBOL:
          return "Company Symbol";
        case INVESTMENT_DATE:
          return "Inv. Date";
        case SHARES_COUNT:
          return "Shares Count";
        case START_QUOTE:
          return "Start Quote";
        case START_VALUE:
          return "Start Value";
        case CURRENT_QUOTE:
          return "Current Quote";
        case CURRENT_VALUE:
          return "Current Value";
        case CURRENT_PROFIT:
          return "Current Profit/Loss";
        case YEARLY_RATE:
          return "Yearly Rate";
        default:
          return "";
      }
    }

    @Override
    public int getColumnCount() {
      if (currentWallet == null) {
        return COLUMNS_COUNT;
      } else {
        return COLUMNS_COUNT_WITH_CURRENT;
      }
    }

    @Override
    public int getRowCount() {
      return myWallet.size() + 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < myWallet.size()) {
        return getShareInfo(rowIndex, columnIndex);
      }

      if (rowIndex == myWallet.size()) {
        return "-----";
      }

      if (rowIndex == myWallet.size() + 1) {
        return getWalletInfo(columnIndex);
      }

      if (rowIndex == myWallet.size() + 2) {
        return getNetWalletInfo(columnIndex);
      }
      
      return null;
    }

    private String getShareInfo(int rowIndex, int columnIndex) {
      ShareInfo share = myWallet.getShareInfo(rowIndex);
      switch (columnIndex) {
        case COMPANY_SYMBOL:
          return share.getName();
        case INVESTMENT_DATE:
          return format.format(share.getStartDate());
        case SHARES_COUNT:
          return String.format("%d", share.getCount());
        case START_QUOTE:
          return String.format("%.2f", share.getQuote());
        case START_VALUE:
          return String.format("%.2f", share.getValue());
        default:
      }

      return currentWallet.map((wallet) -> {
        ShareInfo currentShare = wallet.getShareInfo(rowIndex);
        switch (columnIndex) {
          case CURRENT_QUOTE:
            return String.format(Util.FORMAT_MONEY, currentShare.getQuote());
          case CURRENT_VALUE:
            return String.format(Util.FORMAT_MONEY, currentShare.getValue());
          case CURRENT_PROFIT:
            return String.format("%s (%s)", Util.diff(share, currentShare), Util.percentageSigned(share, currentShare));
          case YEARLY_RATE:
            return String.format("%s", Util.yearlyRate(share, currentShare));
          default:
            return "";
        }
      }).orElse("");
    }

    private String getWalletInfo(int columnIndex) {
      switch (columnIndex) {
        case START_VALUE:
          return "Sum:";
        case CURRENT_QUOTE:
          return String.format(Util.FORMAT_MONEY, myWallet.getValue());
        default:
      }

      return currentWallet.map((wallet) -> {
        switch (columnIndex) {
          case CURRENT_VALUE:
            return String.format(Util.FORMAT_MONEY, wallet.getValue());
          case CURRENT_PROFIT:
            return String.format("%s (%s)", Util.diff(myWallet, wallet),
                Util.percentageSigned(myWallet, wallet));
          default:
            return "";
        }
      }).orElse("");
    }

    private String getNetWalletInfo(int columnIndex) {
      switch (columnIndex) {
        case CURRENT_VALUE:
          return "Net Profit/Loss:";
        default:
      }

      return currentWallet.map((wallet) -> {
        switch (columnIndex) {
          case CURRENT_PROFIT:
            return String.format("%s", Util.diff(myWallet, wallet, Util.NET_PROFIT_RATE));
          default:
            return "";
        }
      }).orElse("");
    }
  }
}
