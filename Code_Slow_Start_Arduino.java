#include <Wire.h>

// Adresse du MUX
#define MUX_ADDRESS 0x70

// Adresse des DS3502
#define DS3502_1 0x28
#define DS3502_2 0x28
#define DS3502_3 0x28
#define DS3502_4 0x28

// Définir les broches des relais
const int relay1 = 8;
const int relay2 = 9;
const int relay3 = 10;
const int relay4 = 11;

// Variables pour stocker les valeurs des potentiomètres
int potValues[4] = {0, 0, 0, 0};

// Fonction pour sélectionner un canal du MUX
void selectMuxChannel(uint8_t channel) {
  if (channel > 7) return; // Le MUX a 8 canaux (0-7)
  Wire1.beginTransmission(MUX_ADDRESS);
  Wire1.write(1 << channel);
  Wire1.endTransmission();
}

// Fonction pour écrire une valeur dans un DS3502
void writePotentiometer(uint8_t address, uint8_t value) {
  Wire1.beginTransmission(address);
  Wire1.write(0x00); // Registre de contrôle pour DS3502
  Wire1.write(value);
  Wire1.endTransmission();
}

void setup() {
  // Initialisation des potentiomètres
  Wire1.begin();

  // Initialisation des relais
  pinMode(relay1, OUTPUT);
  pinMode(relay2, OUTPUT);
  pinMode(relay3, OUTPUT);
  pinMode(relay4, OUTPUT);

  // Initialiser les relais à l'état OFF (HIGH si les relais sont actifs LOW)
  digitalWrite(relay1, HIGH);
  digitalWrite(relay2, HIGH);
  digitalWrite(relay3, HIGH);
  digitalWrite(relay4, HIGH);

  // Communication série pour Processing
  Serial.begin(115200);

  Serial.println("Prêt pour recevoir des commandes.");
}

void loop() {
  // Vérifier si des données arrivent depuis Processing
  if (Serial.available() > 0) {
    String input = Serial.readStringUntil('\n'); // Lire la ligne reçue

    // Commande pour contrôler les potentiomètres
    if (input.startsWith("SET")) {
      int potIndex = input.substring(4, 5).toInt(); // Index du potentiomètre
      int value = input.substring(6).toInt();      // Valeur à définir
      if (potIndex >= 0 && potIndex < 4 && value >= 0 && value <= 255) {
        potValues[potIndex] = value;

        // Sélectionner le canal du MUX et écrire la valeur
        selectMuxChannel(potIndex);
        writePotentiometer(0x28, value);
      }
    }

    // Commande pour activer les relais
    else if (input.equalsIgnoreCase("marche")) {
      digitalWrite(relay1, LOW);
      digitalWrite(relay2, LOW);
      digitalWrite(relay3, LOW);
      digitalWrite(relay4, LOW);
      Serial.println("Relais activés !");
    }

    // Commande pour désactiver les relais
    else if (input.equalsIgnoreCase("arret")) {
      digitalWrite(relay1, HIGH);
      digitalWrite(relay2, HIGH);
      digitalWrite(relay3, HIGH);
      digitalWrite(relay4, HIGH);
      Serial.println("Relais désactivés !");
    }

    // Commande invalide
    else {
      Serial.println("Commande inconnue. Utilisez 'SET', 'marche' ou 'arret'.");
    }
  }

  // Retourner les valeurs actuelles (optionnel)
  Serial.print("CURRENT_VALUES ");
  for (int i = 0; i < 4; i++) {
    Serial.print(potValues[i]);
    if (i < 3) Serial.print(",");
  }
  Serial.println();

  delay(100); // Petit délai pour éviter de saturer la communication
}



