package com.bielu.gpw.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import com.bielu.gpw.Util;

public class ShareInfo implements Comparable<ShareInfo>, Investment {

  private final String name;
  private final BigDecimal quote;
  private final BigDecimal value;
  private final BigDecimal sharesCount;
  private final String error;
  private final Date startDate;
  private final ShareTypeEnum shareType;

  public static ShareInfo newErrorInstance(ShareInfo original, String error) {
    ShareBuilder builder = new ShareBuilder();
    builder.name = original.name;
    builder.quote = original.quote.doubleValue();
    builder.sharesCount = original.sharesCount.doubleValue();
    builder.value = original.value.doubleValue();
    builder.startDate = original.startDate;
    builder.error = error;
    builder.shareType = original.shareType;
    return new ShareInfo(builder);
  }

  public ShareInfo newInstanceForQuote(double quote) {
    ShareBuilder builder = new ShareBuilder();
    builder.name = name;
    builder.quote = quote;
    builder.sharesCount = sharesCount.doubleValue();
    builder.value = -1;
    builder.startDate = startDate;
    builder.shareType = shareType;
    return new ShareInfo(builder);
  }

  public static ShareInfo merge(ShareInfo one, ShareInfo two) {
    if (one.equals(two) == false) {
      throw new IllegalArgumentException("Share information are not equal");
    }
    ShareBuilder builder = new ShareBuilder();
    builder.name = one.getName();
    builder.quote = one.getQuote().doubleValue();
    builder.sharesCount = -1;
    builder.value = one.value.add(two.value).doubleValue();
    builder.startDate = one.startDate;
    return new ShareInfo(builder);
  }

  private ShareInfo(ShareBuilder builder) {
    if (builder.value < 0 && builder.sharesCount < 0) {
      throw new IllegalArgumentException("Value and Shares Count cannot be both below zero");
    }

    error = builder.error;
    name = builder.name;
    quote = BigDecimal.valueOf(builder.quote);
    startDate = builder.startDate;
    shareType = builder.shareType;
    if (builder.value > 0) {
      value = BigDecimal.valueOf(builder.value);
      sharesCount = value.divide(quote, MathContext.DECIMAL128);
    } else {
      sharesCount = BigDecimal.valueOf(builder.sharesCount);
      value = quote.multiply(BigDecimal.valueOf(builder.sharesCount), MathContext.DECIMAL128);
    }
  }

  public String getName() {
    return name;
  }

  public BigDecimal getQuote() {
    return quote;
  }

  @Override
  public BigDecimal value() {
    return value;
  }

  public BigDecimal getCount() {
    return sharesCount;
  }

  public String getError() {
    return error;
  }
  
  public ShareTypeEnum getShareType() {
    return shareType;
  }

  @Override
  public Date startDate() {
    return (Date) startDate.clone();
  }

  public boolean hasError() {
    return error != null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((quote == null) ? 0 : quote.hashCode());
    result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ShareInfo)) {
      return false;
    }
    ShareInfo other = (ShareInfo) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (quote == null) {
      if (other.quote != null) {
        return false;
      }
    } else if (!quote.equals(other.quote)) {
      return false;
    }
    if (startDate == null) {
      if (other.startDate != null) {
        return false;
      }
    } else if (!startDate.equals(other.startDate)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(ShareInfo o) {
    return value.compareTo(o.value);
  }

  @Override
  public String toString() {
    return String.format("<ShareInfo '%s' (" + Util.FORMAT_MONEY + ") - " + Util.FORMAT_MONEY
        + " shares of total value " + Util.FORMAT_MONEY + ">", name, quote, sharesCount, value);
  }

  public static final class ShareBuilder {
    public String name;
    public double quote;
    public double value;
    public double sharesCount;
    public String error;
    public Date startDate;
    public ShareTypeEnum shareType = ShareTypeEnum.SHARE;
    
    public ShareInfo buildShareInfo() {
      return new ShareInfo(this);
    }
  }
}
