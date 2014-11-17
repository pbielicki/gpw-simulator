package com.bielu.gpw.listener.gui.plugin;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogPlugin implements GuiPlugin {

    private static final int FONT_SIZE = 12;
    
    @Override
    public JComponent getComponent() {
        if (System.out instanceof TextPrintStream) {
            final JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setAutoscrolls(true);
            textArea.setFont(new Font("Courier New", Font.PLAIN, FONT_SIZE));
            JPopupMenu menu = new JPopupMenu();
            JMenuItem clearMenu = new JMenuItem("Clear logs");
            clearMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    textArea.setText("");
                }
            });
            menu.add(clearMenu);
            textArea.setComponentPopupMenu(menu);
            
            ((TextPrintStream) System.out).getOut().setTextArea(textArea);
            if (System.err instanceof TextPrintStream) {
                ((TextPrintStream) System.err).getOut().setTextArea(textArea);
            }
            return new JScrollPane(textArea);
        } else {
            return null;
        }
    }
}
