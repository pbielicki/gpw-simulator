package com.bielu.gpw.config;

public class MenuItemInfo {

  private final String type;
  private final String label;
  private final String className;

  public MenuItemInfo(String type, String label, String className) {
    this.type = type;
    this.label = label;
    this.className = className;
  }

  public String getType() {
    return type;
  }

  public String getClassName() {
    return className;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return String.format("MenuItem: %s, %s, %s", type, label, className);
  }
}
