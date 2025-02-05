import processing.serial.*;
import processing.data.*;
import java.util.*;

Serial arduino;

// Pages
String currentPage = "login"; // La page par défaut est la connexion
String selectedProduction = "";

// Dimensions des sliders et boutons
int sliderWidth, sliderHeight;
int buttonWidth, buttonHeight;
int sliderSpacing;
int buttonSpacing;

// Positions dynamiques
float slidersStartY;
float slidersEndY;
float buttonsStartY;
float graphStartY;

// Champs de texte pour la connexion
String username = "";
String password = "";
boolean loggedIn = false; // Statut de connexion
boolean isAdmin = false; // Vérifie si l'utilisateur est un administrateur
boolean isUsernameFocused = true; // Gère le focus entre les champs utilisateur et mot de passe

// Boutons pour la navigation
PButton loginButton, logoutButton;
PButton productionButton, defaultButton, backButton;
PButton production1Button, production2Button, production3Button;
PButton activateButton, deactivateButton;
PButton saveButton, loadButton; // Boutons pour sauvegarde et chargement

// Sliders
ArrayList<CustomSlider> sliders;
String[] sliderLabels = {"Vitesse 1", "Vitesse 2", "Vitesse 3", "Vitesse 4"};
float maxSpeed = 10.0; // Vitesse maximale en m/s

// Graphiques en temps réel
RealTimeGraph realTimeGraph;

// Seuil pour les notifications visuelles
float criticalSpeed = 8.0; // Seuil critique en m/s

// Logo (remplacez par votre image si disponible)
PImage logo;

void setup() {
  fullScreen();

  // Chargement du logo
  logo = loadImage("logo-wkw-automotive.jpg"); // Assurez-vous que le fichier est dans le dossier "data"

  // Dimensions des éléments
  sliderWidth = int(width * 0.6);
  sliderHeight = int(height * 0.04);
  sliderSpacing = int(height * 0.08); // Ajustement de l'espacement entre les sliders
  buttonWidth = int(width * 0.2);
  buttonHeight = int(height * 0.08);
  buttonSpacing = int(height * 0.02); // Espacement vertical entre les boutons

  // Position de départ des sliders
  slidersStartY = height / 6;

  // Initialisation des sliders
  sliders = new ArrayList<CustomSlider>();
  for (int i = 0; i < sliderLabels.length; i++) {
    float sliderY = slidersStartY + i * sliderSpacing;
    sliders.add(new CustomSlider(sliderLabels[i], width / 2 - sliderWidth / 2, sliderY, sliderWidth, sliderHeight, maxSpeed));
  }

  // Calcul des positions dynamiques après l'initialisation des sliders
  slidersEndY = slidersStartY + (sliderLabels.length - 1) * sliderSpacing + sliderHeight;
  buttonsStartY = slidersEndY + int(height * 0.05);

  // Initialisation des boutons
  loginButton = new PButton("Se connecter", width / 2 - buttonWidth / 2, height / 2, buttonWidth, buttonHeight, color(0, 150, 255));
  logoutButton = new PButton("Se déconnecter", width - buttonWidth - 20, 20, buttonWidth, buttonHeight, color(200, 100, 0));
  backButton = new PButton("Retour", width - buttonWidth - 20, 20, buttonWidth, buttonHeight, color(200, 200, 200));

  productionButton = new PButton("Production", width / 2 - buttonWidth - 20, height / 2, buttonWidth, buttonHeight, color(0, 150, 255));
  defaultButton = new PButton("Défaut", width / 2 + 20, height / 2, buttonWidth, buttonHeight, color(255, 100, 0));

  production1Button = new PButton("Production 1", width / 2 - buttonWidth / 2, height / 3, buttonWidth, buttonHeight, color(0, 200, 200));
  production2Button = new PButton("Production 2", width / 2 - buttonWidth / 2, height / 3 + buttonSpacing + buttonHeight, buttonWidth, buttonHeight, color(0, 200, 100));
  production3Button = new PButton("Production 3", width / 2 - buttonWidth / 2, height / 3 + 2 * (buttonSpacing + buttonHeight), buttonWidth, buttonHeight, color(200, 100, 0));

  activateButton = new PButton("Activer Relais", width / 4 - buttonWidth / 2, buttonsStartY, buttonWidth, buttonHeight, color(0, 200, 0));
  deactivateButton = new PButton("Désactiver Relais", (3 * width) / 4 - buttonWidth / 2, buttonsStartY, buttonWidth, buttonHeight, color(200, 0, 0));

  saveButton = new PButton("Sauvegarder", width / 2 - buttonWidth - 20, buttonsStartY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, color(100, 100, 255));
  loadButton = new PButton("Charger", width / 2 + 20, buttonsStartY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, color(100, 255, 100));

  // Positionnement du graphique en temps réel
  graphStartY = buttonsStartY + 2 * (buttonHeight + buttonSpacing) + int(height * 0.05);
  realTimeGraph = new RealTimeGraph(width / 2 - sliderWidth / 2, graphStartY, sliderWidth, height - graphStartY - int(height * 0.05));

  // Initialisation du port série
  try {
    arduino = new Serial(this, Serial.list()[0], 115200);
  } catch (Exception e) {
    println("Erreur : Aucun port série détecté !");
  }
}

