#define AIN1 7 // INA1 3RD RIGHT
#define AIN2 6 // INA2 2ND RIGHT
#define PWMA 5 // PWMA 1ST RIGHT

#define dirPinOne 2
#define dirPinTwo 3
#define stepPinOne 4
#define stepPinTwo 9

#define TRIG_PIN 10
#define ECHO_PIN 11

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
  if (Serial.available()) {
    String command = Serial.readString();
    command.trim();

    if (command == "Classification Done") {
        dropItem();

        if (true) { // replace with ultrasonic fill level checker function 
          compress();
        }
        Serial.println("Function Executed!");
        // Serial.flush();
    }
  }
}

void dropItem() {
  digitalWrite(AIN1, HIGH);
  digitalWrite(AIN2, LOW);
  for (int speed = 64; speed <= 255; speed += 10) {
    analogWrite(PWMA, speed);
    delay(50);
  }

  delay(500);

  digitalWrite(AIN1, LOW);
  digitalWrite(AIN2, LOW);
  analogWrite(PWMA, 0);

  delay(1000);

  digitalWrite(AIN1, LOW);
  digitalWrite(AIN2, HIGH);
  analogWrite(PWMA, 255);

  delay(500);

  digitalWrite(AIN1, LOW);
  digitalWrite(AIN2, LOW);
  analogWrite(PWMA, 0);
}

void compress() {
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