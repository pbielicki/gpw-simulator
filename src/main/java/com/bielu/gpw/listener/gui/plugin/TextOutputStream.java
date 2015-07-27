package com.bielu.gpw.listener.gui.plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

public class TextOutputStream extends OutputStream {

  private static final int BUFFER_LENGTH = 100;
  private JTextArea textArea;
  private PrintStream out;
  private StringBuilder buffer = new StringBuilder();

  public TextOutputStream(PrintStream out) {
    this.out = out;
  }

  protected synchronized void setTextArea(JTextArea textArea) {
    this.textArea = textArea;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    buffer.append((char) b);
    if (buffer.length() < BUFFER_LENGTH) {
      return;
    } else if (textArea != null) {
      textArea.append(buffer.toString());
    }
    buffer = new StringBuilder();
  }

  @Override
  public void flush() throws IOException {
    super.flush();
    if (textArea != null) {
      textArea.append(buffer.toString());
      buffer = new StringBuilder();
    }
  }
}
