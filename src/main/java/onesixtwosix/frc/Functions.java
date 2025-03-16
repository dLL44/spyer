package onesixtwosix.frc;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Functions {
    /**
     * Retrieves libraries' verisons, nothing complex
     * @return LinkedHashMap<> with library's name and version
     */
    public static Map<String, String> retrieveLibraries() {
        Map<String, String> libraries = new LinkedHashMap<>();
        libraries.put("opencv", Core.VERSION);
        return libraries;
    }    

    /**
     * Retrieve available VideoCaptures
     * @return Array of available VCs
     */
    public static List<Integer> retrieveCameras() {
        List<Integer> cameralist = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            VideoCapture tempCapture = new VideoCapture(i);

            if (tempCapture.isOpened()) {
                cameralist.add(i);
                tempCapture.release();
            }
        }
        return cameralist;
    }

    /**
     * Convert a given mat to a buffered image
     * @param mat
     * @return a buffered image of provided mat
     */
    public static Image Mat2BufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }
}
