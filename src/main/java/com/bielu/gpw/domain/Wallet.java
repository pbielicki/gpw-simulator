package com.bielu.gpw.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Wallet implements Comparable<Wallet> {

  private final List<ShareInfo> shareInfoList;
  private final Set<String> shareNameSet;
  private final BigDecimal walletValue;

  public Wallet(List<ShareInfo> shareInfoList) {
    this.shareInfoList = Collections.unmodifiableList(shareInfoList);

    Set<String> set = new HashSet<String>();
    for (ShareInfo share : shareInfoList) {
      if (share != null) {
        set.add(share.getName());
      }
    }
    this.shareNameSet = Collections.unmodifiableSet(set);

    BigDecimal value = BigDecimal.ZERO;
    for (ShareInfo share : shareInfoList) {
      value = value.add(share.getValue(), MathContext.DECIMAL32);
    }
    this.walletValue = value;
  }

  public int size() {
    return shareInfoList.size();
  }

  public ShareInfo getShareInfo(int index) {
    return shareInfoList.get(index);
  }

  public List<ShareInfo> getShareInfoList() {
    return shareInfoList;
  }

  public BigDecimal getWalletValue() {
    return walletValue;
  }

  public boolean contains(String shareName) {
    return shareNameSet.contains(shareName);
  }

  public boolean contains(ShareInfo share) {
    return shareInfoList.contains(share);
  }

  @Override
  public int compareTo(Wallet o) {
    return this.walletValue.compareTo(o.walletValue);
  }

  @Override
  public String toString() {
    return String.format("Wallet contains %d shares of value %.2f", shareInfoList.size(), walletValue);
  }
}
