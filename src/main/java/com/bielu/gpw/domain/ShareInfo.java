package com.bielu.gpw.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

public final class ShareInfo implements Comparable<ShareInfo> {

    private final String name;
    private final BigDecimal quote;
    private final BigDecimal value;
    private final int sharesCount;
    private final String error;
    private final Date startDate;

    public static ShareInfo newErrorInstance(ShareInfo original, String error) {
        ShareBuilder builder = new ShareBuilder();
        builder.name = original.name;
        builder.quote = original.quote.doubleValue();
        builder.sharesCount = original.sharesCount;
        builder.value = original.value.doubleValue();
        builder.startDate = original.startDate;
        builder.error = error;
        return new ShareInfo(builder);
    }

    public static ShareInfo newInstanceFromValue(String name, double quote, double value) {
        ShareBuilder builder = new ShareBuilder();
        builder.name = name;
        builder.quote = quote;
        builder.sharesCount = -1;
        builder.value = value;
        builder.startDate = new Date(0);
        return new ShareInfo(builder);
    }

    public static ShareInfo newInstanceFromSharesCount(String name, double quote, int sharesCount) {
        return newInstanceFromSharesCount(name, quote, sharesCount, new Date());
    }

    public static ShareInfo newInstanceFromSharesCount(String name, double quote, int sharesCount, Date startDate) {
        ShareBuilder builder = new ShareBuilder();
        builder.name = name;
        builder.quote = quote;
        builder.sharesCount = sharesCount;
        builder.value = -1;
        builder.startDate = startDate;
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
        builder.value = one.getValue().add(two.getValue()).doubleValue();
        builder.startDate = one.getStartDate();
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
        if (builder.value > 0) {
            value = BigDecimal.valueOf(builder.value);
            sharesCount = value.divide(quote, MathContext.DECIMAL128).intValue();
        } else {
            sharesCount = builder.sharesCount;
            value = quote.multiply(BigDecimal.valueOf(builder.sharesCount), MathContext.DECIMAL128);
        }
    }

    public ShareInfo updateQuote(double newQuote, Date startDate) {
        return newInstanceFromSharesCount(name, newQuote, sharesCount, startDate);
    }

    public ShareInfo updateCount(int newCount, Date startDate) {
        return newInstanceFromSharesCount(name, quote.doubleValue(), newCount, startDate);
    }

    public String getName() {
        return name;
    }

    public BigDecimal getQuote() {
        return quote;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getCount() {
        return sharesCount;
    }

    public String getError() {
        return error;
    }

    public Date getStartDate() {
        return startDate;
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
        return String.format("<ShareInfo '%s' (%.2f) - %.2f shares of total value %.2f>", name, quote, sharesCount, value);
    }

    private static final class ShareBuilder {
        private String name;
        private double quote;
        private double value;
        private int sharesCount;
        private String error;
        private Date startDate;
    }
}