void draw() {
  background(240);

  // Afficher le logo, la date et l'heure sur toutes les pages
  drawHeader();

  if (currentPage.equals("login")) {
    drawLoginPage();
  } else if (currentPage.equals("home")) {
    drawHomePage();
  } else if (currentPage.equals("production")) {
    drawProductionMenu();
  } else if (currentPage.equals("sliders")) {
    drawSlidersPage();
  } else if (currentPage.equals("default")) {
    drawDefaultPage();
  }
}

void drawHeader() {
  // Afficher le logo en haut à droite
  if (logo != null) {
    image(logo, width - width / 6 - 20, 20, width / 6, height / 10);
  }

  // Afficher la date et l'heure
  textSize(height / 30);
  fill(0);
  textAlign(LEFT);
  text("Date : " + nf(day(), 2) + "/" + nf(month(), 2) + "/" + year(), 20, height / 10);
  text("Heure : " + nf(hour(), 2) + ":" + nf(minute(), 2) + ":" + nf(second(), 2), 20, height / 10 + height / 30);
}

void drawLoginPage() {
  // Afficher le formulaire de connexion
  textSize(height / 30);
  fill(0);
  textAlign(CENTER);
  text("Connexion requise", width / 2, height / 4);

  // Saisie de l'utilisateur
  textAlign(LEFT);
  fill(0);
  if (isUsernameFocused) {
    fill(0, 150, 255); // Couleur de focus
  }
  text("Utilisateur :", width / 2 - buttonWidth, height / 2 - buttonHeight * 2);
  fill(0);
  text(username, width / 2 - buttonWidth, height / 2 - buttonHeight * 1.5);

  fill(0);
  if (!isUsernameFocused) {
    fill(0, 150, 255); // Couleur de focus
  }
  text("Mot de passe :", width / 2 - buttonWidth, height / 2 - buttonHeight);
  fill(0);
  text(password.replaceAll(".", "*"), width / 2 - buttonWidth, height / 2 - buttonHeight * 0.5);

  loginButton.display();
}

void keyPressed() {
  if (currentPage.equals("login")) {
    if (key == BACKSPACE) {
      if (isUsernameFocused && username.length() > 0) {
        username = username.substring(0, username.length() - 1);
      } else if (!isUsernameFocused && password.length() > 0) {
        password = password.substring(0, password.length() - 1);
      }
    } else if (key == TAB) {
      isUsernameFocused = !isUsernameFocused; // Alterner le focus entre utilisateur et mot de passe
    } else if (key == ENTER || key == RETURN) {
      if (username.equals("admin") && password.equals("1234")) {
        loggedIn = true;
        isAdmin = true;
        currentPage = "home";
      } else if (username.equals("operateur") && password.equals("0000")) {
        loggedIn = true;
        isAdmin = false;
        currentPage = "home";
      } else {
        println("Identifiants incorrects !");
        username = "";
        password = "";
      }
    } else {
      if (isUsernameFocused) {
        username += key;
      } else {
        password += key;
      }
    }
  }
}

