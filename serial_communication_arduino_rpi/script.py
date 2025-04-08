
# For serial connection with Arduino
import serial
import time
import subprocess
#import myserial

# For capturing image and object detection
import cv2
import numpy as np
import tflite_runtime.interpreter as tflite

# For firebase connection
import firebase_admin
from firebase_admin import credentials, firestore

cred = credentials.Certificate("/home/sortiphy/sortiphy-firebase-adminsdk-fbsvc-6a196b99d0.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Initialize serial connection to Arduino
arduino = serial.Serial('/dev/ttyUSB0', 9600, timeout=1) 
time.sleep(3)  # Wait for Arduino to initialize

MODEL_PATH = "/home/sortiphy/second_training_no_bg_tflite.tflite"
LABELS = ['biological', 'glass', 'metal', 'paper', 'plastic']
INPUT_SIZE = (224, 224)

interpreter = tflite.Interpreter(model_path=MODEL_PATH)


def load_the_model():
    # Model Loading 
    interpreter.allocate_tensors()

    # Get input and output tensors
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    print("Model loaded successfully")

    return input_details, output_details


def capture_image():
    #  Para hinddi maglag
    cam = cv2.VideoCapture(0)
    cam.set(cv2.CAP_PROP_FRAME_WIDTH, 650)
    cam.set(cv2.CAP_PROP_FRAME_HEIGHT, 650)

    ret, frame = cam.read()
    if not ret or frame is None:
        print("Failed to capture image!")
        cam.release()
        return None
    print("Image Captured")
    image_preprocess()
    cam.release()
    return frame

# Image preprocessing
def image_preprocess(frame, input_details):
    img = cv2.resize(frame, INPUT_SIZE)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img / 255.0
    img = np.expand_dims(img, axis=0).astype(np.float32)

    interpreter.set_tensor(input_details[0]['index'], img)
    interpreter.invoke()

def classify_the_image(output_details):
    # Get classification results
    output = interpreter.get_tensor(output_details[0]['index'])
    label_idx = np.argmax(output)
    label = LABELS[label_idx]

    return label

def pass_the_classification_to_arduino(label):
    classification_to_send = label
    arduino.write(f"{classification_to_send}\n".encode())
    print(f"Sent classification: {classification_to_send} ({label}) to Arduino")

# di pa nababasa talaga yung label
def read_the_binLevel_fromArduinoA(label):
    serial_output = arduino.readline().decode('utf-8').strip()


    #kapag nabasa send sa firebase
    #call out yung function na rpi to firebase
    # pLAN a: if yung serial_output is floaT, DOON PALANG MAGSEND
    # Plan B: while serial output is not a float, do nothing

    try:
        bin_level = float(serial_output)
        print(f"Bin level: {bin_level}")
        send_classification_to_firebase(label)
    except ValueError:
        print("Invalid bin level, skipping...")



def send_classification_to_firebase(classification_idx):
    doc_name = f"trashClassification{classification_idx}"

    data_to_upload = {
        "className": LABELS[classification_idx],
        "fillLevel": np.random.randint(0, 100), 
        "isCompacting": False,  
        "isFull": False 
    }

    # Send data to Firestore
    try:
        db.collection("binData").document(doc_name).set(data_to_upload)
        print(f"Data sent to Firebase: {doc_name} -> {data_to_upload}")
        update_daily_statistics()
    except Exception as e:
        print(f"Failed to send data to Firebase: {e}")

def update_daily_statistics(classification_idx):
    category_mapping = {
        0: "categoryOneCount",  # Biological
        1: "categoryTwoCount",  # Glass
        2: "categoryThreeCount",  # Metal
        3: "categoryFourCount",  # Paper
        4: "categoryFiveCount"  # Plastic
    }

    # Get the correct field name for the category
    category_field = category_mapping.get(classification_idx)

    # Reference to statistics document
    statistics_reference = db.collection("statistics").document("dailyTrashStatistics")

    # Get current statistics data
    doc = statistics_reference.get()
    if doc.exists:
        current_data = doc.to_dict()
        # Increment count for the corresponding category
        new_count = current_data.get(category_field, 0) + 1
        # Update statistics in Firestore
        statistics_reference.update({
            category_field: new_count,
            "lastUpdated": firestore.SERVER_TIMESTAMP
        })
        print(f"Updated {category_field} to {new_count}")
    else:
        print("dailyTrashStatistics document not found. Creating new document...")
        # Create document with initial statistics if it does not exist
        statistics_reference.set({
            "categoryOneCount": 0,
            "categoryTwoCount": 0,
            "categoryThreeCount": 0,
            "categoryFourCount": 0,
            "categoryFiveCount": 0,
            "lastUpdated": firestore.SERVER_TIMESTAMP,
            "type": "Daily"
        })
        

if __name__ == "__main__":
    input_details, output_details = load_the_model()
    while True:
        if arduino.in_waiting > 0:
            line = arduino.readline().decode('utf-8').strip() #pambasa ng serial output
            print(f"Raw input: {line}")

            if line == 'Detected':
                print("Object detected. Capturing and classifying..")

                capture_image()
                frame = capture_image()
                if frame is not None:
                    image_preprocess(frame, input_details, output_details)
                    labels = classify_the_image(output_details)
                    pass_the_classification_to_arduino(labels)
                    read_the_binLevel_fromArduinoA(labels)
  
            
                
                time.sleep(5)
	
    time.sleep(0.1)
