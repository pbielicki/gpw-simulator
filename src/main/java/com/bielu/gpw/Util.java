package com.bielu.gpw;

import java.math.BigDecimal;

import static java.math.MathContext.DECIMAL128;

import java.util.Calendar;
import java.util.Date;

import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;

public final class Util {
  public static final BigDecimal THRESHOLD_PERCENT = BigDecimal.ONE;
  public static final int OPEN_HOUR = 8;
  public static final int CLOSE_HOUR = 18;
  public static final BigDecimal NET_PROFIT_RATE = BigDecimal.valueOf(0.81D); // 19% of tax
  public static final String FORMAT_PERCENT = "%.1f%%";
  public static final String FORMAT_MONEY = "%.2f";

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal DAYS_IN_A_YEAR = BigDecimal.valueOf(365);
  private static final long MILLIS_IN_A_DAY = 1000L * 60L * 60L * 24L;

  public static BigDecimal percentage(BigDecimal originalValue, BigDecimal changedValue) {
    return changedValue.divide(originalValue, DECIMAL128).multiply(HUNDRED, DECIMAL128).subtract(HUNDRED, DECIMAL128).abs();
  }

  public static String percentageSigned(BigDecimal originalValue, BigDecimal changedValue) {
    BigDecimal value = percentage(originalValue, changedValue);
    String sign = "";
    if (originalValue.compareTo(changedValue) > 0) {
      sign = "-";
    } else if (originalValue.compareTo(changedValue) < 0) {
      sign = "+";
    }
    return String.format("%s" + FORMAT_PERCENT, sign, value);
  }

  public static BigDecimal percentage(Wallet originalValue, Wallet changedValue) {
    return percentage(originalValue.getWalletValue(), changedValue.getWalletValue());
  }

  public static BigDecimal percentage(ShareInfo originalValue, ShareInfo changedValue) {
    return percentage(originalValue.getValue(), changedValue.getValue());
  }

  public static String percentageSigned(Wallet originalValue, Wallet changedValue) {
    return percentageSigned(originalValue.getWalletValue(), changedValue.getWalletValue());
  }

  public static String percentageSigned(ShareInfo originalValue, ShareInfo changedValue) {
    return percentageSigned(originalValue.getValue(), changedValue.getValue());
  }

  public static String diff(Wallet originalValue, Wallet changedValue, BigDecimal tax) {
    return diff(originalValue.getWalletValue(), changedValue.getWalletValue(), tax);
  }

  public static String diff(Wallet originalValue, Wallet changedValue) {
    return diff(originalValue.getWalletValue(), changedValue.getWalletValue(), BigDecimal.ONE);
  }

  public static String diff(ShareInfo originalValue, ShareInfo changedValue) {
    return diff(originalValue.getValue(), changedValue.getValue(), BigDecimal.ONE);
  }

  public static String diff(BigDecimal originalValue, BigDecimal changedValue, BigDecimal tax) {
    BigDecimal diff = changedValue.subtract(originalValue);
    String sign = "";
    if (diff.compareTo(BigDecimal.ZERO) < 0) {
      sign = "-";
    } else if (diff.compareTo(BigDecimal.ZERO) > 0) {
      diff = diff.multiply(tax);
      sign = "+";
    }

    return String.format("%s" + FORMAT_MONEY, sign, diff.abs());
  }

  public static String yearlyRate(ShareInfo original, ShareInfo current) {
    BigDecimal rate = percentage(original, current);

    long period = new Date().getTime() - original.getStartDate().getTime();
    BigDecimal days = BigDecimal.valueOf((period / MILLIS_IN_A_DAY) + 1);
    if (days.compareTo(DAYS_IN_A_YEAR) < 0) {
      days = DAYS_IN_A_YEAR;
    }

    BigDecimal yearlyRate = rate.multiply(DAYS_IN_A_YEAR, DECIMAL128).divide(days, DECIMAL128);
    return String.format(FORMAT_PERCENT, yearlyRate);
  }

  public static boolean isMarketOpen() {
    Calendar now = Calendar.getInstance();
    // stock exchange is not working between 18 - 9 and on weekends
    if (now.get(Calendar.HOUR_OF_DAY) >= CLOSE_HOUR 
     || now.get(Calendar.HOUR_OF_DAY) <= OPEN_HOUR
     || now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY 
     || now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

      return false;
    }
    return true;
  }

  public static boolean isThresholdReached(Wallet current, Wallet checkPoint) {
    boolean notify = false;
    for (int i = 0; i < checkPoint.size(); i++) {
      if (current.getShareInfo(i).getValue().intValue() == 0) {
        notify = true;
        continue;
      }
      BigDecimal diff = Util.percentage(checkPoint.getShareInfo(i), current.getShareInfo(i));
      if (diff.compareTo(THRESHOLD_PERCENT) > 0) {
        notify = true;
      }
    }

    BigDecimal diff = Util.percentage(checkPoint, current);
    if (diff.compareTo(THRESHOLD_PERCENT) > 0) {
      notify = true;
    }

    return notify;
  }

  private Util() {
  }
}
