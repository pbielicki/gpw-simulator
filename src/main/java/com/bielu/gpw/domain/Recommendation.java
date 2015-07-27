package com.bielu.gpw.domain;

public class Recommendation {

  private final String shareName;
  private final String text;
  private final String price;

  public Recommendation(String shareName, String text, String price) {
    this.shareName = shareName;
    this.text = text;
    this.price = price;
  }

  public String getShareName() {
    return shareName;
  }

  public String getText() {
    return text;
  }

  public String getPrice() {
    return price;
  }

  @Override
  public String toString() {
    return String.format("'%s' at %s for %s", text, price, shareName);
  }
}
