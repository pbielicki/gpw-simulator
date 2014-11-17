package com.bielu.gpw;

import java.math.BigDecimal;
import static java.math.MathContext.DECIMAL128;
import java.util.Calendar;
import java.util.Date;

import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;

public final class Util {

    private static final BigDecimal THRESHOLD = BigDecimal.ONE;
    private static final int OPEN_HOUR = 8;
    private static final int CLOSE_HOUR = 18;
    private static final BigDecimal X_100 = BigDecimal.valueOf(100);
    private static final BigDecimal X_365 = BigDecimal.valueOf(365);

    public static BigDecimal percentage(BigDecimal originalValue, BigDecimal changedValue) {
        return changedValue.divide(originalValue, DECIMAL128).multiply(X_100, DECIMAL128).subtract(X_100, DECIMAL128).abs();
    }

    public static String percentagePlus(BigDecimal originalValue, BigDecimal changedValue) {
        BigDecimal value = percentage(originalValue, changedValue);
        String sign = "";
        if (originalValue.compareTo(changedValue) > 0) {
            sign = "-";
        } else if (originalValue.compareTo(changedValue) < 0) {
            sign = "+";
        }
        return String.format("%s%.3f%%", sign, value);
    }

    public static BigDecimal percentage(Wallet originalValue, Wallet changedValue) {
        return percentage(originalValue.getWalletValue(), changedValue.getWalletValue());
    }

    public static BigDecimal percentage(ShareInfo originalValue, ShareInfo changedValue) {
        return percentage(originalValue.getValue(), changedValue.getValue());
    }

    public static String percentagePlus(Wallet originalValue, Wallet changedValue) {
        return percentagePlus(originalValue.getWalletValue(), changedValue.getWalletValue());
    }

    public static String percentagePlus(ShareInfo originalValue, ShareInfo changedValue) {
        return percentagePlus(originalValue.getValue(), changedValue.getValue());
    }

    public static String diff(Wallet originalValue, Wallet changedValue) {
        return diff(originalValue.getWalletValue(), changedValue.getWalletValue());
    }

    public static String diff(ShareInfo originalValue, ShareInfo changedValue) {
        return diff(originalValue.getValue(), changedValue.getValue());
    }

    public static String diff(BigDecimal originalValue, BigDecimal changedValue) {
        BigDecimal diff = changedValue.subtract(originalValue);
        String sign = "";
        if (diff.compareTo(BigDecimal.ZERO) < 0) {
            sign = "-";
        } else if (diff.compareTo(BigDecimal.ZERO) > 0) {
            sign = "+";
        }

        return String.format("%s%s", sign, diff.abs());
    }

    public static String yearlyRate(ShareInfo original, ShareInfo current) {
        BigDecimal rate = percentage(original, current);

        long period = new Date().getTime() - original.getStartDate().getTime();
        BigDecimal days = BigDecimal.valueOf((period / (1000L * 60L * 60L * 24L)) + 1);

        BigDecimal yearlyRate = rate.multiply(X_365, DECIMAL128).divide(days, DECIMAL128);
        return String.format("%.3f%%", yearlyRate);
    }


    public static boolean isMarketOpen() {
        Calendar now = Calendar.getInstance();
        // stock exchange is not working between 18 - 9 and on weekends
        if (now.get(Calendar.HOUR_OF_DAY) >= CLOSE_HOUR || now.get(Calendar.HOUR_OF_DAY) <= OPEN_HOUR
                || now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

            return false;
        }
        return true;
    }

    public static boolean checkPoint(Wallet current, Wallet checkPoint) {
        boolean notify = false;
        for (int i = 0; i < checkPoint.size(); i++) {
            if (current.getShareInfo(i).getValue().intValue() == 0) {
                notify = true;
                continue;
            }
            BigDecimal diff = Util.percentage(checkPoint.getShareInfo(i), current.getShareInfo(i));
            if (diff.compareTo(THRESHOLD) > 0) {
                notify = true;
            }
        }

        BigDecimal diff = Util.percentage(checkPoint, current);
        if (diff.compareTo(THRESHOLD) > 0) {
            notify = true;
        }

        return notify;
    }

    private Util() {
    }
}
