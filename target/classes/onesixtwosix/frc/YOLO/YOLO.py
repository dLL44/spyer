import cv2
import sys
import json
from ultralytics import YOLO

# get the yolo model
model = YOLO("yolov11n.pt")

# TODO i think maybe i should put a cross-platform accesable data struct to access the opencv cap and everything.
