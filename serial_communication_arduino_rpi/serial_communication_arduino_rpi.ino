// ARDUINO A
#include <SoftwareSerial.h>

#define RX_PIN 12  // Pin 12 receives data from Arduino B
#define TX_PIN 11 // Pin 11 sends data to Arduino B

// Create a SoftwareSerial object
SoftwareSerial mySerial(RX_PIN, TX_PIN);


const int stepPin = 3;
const int dirPin = 2; 

const int trigPin = 8;
const int echoPin = 9;



float duration, distance = 999;


void setup() {
  Serial.begin(9600);

  mySerial.begin(9600);
  pinMode(stepPin, OUTPUT);
  pinMode(dirPin, OUTPUT);
  pinMode(trigPin, OUTPUT);
  
  pinMode(echoPin, INPUT);
  //rotateMotor(stepPin, dirPin, 46250, false, 80); // For calibration. If sorter is in the very RIGHT. 46250 is dead center!
  //rotateMotor(stepPin, dirPin, 900000, false, 80); // RESET
  delay(2000);
}

void loop() {
  distance = getTheDistance();
  int classification;
    if (distance <= 10) {
      Serial.print("Detected");
      delay(3000);
    
        // Wait for classification result from Raspberry Pi
    while (Serial.available() == 0) {
        
    }
    classification = Serial.parseInt();  
    
    Serial.print("Classification Received: ");
    Serial.println(classification);
    
    switch (classification) {
      case 0:
        classify(15416, true); // 15416 is half way of half (X: not the belt, Y: BELT HERE IS HERE) [X][Y][X][X]
      break;
      case 1:
        classify(46250, true); // FULL LEFT (X: not the belt, Y: BELT HERE IS HERE) [Y][X]][X][X]
      break;
      case 2:
        classify(46250, false); // false is just the opposite direction
      break;
      case 3:
        classify(15416, false);
        break;
      case 4:
        classify(15416, false);
        break;
      default:
        classify(0, true);
        break;
    }
 }    
}

void rotateMotor(int stepPin, int dirPin, long steps, bool dir, long motorDelay) {
  digitalWrite(dirPin, dir); // simple function, takes a step pin, the direction pin, how many steps, the direction, and the speed.
  for (long i = 0; i < steps; i++) {
    digitalWrite(stepPin, HIGH);
    delayMicroseconds(motorDelay);
    digitalWrite(stepPin, LOW);
    delayMicroseconds(motorDelay);
  } 
}

void classify(long steps, bool dir) {
  rotateMotor(stepPin, dirPin, steps, dir, 80);
  mySerial.println("Classification Done");
  while (!mySerial.available()) { // Do nothing while waiting. If you receive "Function Executed, run the next code."
    
  }

  String response = mySerial.readString();
  response.trim();

  if (response == "Function Executed!") {
    mySerial.println("Received acknowledgment. Continuing...");
  }

  rotateMotor(stepPin, dirPin, steps, !dir, 80); // put it back
  delay(2000);

  float binLevel =  mySerial.readString().toFloat(); //dapat float //sesend sa rpi
  Serial.println(binLevel);
}



int getTheDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  return duration * 0.034 / 2;
}






//Function to send data to firebase