void mousePressed() {
  if (currentPage.equals("home")) {
    if (productionButton.isPressed(mouseX, mouseY)) {
      currentPage = "production";
    } else if (defaultButton.isPressed(mouseX, mouseY)) {
      currentPage = "default";
    } else if (logoutButton.isPressed(mouseX, mouseY)) {
      loggedIn = false;
      username = "";
      password = "";
      currentPage = "login";
    }
  } else if (currentPage.equals("production")) {
    if (production1Button.isPressed(mouseX, mouseY)) {
      selectedProduction = "Production 1";
      currentPage = "sliders";
    } else if (production2Button.isPressed(mouseX, mouseY)) {
      selectedProduction = "Production 2";
      currentPage = "sliders";
    } else if (production3Button.isPressed(mouseX, mouseY)) {
      selectedProduction = "Production 3";
      currentPage = "sliders";
    } else if (backButton.isPressed(mouseX, mouseY)) {
      currentPage = "home";
    }
  } else if (currentPage.equals("sliders")) {
    if (backButton.isPressed(mouseX, mouseY)) {
      currentPage = "production";
    } else if (isAdmin) {
      if (activateButton.isPressed(mouseX, mouseY)) {
        arduino.write("marche\n");
      } else if (deactivateButton.isPressed(mouseX, mouseY)) {
        arduino.write("arret\n");
      } else if (saveButton.isPressed(mouseX, mouseY)) {
        saveConfiguration();
      } else if (loadButton.isPressed(mouseX, mouseY)) {
        loadConfiguration();
      }
      // Interaction avec les sliders pour l'admin
      for (CustomSlider slider : sliders) {
        slider.mousePressed(mouseX, mouseY);
      }
    }
  } else if (currentPage.equals("default")) {
    if (backButton.isPressed(mouseX, mouseY)) {
      currentPage = "home";
    }
  }
}

void mouseDragged() {
  if (currentPage.equals("sliders") && isAdmin) {
    for (CustomSlider slider : sliders) {
      slider.mouseDragged(mouseX, mouseY);
    }
  }
}

void mouseReleased() {
  if (currentPage.equals("sliders") && isAdmin) {
    for (CustomSlider slider : sliders) {
      slider.mouseReleased();
    }
  }
}

void drawHomePage() {
  if (!loggedIn) {
    currentPage = "login";
    return;
  }

  textSize(height / 30);
  textAlign(CENTER);
  fill(0);
  text(isAdmin ? "Mode Administrateur" : "Mode Opérateur", width / 2, height / 8);

  // Afficher les boutons
  productionButton.display();
  defaultButton.display();
  logoutButton.display();
}

void drawProductionMenu() {
  textSize(height / 30);
  fill(0);
  textAlign(CENTER);
  text("Menu Production", width / 2, height / 10);

  production1Button.display();
  production2Button.display();
  production3Button.display();
  backButton.display();
}

void drawSlidersPage() {
  if (!loggedIn) {
    currentPage = "login";
    return;
  }

  textSize(height / 30);
  fill(0);
  textAlign(CENTER);
  text("Mode " + selectedProduction, width / 2, height / 12);

  for (CustomSlider slider : sliders) {
    slider.display();
  }

  if (isAdmin) {
    activateButton.display();
    deactivateButton.display();
    saveButton.display();
    loadButton.display();
  }

  backButton.display();

  // Mise à jour du graphique en temps réel
  realTimeGraph.update(sliders);
  realTimeGraph.display();
}

void drawDefaultPage() {
  textSize(height / 30);
  fill(0);
  textAlign(CENTER);
  text("Page Défaut", width / 2, height / 10);

  backButton.display();
}

void saveConfiguration() {
  if (isAdmin) {
    JSONObject json = new JSONObject();
    for (CustomSlider slider : sliders) {
      json.setFloat(slider.label, slider.value);
    }
    saveJSONObject(json, "configuration.json");
    println("Configuration sauvegardée.");
  }
}

void loadConfiguration() {
  if (isAdmin) {
    JSONObject json = loadJSONObject("configuration.json");
    if (json != null) {
      for (CustomSlider slider : sliders) {
        slider.value = json.getFloat(slider.label, 0);
      }
      println("Configuration chargée.");
    } else {
      println("Aucune configuration trouvée.");
    }
  }
}

// Classe pour les sliders personnalisés
class CustomSlider {
  String label;
  float x, y, w, h;
  float maxValue;
  float value;
  boolean dragging = false;

