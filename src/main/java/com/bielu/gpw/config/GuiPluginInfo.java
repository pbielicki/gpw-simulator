package com.bielu.gpw.config;

public class GuiPluginInfo {

    private final String label;
    private final String className;

    public GuiPluginInfo(String label, String className) {
        this.label = label;
        this.className = className;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return String.format("GuiPlugin: %s, %s", label, className);
    }
}
