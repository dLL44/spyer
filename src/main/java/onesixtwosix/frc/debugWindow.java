package onesixtwosix.frc;

import onesixtwosix.frc.Classes.TextAreaOutputStream;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/** The Debug Window */
class debugWindow {
    private static JTextArea debugText;     
    public  static JFrame debugFrame;

    /** debugWindow Init */
    public debugWindow() {
        debugFrame = new JFrame();
        debugText = new JTextArea();

        debugFrame.setResizable(false);
        debugFrame.setSize(700, 500);
        debugFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        debugText.setEditable(false);
        debugText.setBackground(Color.BLACK);
        debugText.setForeground(Color.GREEN);

        JScrollPane scrollpane = new JScrollPane(debugText);
        debugFrame.add(scrollpane, BorderLayout.CENTER);

        debugFrame.setVisible(true);

        PrintStream printstream = new PrintStream(new TextAreaOutputStream(debugText));
        System.setOut(printstream);
        System.setErr(printstream);
    }
}