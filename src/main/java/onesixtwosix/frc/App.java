package onesixtwosix.frc;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class App 
{
    public static void main( String[] args )
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        // declare globals
        Scanner inputScanner = new Scanner(System.in);
        Integer cameraIndex = 0;

        System.out.println("spyer\n---\nget cooking");
        
        // retrieve library info
        System.out.println("\nlibraries:");
        Functions.retrieveLibraries().forEach((lib, ver) ->
            System.out.println(lib + "::" + ver));
        
        // find available video captures
        List<Integer> cameras = Functions.retrieveCameras();
        System.out.println("\ncameras:");
        System.out.println(cameras);

        // if there is more than one, argue
        if (cameras.size() > 1) {
            System.err.println("pick an index, or by default 0 will be chosen:");
            cameraIndex = inputScanner.nextInt();
            if (cameraIndex < 0 || cameraIndex >= cameras.size()) {
                System.err.println("invalid, setting to 0.");
                cameraIndex = 0;
            }
        } 

        // make a new VC
        VideoCapture capture = new VideoCapture(cameras.get(cameraIndex));
        if (!capture.isOpened()) {
            System.err.println("frame capture failed. (capture not opened)");
            return;
        }

        JFrame mainFrame = new JFrame("spyer - proccessed");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(680,680);
        mainFrame.setVisible(true);
        mainFrame.setResizable(false);
        mainFrame.setAlwaysOnTop(false);

        JFrame sideFrame = new JFrame("spyer - regular RGB");
        sideFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sideFrame.setSize(680,680);
        sideFrame.setVisible(true);
        sideFrame.setResizable(false);
        sideFrame.setAlwaysOnTop(false);

        Canvas maincanvas = new Canvas();
        maincanvas.setSize(680,680);
        mainFrame.add(maincanvas);

        Canvas sidecanvas = new Canvas();
        sidecanvas.setSize(680,680);
        sideFrame.add(sidecanvas);

        Graphics maing = maincanvas.getGraphics();
        Graphics sideg = sidecanvas.getGraphics();

        Mat frame = new Mat();
        while (true) {
            capture.read(frame);

            if (frame.empty()) {
                System.err.println("frame capture failed (frame is empty)");
                break;
            }

            Mat regRBG = new Mat();
            Imgproc.cvtColor(frame, regRBG, Imgproc.COLOR_BGR2RGB);

            Mat hsv = new Mat();
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

            // Red color range in HSV
            Scalar lowerRed1 = new Scalar(0, 120, 70);
            Scalar upperRed1 = new Scalar(10, 255, 255);
            Scalar lowerRed2 = new Scalar(170, 120, 70);
            Scalar upperRed2 = new Scalar(180, 255, 255);

            // Blue color range in HSV
            Scalar lowerBlue = new Scalar(100, 150, 0);
            Scalar upperBlue = new Scalar(140, 255, 255);

            // Create masks for red and blue colors
            Mat maskRed1 = new Mat();
            Mat maskRed2 = new Mat();
            Mat maskBlue = new Mat();

            Core.inRange(hsv, lowerRed1, upperRed1, maskRed1);
            Core.inRange(hsv, lowerRed2, upperRed2, maskRed2);
            Core.inRange(hsv, lowerBlue, upperBlue, maskBlue);

            Mat redMask = new Mat();
            Core.add(maskRed1, maskRed2, redMask);

            List<MatOfPoint> contoursRed = new ArrayList<>();
            Mat hierarchyRed = new Mat();
            Imgproc.findContours(redMask, contoursRed, hierarchyRed, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            List<MatOfPoint> contoursBlue = new ArrayList<>();
            Mat hierarchyBlue = new Mat();
            Imgproc.findContours(maskBlue, contoursBlue, hierarchyBlue, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            Imgproc.drawContours(frame, contoursRed, -1, new Scalar(0, 0, 255), 3); // Red contours in BGR
            Imgproc.drawContours(frame, contoursBlue, -1, new Scalar(255, 0, 0), 3); // Blue contours in BGR

            Mat rgbFrame = new Mat();
            Imgproc.cvtColor(frame, rgbFrame, Imgproc.COLOR_BGR2RGB);

            Image mainimg = Functions.Mat2BufferedImage(rgbFrame);
            Image sideimg = Functions.Mat2BufferedImage(regRBG);

            maing.drawImage(mainimg, 0, 0, maincanvas);
            sideg.drawImage(sideimg, 0, 0, sidecanvas);
        }
        capture.release();
        inputScanner.close();
    }
}
 