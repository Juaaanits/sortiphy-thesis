/*#include <Servo.h>

//    Ultrasonic sensor for object detection
const int trigPin = 4;
const int echoPin = 5;

// Ultrasonic sensors for bin fill detection
const int trigBin = 8;
const int echoBin = 9;

// Pins for motor control (Classification)
const int stepPin = 3;
const int dirPin = 2;

// Pins for platform control

#define AIN1 6
#define AIN2 7
#define PWMA 5
#define BIN1 10
#define BIN2 11
#define PWMB 9



// Stepper motor for compression

#define COMPRESSOR_STEP_1 3
#define COMPRESSOR_DIR_1 2

#define COMPRESSOR_STEPS 205000  // Steps for compression

#define COMPRESSOR_STEP_2 10
#define COMPRESSOR_DIR_2 9

// Variables for ultrasonic sensors
//float duration, distance;
//int classification = 0;  
//const int motorDelay = 100;  // Stepper motor delay
//const int binFullThreshold = 15;  // cm threshold for full bin

// Steps per classification
//const int stepsPerClass[] = {0, 5000, 10000, 15000, 20000, 25000};  

void setup() {

  
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(trigBin, OUTPUT);
  pinMode(echoBin, INPUT);
  pinMode(stepPin, OUTPUT);
  pinMode(dirPin, OUTPUT);
  pinMode(AIN1, OUTPUT);
  pinMode(AIN2, OUTPUT);
  pinMode(PWMA, OUTPUT);
  pinMode(BIN1, OUTPUT);
  pinMode(BIN2, OUTPUT);
  pinMode(PWMB, OUTPUT);

  
  pinMode(COMPRESSOR_STEP_1, OUTPUT);
  pinMode(COMPRESSOR_DIR_1, OUTPUT);

  pinMode(COMPRESSOR_STEP_2, OUTPUT);
  pinMode(COMPRESSOR_DIR_2, OUTPUT);

  Serial.begin(9600);
}

void loop() {
  activateCompressor();


  //rotateMotor(100000);
  distance = getTheDistance();
  int binFill = getBinFillLevel();  // Check fill level

  if (distance <= 10) {
    Serial.print("Detected");
    delay(3000);

    // Wait for classification result from Raspberry Pi
    while (Serial.available() == 0) {}
    classification = Serial.parseInt();  
    Serial.print("🔄 Classification Received: ");
    Serial.println(classification);

    // Rotate motor based on classification
    rotateMotor(stepsPerClass[classification]);

    // Check if bin is full
    if (binFill <= binFullThreshold) {
      Serial.println("Bin Full - Activating Compressor");
      activateCompressor();  // Compress before opening platform
    }

    // Open platform to drop trash
    openThePlatform(150);
    delay(1000);

    delay(2000); // Wait after dropping trash

    closeThePlatform(150);
    delay(1000);

    resetMotor(stepsPerClass[classification]);

    stopMotors();

    delay(2000);  // Wait before next detection
  }
  
}

// Function to measure object distance


int getTheDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  return duration * 0.034 / 2;
}

// Function to check bin fill level
int getBinFillLevel() {
  digitalWrite(trigBin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigBin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigBin, LOW);
  long durationBin = pulseIn(echoBin, HIGH);
  int binFill = durationBin * 0.034 / 2;
  Serial.print("Bin Fill Level: ");
  Serial.println(binFill);
  return binFill;
}

// Rotate stepper motor for classification
void rotateMotor(int steps) {
  for (int i = 0; i < steps; i++) {
    digitalWrite(stepPin, HIGH);
    delayMicroseconds(motorDelay);
    digitalWrite(stepPin, LOW);
    delayMicroseconds(motorDelay);
  } 
}



// Reset classification motor


void resetMotor(int steps) {
  digitalWrite(dirPin, LOW);
  for (int i = 0; i < steps; i++) {
    digitalWrite(stepPin, HIGH);
    delayMicroseconds(motorDelay);
    digitalWrite(stepPin, LOW);
    delayMicroseconds(motorDelay);
  }
  digitalWrite(dirPin, HIGH);
}



// Activate NEMA 17 stepper motor for compression
void activateCompressor() {
  digitalWrite(COMPRESSOR_DIR_1, HIGH);
  digitalWrite(COMPRESSOR_DIR_2, HIGH);
  for (long i = 0; i < COMPRESSOR_STEPS; i++) {
    digitalWrite(COMPRESSOR_STEP_1, HIGH);
    digitalWrite(COMPRESSOR_STEP_2, HIGH);
    delayMicroseconds(20);
    digitalWrite(COMPRESSOR_STEP_1, LOW);
    digitalWrite(COMPRESSOR_STEP_2, LOW);
    delayMicroseconds(20);
  }
  
  delay(2000);

  digitalWrite(COMPRESSOR_DIR_1, LOW);
  digitalWrite(COMPRESSOR_DIR_2, LOW);
  for (long i = 0; i < COMPRESSOR_STEPS; i++) {
    digitalWrite(COMPRESSOR_STEP_1, HIGH);
    digitalWrite(COMPRESSOR_STEP_2, HIGH);
    delayMicroseconds(20);
    digitalWrite(COMPRESSOR_STEP_1, LOW);
    digitalWrite(COMPRESSOR_STEP_2, LOW);
    delayMicroseconds(20);
  }

  delay(2000);
  Serial.println("Compression Complete");
}



// Open platform
void openThePlatform(int speed) {
  digitalWrite(AIN1, HIGH);
  digitalWrite(AIN2, LOW);
  analogWrite(PWMA, speed);
  digitalWrite(BIN1, HIGH);
  digitalWrite(BIN2, LOW);
  analogWrite(PWMB, speed);
  Serial.println("Opening Platform...");
}

// Close platform
void closeThePlatform(int speed) {
  digitalWrite(AIN1, LOW);
  digitalWrite(AIN2, HIGH);
  analogWrite(PWMA, speed);
  digitalWrite(BIN1, LOW);
  digitalWrite(BIN2, HIGH);
  analogWrite(PWMB, speed);
  Serial.println("Closing Platform...");
}*/

