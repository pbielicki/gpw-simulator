package com.bielu.gpw.listener.gui.plugin;

import java.io.PrintStream;

public class TextPrintStream extends PrintStream {

  private TextOutputStream out;

  public TextPrintStream(TextOutputStream out) {
    super(out);
    this.out = out;
  }

  protected TextOutputStream getOut() {
    return out;
  }
}
