package onesixtwosix.frc.Classes;

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

public class TextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        buffer.write(b);
        if (b == '\n') {
            flush();
        }
    }

    @Override
    public void flush() {
        SwingUtilities.invokeLater(() -> {
            textArea.append(buffer.toString());
            textArea.setCaretPosition(textArea.getDocument().getLength());
            buffer.reset();
        });
    }
}
