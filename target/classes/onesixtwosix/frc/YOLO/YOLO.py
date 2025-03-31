import sys
import json
import cv2
import torch
from ultralytics import YOLO

# Load YOLO model
model = YOLO("yolov8n.pt")

def detect_objects(image_path):
    img = cv2.imread(image_path)
    
    # Ensure image is loaded
    if img is None:
        print("[]")  # Return empty JSON array
        sys.exit(1)

    results = model(img)

    detections = []
    for result in results:
        for box in result.boxes.data.tolist():
            x1, y1, x2, y2, conf, cls = box
            label = model.names[int(cls)]
            detections.append({
                "x1": int(x1),
                "y1": int(y1),
                "x2": int(x2),
                "y2": int(y2),
                "label": label
            })

    print(json.dumps(detections))  # Ensure only JSON is printed

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("[]")  # Return empty JSON array
        sys.exit(1)

    detect_objects(sys.argv[1])
