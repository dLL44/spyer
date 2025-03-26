package onesixtwosix.frc;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

import java.io.IOException;
import java.nio.file.Paths;

public class YoloModel {
    private Model model;
    private Predictor<Image, DetectedObjects> predictor; // Fix: Change expected type to DetectedObjects

    // Constructor: Loads YOLO model
    public YoloModel(String modelPath) throws IOException, ModelException {
        model = Model.newInstance(modelPath);
        
        // Fix: Use `YoloV5Translator.builder().build()` for correct type
        Translator<Image, DetectedObjects> translator = YoloV5Translator.builder().build();
        predictor = model.newPredictor(translator);
    }

    // Method to run YOLO detection on an image
    public DetectedObjects detectObjects(String imagePath) throws IOException, TranslateException {
        Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
        return predictor.predict(img); // Fix: Returns `DetectedObjects`
    }

    // Close the model
    public void close() throws IOException {
        predictor.close();
        model.close();
    }
}
