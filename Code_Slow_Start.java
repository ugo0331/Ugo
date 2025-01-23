import processing.serial.*;

Serial arduino;

// Pages
String currentPage = "login"; // La page par défaut est la connexion
String selectedProduction = "";

// Dimensions des sliders et boutons
int sliderWidth, sliderHeight;
int buttonWidth, buttonHeight;
int sliderSpacing;
int buttonSpacing;

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

// Sliders
int[] potValues = {0, 0, 0, 0};
String receivedData = "";

// Logo (remplacez par votre image si disponible)
PImage logo;

void setup() {
  fullScreen();

  // Chargement du logo
  logo = loadImage("logo-wkw-automotive.jpg"); // Assurez-vous que le fichier "logo-wkw-automotive.jpg" est dans le dossier "data"

  // Dimensions des éléments
  sliderWidth = int(width * 0.6);
  sliderHeight = int(height * 0.04);
  sliderSpacing = int(height * 0.12);
  buttonWidth = int(width * 0.3);
  buttonHeight = int(height * 0.08);
  buttonSpacing = int(height * 0.15); // Espacement vertical entre les boutons

  // Initialisation des boutons
  loginButton = new PButton("Se connecter", width / 2 - buttonWidth / 2, height / 2, buttonWidth, buttonHeight, color(0, 150, 255));
  logoutButton = new PButton("Se déconnecter", width - buttonWidth - 20, height - buttonHeight - 20, buttonWidth, buttonHeight, color(200, 100, 0));
  productionButton = new PButton("Production", width / 2 - buttonWidth - 20, height / 2, buttonWidth, buttonHeight, color(0, 150, 255));
  defaultButton = new PButton("Défaut", width / 2 + 20, height / 2, buttonWidth, buttonHeight, color(255, 100, 0));
  backButton = new PButton("Retour", 20, height - buttonHeight - 20, buttonWidth, buttonHeight, color(200, 200, 200));

  production1Button = new PButton("Production 1", width / 2 - buttonWidth / 2, height / 3, buttonWidth, buttonHeight, color(0, 200, 200));
  production2Button = new PButton("Production 2", width / 2 - buttonWidth / 2, height / 3 + buttonSpacing - buttonHeight / 2, buttonWidth, buttonHeight, color(0, 200, 100));
  production3Button = new PButton("Production 3", width / 2 - buttonWidth / 2, height / 3 + 2 * buttonSpacing - buttonHeight / 2, buttonWidth, buttonHeight, color(200, 100, 0));

  activateButton = new PButton("Activer Relais", width / 4 - buttonWidth / 2, height - buttonHeight * 3, buttonWidth, buttonHeight, color(0, 200, 0));
  deactivateButton = new PButton("Désactiver Relais", (3 * width) / 4 - buttonWidth / 2, height - buttonHeight * 3, buttonWidth, buttonHeight, color(200, 0, 0));

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
  text("Date : " + day() + "/" + month() + "/" + year(), 20, height / 10);
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
  text("Utilisateur :", width / 2 - buttonWidth / 2, height / 2 - buttonHeight * 2);
  fill(0);
  text(username, width / 2 - buttonWidth / 2, height / 2 - buttonHeight * 1.5);

  fill(0);
  if (!isUsernameFocused) {
    fill(0, 150, 255); // Couleur de focus
  }
  text("Mot de passe :", width / 2 - buttonWidth / 2, height / 2 - buttonHeight);
  fill(0);
  text(password.replaceAll(".", "*"), width / 2 - buttonWidth / 2, height / 2 - buttonHeight * 0.5);

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
      if (username.equals("administrateur") && password.equals("1234")) {
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
      }
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
  text("Mode " + selectedProduction, width / 2, height / 10);

  for (int i = 0; i < 4; i++) {
    int sliderX = width / 2 - sliderWidth / 2;
    int sliderY = height / 4 + i * sliderSpacing;
    drawSlider(sliderX, sliderY, i);
  }

  if (isAdmin) {
    activateButton.display();
    deactivateButton.display();
  }

  backButton.display();
}

void drawDefaultPage() {
  textSize(height / 30);
  fill(0);
  textAlign(CENTER);
  text("Page Défaut", width / 2, height / 10);

  backButton.display();
}

void drawSlider(int x, int y, int index) {
  fill(200);
  rect(x, y, sliderWidth, sliderHeight, sliderHeight / 2);
  fill(100, 200, 100);
  rect(x, y, sliderWidth * potValues[index] / 255, sliderHeight, sliderHeight / 2);
  fill(0);
  textSize(height / 50);
  textAlign(CENTER);
  text("Pot " + (index + 1) + ": " + potValues[index], x + sliderWidth / 2, y - 10);
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
