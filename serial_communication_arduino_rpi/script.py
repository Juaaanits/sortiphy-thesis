import cv2
import numpy as np
import tflite_runtime.interpreter as tflite
import serial
import time
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime
import os

#Firebase initialization
cred = credentials.Certificate("/home/sortiphy/sortiphy-firebase-adminsdk-fbsvc-6a196b99d0.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

#Initialize serial connection to Arduino
arduino = serial.Serial('/dev/ttyACM0', 9600, timeout=1)
time.sleep(3)  # Wait for Arduino to initialize

#Model and labels
MODEL_PATH = "/home/sortiphy/FinalModelXception.tflite"
LABELS = ['glass', 'non-recyclable', 'paper', 'recyclable']
INPUT_SIZE = (299, 299)

#Load TFLite model
interpreter = tflite.Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

# Get input and output tensors
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print("Model loaded successfully. Ready to classify!")

# Map classification index to bin document name
def get_bin_doc_name(classification_idx):
    bin_mapping = {
        0: "trashClassificationOne",  # glass
        1: "trashClassificationTwo",  # non-recyclable
        2: "trashClassificationThree",  # paper
        3: "trashClassificationFour",  # recyclable
    }
    return bin_mapping.get(classification_idx, "trashClassificationUnknown")

#Capture image and classify
def capture_and_classify():
    #Open camera (avoid lag)
    cam = cv2.VideoCapture(0)
    cam.set(cv2.CAP_PROP_FRAME_WIDTH, 650)
    cam.set(cv2.CAP_PROP_FRAME_HEIGHT, 650)

    #Capture frame
    ret, frame = cam.read()
    if not ret or frame is None:
        print(" Failed to capture image!")
        cam.release()
        return None

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    save_dir = "/home/sortiphy/captured_images/"
    os.makedirs(save_dir, exist_ok=True)
    image_path = os.path.join(save_dir, f"image_{timestamp}.jpg")
    print(f" Image saved at {image_path}")

    #Preprocess image
    img = cv2.resize(frame, INPUT_SIZE)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = img / 255.0
    img = np.expand_dims(img, axis=0).astype(np.float32)

    #Run inference
    interpreter.set_tensor(input_details[0]['index'], img)
    interpreter.invoke()

    #Get classification results
    output = interpreter.get_tensor(output_details[0]['index'])
    classification_idx = np.argmax(output)
    label = LABELS[classification_idx]

    #Send classification (0-4) to Arduino
    classification_to_send = classification_idx
    arduino.write(f"{classification_to_send}\n".encode())
    print(f" Sent classification: {classification_to_send} ({label}) to Arduino")

    send_classification_to_firebase(classification_to_send)
    update_daily_statistics(classification_to_send)
    update_activity_level()
    print(f" Activity level updated to firestore")

    cam.release()
    return classification_to_send

def get_arduino_float():
    while True:  # Keep trying until we get valid data
        if arduino.in_waiting > 0:
            line = arduino.readline().decode('utf-8').strip()
            print(f" Raw input from Arduino: {line}")
            
            if line:  
                val = float(line)
                if 0 <= val <= 100:
                    return val
                else:
                    print(" Invalid fill level range.")
        
        # Small delay to prevent CPU overload
        time.sleep(0.01)

#Send classification to Firebase
def send_classification_to_firebase(classification_idx):
    doc_name = get_bin_doc_name(classification_idx)

    fillLevel = get_arduino_float()

    #Data to be sent to Firestore under binData collection
    data_to_upload = {
        "className": LABELS[classification_idx],
        "fillLevel": fillLevel,
        "isCompacting": False,
        "isFull": False
    }

    #Upload data to Firebase
    try:
        db.collection("binData").document(doc_name).set(data_to_upload)
        print(f" Data sent to Firebase: {doc_name} -> {data_to_upload}")
    except Exception as e:
        print(f" Failed to send data to Firebase: {e}")

#Update daily statistics in Firestore (statistics/dailyTrashStatistics)
def update_daily_statistics(classification_idx):
    category_mapping = {
        0: "categoryOneCount",  # glass
        1: "categoryTwoCount",  # non-recyclable
        2: "categoryThreeCount",  # paper
        3: "categoryFourCount",  # recyclable
        #4: "categoryFiveCount"  # Plastic
    }

    #Get the correct field name for the category
    category_field = category_mapping.get(classification_idx)

    #References to all statistics documents
    daily_ref = db.collection("statistics").document("dailyTrashStatistics")
    weekly_ref = db.collection("statistics").document("weeklyTrashStatistics")
    monthly_ref = db.collection("statistics").document("monthlyTrashStatistics")

    # Batch write for atomic updates
    batch = db.batch()

    # Function to handle document update or creation
    def update_or_create_doc(ref, doc_type):
        doc = ref.get()
        if doc.exists:
            current_data = doc.to_dict()
            new_count = current_data.get(category_field, 0) + 1
            batch.update(ref, {
                category_field: new_count,
                "lastUpdated": firestore.SERVER_TIMESTAMP
            })
            print(f" Updated {category_field} in {doc_type} to {new_count}")
        else:
            print(f" {doc_type} document not found. Creating new document...")
            batch.set(ref, {
                "categoryOneCount": 0,
                "categoryTwoCount": 0,
                "categoryThreeCount": 0,
                "categoryFourCount": 0,
                "lastUpdated": firestore.SERVER_TIMESTAMP,
                "type": doc_type.capitalize()
            })
            # Increment count for the new document
            batch.update(ref, {
                category_field: 1,
                "lastUpdated": firestore.SERVER_TIMESTAMP
            })

    update_or_create_doc(daily_ref, "daily")
    update_or_create_doc(weekly_ref, "weekly")
    update_or_create_doc(monthly_ref, "monthly")

    try:
        batch.commit()
        print(" Successfully updated all statistics documents")
    except Exception as e:
        print(f" Error updating statistics: {e}")

def update_activity_level():
    current_hour = datetime.now().hour #kukunin hour in terms or army number

    time_intervals = {
        "6am": range(6, 8),    # 6-8am
        "8am": range(8, 10),   # 8-10am
        "10am": range(10, 12), # 10-12pm
        "12pm": range(12, 14), # 12-2pm
        "2pm": range(14, 16),  # 2-4pm
        "4pm": range(16, 18)  # 4-6pm
    }

    time_interval_field = next(
        (key for key, val in time_intervals.items() if current_hour in val),
        None
    )
    if not time_interval_field:
        print(" Current time does not match any interval.")
        return

    # Para makaconnect sa timeStamp ng firestore
    daily_ref_ts = db.collection("timeStamp").document("daily")
    weekly_ref_ts = db.collection("timeStamp").document("weekly")
    monthly_ref_ts = db.collection("timeStamp").document("monthly")

    batch = db.batch()

    def update_or_create_activity(ref, doc_type):
        doc = ref.get()
        if doc.exists:
            current_data = doc.to_dict()
            new_count = current_data.get(time_interval_field, 0) + 1
            batch.update(ref, {
                time_interval_field: new_count,
                "lastUpdated": firestore.SERVER_TIMESTAMP
            })
            print(f" Updated {time_interval_field} in {doc_type} to {new_count}")
        else:
            print(f" {doc_type} document not found. Creating new document...")
            initial_data = {
                "6am": 0,
                "8am": 0,
                "10am": 0,
                "12pm": 0,
                "2pm": 0,
                "4pm": 0,
                "lastUpdated": firestore.SERVER_TIMESTAMP
            }
            batch.set(ref, initial_data)
            batch.update(ref, {
                time_interval_field: 1,
                "lastUpdated": firestore.SERVER_TIMESTAMP
            })
            print(f" Created and updated {time_interval_field} in {doc_type}")

    update_or_create_activity(daily_ref_ts, "daily")
    update_or_create_activity(weekly_ref_ts, "weekly")
    update_or_create_activity(monthly_ref_ts, "monthly")

    try:
        batch.commit()
        print(" Successfully updated all timeStamp documents.")
    except Exception as e:
        print(f" Error updating activity level: {e}")

#Main loop for object detection and classification
if __name__ == "__main__":
    print(" System ready to classify and send data to Arduino & Firebase!")
    while True:
        if arduino.in_waiting > 0:
            line = arduino.readline().decode('utf-8').strip()
            print(f" Raw input from Arduino: {line}")

            #Detect 'Detected' signal from Arduino for object capture
            if line == 'Detected':
                print(" Object detected! uring and classifying...")
                capture_and_classify()
                time.sleep(5)  # Delay to avoid frequent classification

        #Short delay to reduce CPU usage
        time.sleep(0.1)
