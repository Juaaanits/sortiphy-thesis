<p align="center">
<img width="2000" height="750" alt="sortiphy logo nontransparent" src="https://github.com/user-attachments/assets/41e2176b-b534-4ab4-aa8b-a95bc416c2e9" />
</p>

# SortiPhy: Recyclable Waste and Monitoring System

SortiPhy is a smart waste bin and waste management system that utilizes deep learning and sensors for waste management. The project is built with Raspberry Pi, ESP32, TensorFlow, Android Studio, and Firebase. SortiPhy also includes an Android app that will be used as a medium for monitoring the bin's status and sending notification regarding waste to its personnel.

# Features
- A trash bin that can identify, via deep learning between the following categories of waste: glass, non-recyclable, paper, plastic and metal.
- A compacting system that uses linear actuators to allow more trash per bin.
- A Firebase-powered android application that sends realtime notifications when the bin is close to being full and its current fill levels.
- A fully working account and authentication system on the android application powered by Firebase.

# Tech Stack
- Android Studio with Java for Application Development
- Ubuntu 20.04 Server (Focal Fossi) for Raspberry Pi
- ESP32 with Arduino IDE for Embedded Systems Development
- TensorFlow with Google Colab/Jupyter for Pre-Trained Model
- Firebase Console for Backend Development and Device Communication
- Cloudinary for Storage
- Cloud Firestore for Database
- Firebase Authentication for User Authentication
- Firebase Cloud Messaging for Notifications

# How it Worked
1. The user would place a piece of waste (the system could only accept one waste at a time) into the trash chute.
2. An ultrasonic sensor would detect this and move a conveyor belt after the camera has identified the waste category.
3. If the Raspberry Pi detected that the waste was glass, it will continue, otherwise, the compactor will activate.
4. Then the trash will be moved via conveyor belt to the second layer of trashbin.
5. Motors would move it to the appropriate bin and then drop the waste.
6. The sensors would trigger and tell the Raspberry Pi to update the Firebase backend.
7. If the waste level threshold triggers a certain fill level, all users wouild be notified.
8. Otherwise, the application would only update.
9. User could register and login to the application to view the waste statistics.


# Images
*Note: The Firebase backend has been shut down, and so, access to the application is no longe possible. The images for the applications are the initial draft. This repository serves as the documentation of the thesis work and the codebase.*

**Android Application**
Login Screen:

<img width="554" height="981" alt="image" src="https://github.com/user-attachments/assets/9b6a9748-e70f-49d9-b87a-894f77ee0229" />

Dashboard:

<img width="577" height="1014" alt="image" src="https://github.com/user-attachments/assets/d91f1e33-271a-4266-9b44-1ded8378e832" />

---

**Hardware**

*Note: This was the first iteration of the design, multiple changes have been made as requested by the panelist, so the following designs only represent the initial design, which lacked a compactor.*

Prototype 3D Design:

<img width="891" height="502" alt="image" src="https://github.com/user-attachments/assets/8bc5fd26-0166-4e24-9b96-cfd076147f55" />
<img width="869" height="490" alt="image" src="https://github.com/user-attachments/assets/2f534604-f96d-4c3e-83ac-5e15bb7aa825" />
<img width="902" height="507" alt="image" src="https://github.com/user-attachments/assets/181229c1-57f7-4769-b4f5-2fb2ed282219" />





