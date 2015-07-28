package com.bielu.gpw.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bielu.gpw.Util;

public class Wallet implements Comparable<Wallet>, Investment {

  private final List<ShareInfo> shareInfoList;
  private final Set<String> shareNameSet;
  private final BigDecimal value;
  private final Date startDate;

  public Wallet(List<ShareInfo> shareInfoList) {
    this.shareInfoList = Collections.unmodifiableList(shareInfoList);

    Date startDate = new Date(); 
    Set<String> set = new HashSet<>();
    for (ShareInfo share : shareInfoList) {
      if (share != null) {
        set.add(share.getName());
        if (share.startDate().before(startDate)) {
          startDate = share.startDate();
        }
      }
    }
    this.shareNameSet = Collections.unmodifiableSet(set);
    this.startDate = startDate;

    BigDecimal value = BigDecimal.ZERO;
    for (ShareInfo share : shareInfoList) {
      value = value.add(share.value(), MathContext.DECIMAL32);
    }
    this.value = value;
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

  @Override
  public BigDecimal value() {
    return value;
  }
  
  @Override
  public Date startDate() {
    return startDate;
  }

  public boolean contains(String shareName) {
    return shareNameSet.contains(shareName);
  }

  public boolean contains(ShareInfo share) {
    return shareInfoList.contains(share);
  }

  @Override
  public int compareTo(Wallet o) {
    return this.value.compareTo(o.value);
  }

  @Override
  public String toString() {
    return String.format("Wallet contains %d shares of value " + Util.FORMAT_MONEY, shareInfoList.size(), value);
  }
}