  CustomSlider(String label, float x, float y, float w, float h, float maxValue) {
    this.label = label;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.maxValue = maxValue;
    this.value = 0;
  }

  void display() {
    // Barre de fond du slider
    fill(200);
    rect(x, y, w, h, h / 2);

    // Vérifier si la valeur dépasse le seuil critique
    if (value >= criticalSpeed) {
      fill(255, 0, 0); // Rouge pour alerte
    } else {
      fill(100, 200, 100); // Vert par défaut
    }

    // Barre de valeur du slider
    float sliderPos = map(value, 0, maxValue, 0, w);
    rect(x, y, sliderPos, h, h / 2);

    // Affichage du label et de la valeur en m/s
    fill(0);
    textSize(height / 50);
    textAlign(LEFT);
    text(label + " : " + nf(value, 1, 2) + " m/s", x, y - 10);
  }

  void mousePressed(float mx, float my) {
    if (isAdmin) {
      if (mx > x && mx < x + w && my > y && my < y + h) {
        dragging = true;
        updateValue(mx);
      }
    }
  }

  void mouseDragged(float mx, float my) {
    if (isAdmin && dragging) {
      updateValue(mx);
    }
  }

  void mouseReleased() {
    if (isAdmin) {
      dragging = false;
    }
  }

  void updateValue(float mx) {
    value = map(mx, x, x + w, 0, maxValue);
    value = constrain(value, 0, maxValue);
    // Envoyer la valeur au port série
    if (arduino != null) {
      arduino.write(label + ":" + nf(value, 1, 2) + "\n");
    }
  }
}

// Classe pour les boutons
class PButton {
  String label;
  float x, y, w, h;
  int buttonColor;

  PButton(String label, float x, float y, float w, float h, int buttonColor) {
    this.label = label;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.buttonColor = buttonColor;
  }

  void display() {
    fill(buttonColor);
    rect(x, y, w, h, h / 2);
    fill(255);
    textAlign(CENTER, CENTER);
    textSize(height / 50);
    text(label, x + w / 2, y + h / 2);
  }

  boolean isPressed(float mx, float my) {
    return mx > x && mx < x + w && my > y && my < y + h;
  }
}

// Classe pour le graphique en temps réel
class RealTimeGraph {
  float x, y, w, h;
  int maxDataPoints = 100;
  ArrayList<float[]> dataHistory;

  RealTimeGraph(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    dataHistory = new ArrayList<float[]>();
  }

  void update(ArrayList<CustomSlider> sliders) {
    float[] currentValues = new float[sliders.size()];
    for (int i = 0; i < sliders.size(); i++) {
      currentValues[i] = sliders.get(i).value;
    }
    dataHistory.add(currentValues);
    if (dataHistory.size() > maxDataPoints) {
      dataHistory.remove(0);
    }
  }

  void display() {
    // Dessiner le cadre du graphique
    stroke(0);
    noFill();
    rect(x, y, w, h);

    // Tracer les données
    if (dataHistory.size() > 1) {
      for (int i = 0; i < sliders.size(); i++) {
        stroke(sliderColor(i));
        noFill();
        beginShape();
        for (int j = 0; j < dataHistory.size(); j++) {
          float[] values = dataHistory.get(j);
          float vx = map(j, 0, maxDataPoints - 1, x, x + w);
          float vy = map(values[i], 0, maxSpeed, y + h, y);
          vertex(vx, vy);
        }
        endShape();
      }
    }

    // Légende des courbes
    float legendX = x + w + 20;
    float legendY = y + 20;
    textSize(height / 50);
    for (int i = 0; i < sliders.size(); i++) {
      fill(sliderColor(i));
      noStroke();
      rect(legendX, legendY + i * 30, 20, 20);
      fill(0);
      textAlign(LEFT, CENTER);
      text(sliders.get(i).label, legendX + 30, legendY + i * 30 + 10);
    }
  }

  int sliderColor(int index) {
    switch (index) {
      case 0:
        return color(255, 0, 0); // Rouge
      case 1:
        return color(0, 255, 0); // Vert
      case 2:
        return color(0, 0, 255); // Bleu
      case 3:
        return color(255, 165, 0); // Orange
      default:
        return color(0); // Noir par défaut
    }
  }
}
