// Pins for Trash Dispenser arduino B
#define AIN1 7 // INA1 3RD RIGHT
#define AIN2 6 // INA2 2ND RIGHT
#define PWMA 5 // PWMA 1ST RIGHT

// Pins for Compressor
#define dirPinOne 2 
#define dirPinTwo 3
#define stepPinOne 4
#define stepPinTwo 9

// Ultrasonic Pins
#define TRIG_PIN 10
#define ECHO_PIN 11

float binLevel;

float tolerance;
float binHeight;



void setup() {
  pinMode(AIN1, OUTPUT);
  pinMode(AIN2, OUTPUT);
  pinMode(PWMA, OUTPUT);
  pinMode(dirPinOne, OUTPUT);
  pinMode(stepPinOne, OUTPUT);
  pinMode(dirPinTwo, OUTPUT);
  pinMode(stepPinTwo, OUTPUT);
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  Serial.begin(9600);
}

void loop() {
  if (Serial.available()) { // Checks if the serial monitor is not empty
    String command = Serial.readString();
    command.trim();

    if (command == "Classification Done") { 
        dropItem();


        if (true/*getFillLevel >= 90*/) { // replace with ultrasonic fill level checker function 
          compress();
        }

        
        
      
        Serial.println("Function Executed!");
        delay(1000);
        Serial.println(getFillLevel());
        // Serial.flush(); readd this if there are issues with RXTX passing
    }

    
  }
  //dropItem();
  //compress();
}

void dropItem() {
  // Sets the Boolean of the driver motor to 10, which indicates an OPENING motion
  digitalWrite(AIN1, LOW);
  digitalWrite(AIN2, HIGH);

  analogWrite(PWMA, 255);

  delay(2500);

  // Sets the Boolean of the driver motor to 00, which indicates a DO NOTHING
  digitalWrite(AIN1, LOW); 
  digitalWrite(AIN2, LOW);
  analogWrite(PWMA, 0);

  delay(2500);



  // Sets the Boolean of the driver motor to 01, which indicates a CLOSING motion
  digitalWrite(AIN1, HIGH);
  digitalWrite(AIN2, LOW);
  analogWrite(PWMA, 255); // This line means 100% speed.

  delay(2500);

  digitalWrite(AIN1, LOW);
  digitalWrite(AIN2, LOW);
  analogWrite(PWMA, 0);
}

void compress() {
  // Function for compressing, same as the one we tested last time.
  digitalWrite(dirPinOne, true);
  digitalWrite(dirPinTwo, true);
  for (long i = 0; i < 205000; i++) {
    digitalWrite(stepPinOne, HIGH);
    digitalWrite(stepPinTwo, HIGH);
    delayMicroseconds(20);
    digitalWrite(stepPinOne, LOW);
    digitalWrite(stepPinTwo, LOW); 
    delayMicroseconds(20);
  }





  delay(2500);

  digitalWrite(dirPinOne, false);
  digitalWrite(dirPinTwo, false);
  for (long i = 0; i < 205000; i++) {
    digitalWrite(stepPinOne, HIGH);
    digitalWrite(stepPinTwo, HIGH);
    delayMicroseconds(20);
    digitalWrite(stepPinOne, LOW);
    digitalWrite(stepPinTwo, LOW); 
    delayMicroseconds(20);
  }
}

int getTheDistance() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  float duration = pulseIn(ECHO_PIN, HIGH);
  return duration * 0.034 / 2;
}

float getFillLevel() {
  float binFillLevel = getTheDistance();
  float actualFillLevel = binFillLevel - tolerance;
  float fillLevelPercentage = (actualFillLevel / binHeight) * 100;

  return fillLevelPercentage;
}



