#include <SoftwareSerial.h>
#include <Servo.h> // Librería para el control del servomotor

SoftwareSerial BT(10, 11); // Pines del módulo bluetooth

Servo servo; // Objeto para controlar el servomotor
int motorSpeedPin = 6; // Pin para controlar la velocidad del motor DC
int motorDirectionPin1 = 7; // Pin para controlar la dirección del motor DC (puente H)
int motorDirectionPin2 = 8; // Pin para controlar la dirección del motor DC (puente H)

char CharIN = ' '; // Comando recibido desde el teléfono

void setup()
{
  Serial.begin(9600); // Establecer la comunicación a 9600 baudios
  BT.begin(9600);
  servo.attach(9); // Conectar el servomotor al pin 9
  pinMode(motorSpeedPin, OUTPUT); // Configurar el pin de velocidad del motor como salida
  pinMode(motorDirectionPin1, OUTPUT); // Configurar el pin de dirección 1 del motor como salida
  pinMode(motorDirectionPin2, OUTPUT); // Configurar el pin de dirección 2 del motor como salida
  Serial.println("Iniciando Control ...");
}

void loop()
{
  if (BT.available())
  {
    CharIN = BT.read();
    Serial.print(CharIN);
    ControlDispositivos();
  }
}

void ControlDispositivos()
{
  if (CharIN == 'F') // Mover el servomotor a una posición específica
  {
    int position = BT.parseInt();
    servo.write(position);
  }
  else if (CharIN == 'M') // Controlar la velocidad del motor DC
  {
    int motorSpeed = BT.parseInt();
    analogWrite(motorSpeedPin, motorSpeed);
  }
}
