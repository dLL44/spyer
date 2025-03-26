package onesixtwosix.frc;

import java.awt.Color;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture; 

import com.formdev.flatlaf.FlatDarkLaf;

public class App {
    // Declare globals
    public static VideoCapture capture = null;
    // private static int cameraIndex = 0;
    public static boolean changingCameras = false;
    public static int teamNoFilter = 1626; // ours for testing and example
    public static int threadSleep = 1;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        int cameraIndex = 0;
        debugWindow dbgwindow = new debugWindow(); // VS Code says its unused, but leave it alone. This is just incase i have to touch the dbgwindow 
        System.out.println("dbgwindow made");
        
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("spyer\n---\nget cooking");

        // Retrieve library info
        System.out.println("\nlibraries:");
        Functions.retrieveLibraries().forEach((lib, ver) ->
            System.out.println(lib + "::" + ver));

        // Find available video captures
        List<Integer> cameras = Functions.retrieveCameras();
        System.out.println("\ncameras:");
        System.out.println(cameras);

        // If multiple cameras exist, let the user pick one
        if (cameras.size() > 1) {
            System.err.println("Pick an index, or by default 0 will be chosen:");
            cameraIndex = Integer.parseInt(JOptionPane.showInputDialog(null, "Pick an index, or by default 0 will be chosen", 0));
            if (cameraIndex < 0 || cameraIndex >= cameras.size()) {
                System.err.println("Invalid, setting to 0.");
                cameraIndex = 0;
            }
        }

        // Create VideoCapture
        try {
            capture = new VideoCapture(cameras.get(cameraIndex));
            if (!capture.isOpened()) {
                System.err.println("Frame capture failed. (capture not opened)");
                return;
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("No Cameras - check if your camera is on.");
            System.err.println("Stack Trace:\n"+e);
        }


        // Create JFrame with a custom JPanel 
        JFrame mainFrame = new JFrame("spyer - camera feed");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1400, 715);
        mainFrame.setResizable(false);
        mainFrame.setAlwaysOnTop(false);
        mainFrame.setBackground(Color.DARK_GRAY);

        // Create menuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu options = new JMenu("Options"); // Options, everything below is self explanatory.
        mainFrame.setJMenuBar(menuBar);

        // Options
        JMenuItem changeTeamFilter = new JMenuItem("Change Team Filter");
        changeTeamFilter.addActionListener(e -> {
            try {
                int newTeamFilter = Integer.parseInt(JOptionPane.showInputDialog(mainFrame, "Insert new team number to look for", teamNoFilter));
                teamNoFilter = newTeamFilter;
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        options.add(changeTeamFilter);

        JMenuItem changeThreadSleep = new JMenuItem("Change Thread Sleep");
        changeThreadSleep.addActionListener(e -> {
            try {
                int newThreadSleep = Integer.parseInt(JOptionPane.showInputDialog(mainFrame, "Insert new thread sleep time ( Milliseconds )\nAFFECTS FPS", threadSleep));
                threadSleep = newThreadSleep;
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        options.add(changeThreadSleep);

        JRadioButtonMenuItem toggleDbg = new JRadioButtonMenuItem("Toggle Debug Window Visibility");
        toggleDbg.setSelected(dbgwindow.debugFrame.isVisible());
        toggleDbg.addActionListener(e -> {
            try {
                boolean isVisible = dbgwindow.debugFrame.isVisible();
                if (isVisible) {
                    dbgwindow.debugFrame.setVisible(false);
                } else {
                    dbgwindow.debugFrame.setVisible(true);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        options.add(toggleDbg);

        menuBar.add(options);

        VideoPanel videoPanel = new VideoPanel(capture, teamNoFilter, threadSleep); // since i cant access the class, i put the editable vars in the init func.
        mainFrame.add(videoPanel);
        mainFrame.setVisible(true);
        
        // Start video update loop
        new Thread(videoPanel).start();
    }
}