// For Classification Motors

const int stepPin = 3;
const int dirPin = 2;

void setup() {
  pinMode(stepPin, OUTPUT);
  pinMode(dirPin, OUTPUT);
  rotateMotor(stepPin, dirPin, 46250, true, 80);
  delay(2000);
}

void loop() {

  int classType = 0;
  switch (classType) {
    case 0:
      rotateMotor(stepPin, dirPin, 15416, true, 80);
      delay(2000);
      rotateMotor(stepPin, dirPin, 15416, false, 80);
      delay(2000);
    break;
    case 1:
      rotateMotor(stepPin, dirPin, 46250, true, 80);
      delay(2000);
      rotateMotor(stepPin, dirPin, 46250, false, 80);
      delay(2000);
    break;
    case 2:
      rotateMotor(stepPin, dirPin, 15416, false, 80);
      delay(2000);
      rotateMotor(stepPin, dirPin, 15416, true, 80);
      delay(2000);
    break;
    case 3:
      rotateMotor(stepPin, dirPin, 46250, false, 80);
      delay(5000);
      rotateMotor(stepPin, dirPin, 46250, true, 80);
      delay(5000);
      break;
    default:
      rotateMotor(stepPin, dirPin, 0, true, 80);
      delay(5000);
      rotateMotor(stepPin, dirPin, 0, false, 80);
      delay(5000);
    break;
  }

  
}

void rotateMotor(int stepPin, int dirPin, long steps, bool dir, long motorDelay) {
  digitalWrite(dirPin, dir);
  for (long i = 0; i < steps; i++) {
    digitalWrite(stepPin, HIGH);
    delayMicroseconds(motorDelay);
    digitalWrite(stepPin, LOW);
    delayMicroseconds(motorDelay);
  } 
}

