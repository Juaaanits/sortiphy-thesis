'''import cv2
import numpy as np
import tflite_runtime.interpreter as tflite
import serial
import time

# Initialize serial communication with Arduino Nano
arduino = serial.Serial('/dev/ttyUSB0', 9600, timeout=1)  # Change port if needed

MODEL_PATH = "/home/sortiphy/second_training_tflite.tflite"
LABELS = ['biological', 'glass', 'metal', 'paper', 'plastic']
INPUT_SIZE = (224, 224)

# Load and allocate the model
interpreter = tflite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

# Get input & output tensors
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Capture and classify the image
def capture_and_classify():
    """Capture an image, classify, and send result to Arduino."""
    cam = cv2.VideoCapture(0)
    cam.set(cv2.CAP_PROP_FRAME_WIDTH, 650)
    cam.set(cv2.CAP_PROP_FRAME_HEIGHT, 650)

    ret, frame = cam.read()
    if not ret or frame is None:
        print("❌ Failed to capture image!")
        cam.release()
        return None

    # Preprocess the image
    img = cv2.resize(frame, INPUT_SIZE)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img / 255.0
    img = np.expand_dims(img, axis=0).astype(np.float32)

    # Run inference
    interpreter.set_tensor(input_details[0]['index'], img)
    interpreter.invoke()

    # Get classification results
    output = interpreter.get_tensor(output_details[0]['index'])
    label_idx = np.argmax(output)
    label = LABELS[label_idx]

    # Send classification (1-5) to Arduino
    classification_to_send = label_idx + 1
    arduino.write(f"{classification_to_send}\n".encode())
    print(f"📡 Sent classification: {classification_to_send} ({label}) to Arduino")

    cam.release()

# Main loop: Wait for object detection from Arduino
if name == "main":
    print("🚀 Ready to classify and send data to Arduino!")
    while True:
        if arduino.in_waiting > 0:
	    # Read
            line = arduino.readline().decode('utf-8').strip()
            print(f"📩 Raw input: {line}")  # Debugging: See what data is received

            if line and line == "Detected":
                print("✅ Object detected! Capturing and classifying...")
                capture_and_classify()
                time.sleep(5)  # Delay to prevent multiple triggers
        
        time.sleep(0.1)
'
'''

# For serial connection with Arduino
import serial
import time
import subprocess

# For object detection
import cv2
import numpy as np
import tflite_runtime.interpreter as tflite


# Initialize serial connection to Arduino
arduino = serial.Serial('/dev/ttyACM0', 9600, timeout=1) 
time.sleep(2)  # Wait for Arduino to initialize

MODEL_PATH = "/home/sortiphy/second_training_tflite.tflite"
LABELS = ['biological', 'glass', 'metal', 'paper', 'plastic']
INPUT_SIZE = (224, 224)

# Load and allocate the model
interpreter = tflite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

# Get input & output tensors
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Capture and classify the image
def capture_and_classify():
    """Capture an image, classify, and send result to Arduino."""
    cam = cv2.VideoCapture(0)
    cam.set(cv2.CAP_PROP_FRAME_WIDTH, 650)
    cam.set(cv2.CAP_PROP_FRAME_HEIGHT, 650)

    ret, frame = cam.read()
    if not ret or frame is None:
        print("❌ Failed to capture image!")
        cam.release()
        return None

    # Preprocess the image
    img = cv2.resize(frame, INPUT_SIZE)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img / 255.0
    img = np.expand_dims(img, axis=0).astype(np.float32)

    # Run inference
    interpreter.set_tensor(input_details[0]['index'], img)
    interpreter.invoke()

    # Get classification results
    output = interpreter.get_tensor(output_details[0]['index'])
    label_idx = np.argmax(output)
    label = LABELS[label_idx]

    # Send classification (1-5) to Arduino
    classification_to_send = label_idx + 1
    arduino.write(f"{classification_to_send}\n".encode())
    print(f"📡 Sent classification: {classification_to_send} ({label}) to Arduino")

    cam.release()


# Main loop: Wait for object detection from Arduino
if name == "main":
    print("Ready to classify and send data to Arduino!")
    while True:
        if arduino.in_waiting > 0:
            line = arduino.readline().decode('utf-8').strip()
            print(f"📩 Raw input: {line}")

            if line == 'Detected':
		print("Object detected. Capturing and classifying..")
		capture_and_classify()
		time.sleep(5)
	
	time.sleep(0.1)

 print("🚀 Ready to classify and send data to Arduino!")
    while True:
        if arduino.in_waiting > 0:
	    # Read
            line = arduino.readline().decode('utf-8').strip()
            print(f"📩 Raw input: {line}")  # Debugging: See what data is received

            if line and line == "Detected":
                print("✅ Object detected! Capturing and classifying...")
                capture_and_classify()
                time.sleep(15)  # Delay to prevent multiple triggers
        
        time.sleep(0.1)