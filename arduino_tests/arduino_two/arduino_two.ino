#define AIN1 7 // INA1 3RD RIGHT
#define AIN2 6 // INA2 2ND RIGHT
#define PWMA 5 // PWMA 1ST RIGHT

void setup() {
  pinMode(AIN1, OUTPUT);
  pinMode(AIN2, OUTPUT);
  pinMode(PWMA, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  if (Serial.available()) {
    String command = Serial.readString();
    command.trim();

    if (command == "Classification Done") {
        dropItem();
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

  Serial.println("Function Executed!");
  Serial.flush();
}