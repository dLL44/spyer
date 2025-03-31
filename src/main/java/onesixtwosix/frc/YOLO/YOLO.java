package onesixtwosix.frc.YOLO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;

import java.io.*;
import java.util.*;
import org.json.*;

public class YOLO {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private VideoCapture camera;

    public YOLO(VideoCapture camera) {
        this.camera = camera;
    }

    public void detectAndDisplay() {
        Mat frame = new Mat();

        if (!camera.read(frame)) {
            System.out.println("Cannot read frame");
            return;
        }

        String imagePath = "frame.jpg";
        Imgcodecs.imwrite(imagePath, frame);

        // Call Python YOLO script
        String jsonResult = runPythonYOLO(imagePath);

        // Parse JSON and draw bounding boxes
        if (!jsonResult.equals("unable")) {
            drawBoundingBoxes(frame, jsonResult);
        }

        // Display the modified frame
        HighGui.imshow("YOLO Detection", frame);
        HighGui.waitKey(1);
    }

    private String runPythonYOLO(String imagePath) {
        StringBuilder jsonOutput = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder("python", "YOLO.py", imagePath);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            reader.close();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return "unable";
        }

        return jsonOutput.toString();
    }

    private void drawBoundingBoxes(Mat frame, String jsonResult) {
        try {
            JSONArray detections = new JSONArray(jsonResult);

            for (int i = 0; i < detections.length(); i++) {
                JSONObject obj = detections.getJSONObject(i);
                int x1 = obj.getInt("x1");
                int y1 = obj.getInt("y1");
                int x2 = obj.getInt("x2");
                int y2 = obj.getInt("y2");
                String label = obj.getString("label");

                // Draw bounding box
                Imgproc.rectangle(frame, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0), 2);
                
                // Draw label
                Imgproc.putText(frame, label, new Point(x1, y1 - 5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
