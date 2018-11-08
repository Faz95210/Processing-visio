package app.video;

import ddf.minim.*;
import ddf.minim.analysis.*;
import processing.core.PApplet;


public class Animations extends PApplet {

    private AudioPlayer song;
    private FFT fft;

    private float specHi = 0.20f;   // 20%

// Il reste donc 64% du spectre possible qui ne sera pas utilisé.
// Ces valeurs sont généralement trop hautes pour l'oreille humaine de toute facon.

    // Valeurs de score pour chaque zone
    private float scoreLow = 0;
    private float scoreMid = 0;
    private float scoreHi = 0;

    // Cubes qui apparaissent dans l'espace
    private int nbCubes;
    private Cube[] cubes;

    //Lignes qui apparaissent sur les cotés
    private int nbMurs = 500;
    private Mur[] murs;

    public Animations() {

    }

    public void setVolume(final float volume) {
        song.setGain(volume);
    }

    @Override
    public void setup() {
//        fullScreen(P3D);
        //Charger la librairie minim
        this.savePath(".");
        Minim minim = new Minim(this);

        //Charger la chanson
        song = minim.loadFile("song.mp3");
        //Créer l'objet FFT pour analyser la chanson
        fft = new FFT(song.bufferSize(), song.sampleRate());

        //Un cube par bande de fréquence
        nbCubes = (int) (fft.specSize() * specHi);
        cubes = new Cube[nbCubes];

        //Autant de murs qu'on veux
        murs = new Mur[nbMurs];

        //Créer tous les objets
        //Créer les objets cubes
        for (int i = 0; i < nbCubes; i++) {
            cubes[i] = new Cube(this);
        }

        //Créer les objets murs
        //Murs gauches
        for (int i = 0; i < nbMurs; i += 4) {
            murs[i] = new Mur(0, height / 2f, 10, height, this);
        }

        //Murs droits
        for (int i = 1; i < nbMurs; i += 4) {
            murs[i] = new Mur(width, height / 2f, 10, height, this);
        }

        //Murs bas
        for (int i = 2; i < nbMurs; i += 4) {
            murs[i] = new Mur(width / 2f, height, width, 10, this);
        }

        //Murs haut
        for (int i = 3; i < nbMurs; i += 4) {
            murs[i] = new Mur(width / 2f, 0, width, 10, this);
        }

        //Fond noir

        //Commencer la chanson
//        song.play(0);
    }

    @Override
    public void settings() {
//        song.play(0);
//        fullScreen(P3D);
        fullScreen(P3D);
//        song.loop();
        //        background(0);
    }

    @Override
    public void draw() {
        //Faire avancer la chanson. On draw() pour chaque "frame" de la chanson...
        if (!song.isPlaying()) {
            song.play(0);
        }
        fft.forward(song.mix);
        final float volume = song.getGain();

        //Calcul des "scores" (puissance) pour trois catégories de son
        //D'abord, sauvgarder les anciennes valeurs
        // Valeur précédentes, pour adoucir la reduction
        float oldScoreLow = scoreLow;
        float oldScoreMid = scoreMid;
        float oldScoreHi = scoreHi;

        //Réinitialiser les valeurs
        scoreLow = 0;
        scoreMid = 0;
        scoreHi = 0;

        //Calculer les nouveaux "scores"
        // Variables qui définissent les "zones" du spectre
        // Par exemple, pour les basses, on prend seulement les premières 4% du spectre total
        // 3%
        float specLow = 0.03f;
        for (int i = 0; i < fft.specSize() * specLow; i++) {
            scoreLow += fft.getBand(i);
        }

        // 12.5%
        float specMid = 0.125f;
        for (int i = (int) (fft.specSize() * specLow); i < fft.specSize() * specMid; i++) {
            scoreMid += fft.getBand(i);
        }

        for (int i = (int) (fft.specSize() * specMid); i < fft.specSize() * specHi; i++) {
            scoreHi += fft.getBand(i);
        }


        //Faire ralentir la descente.
        // Valeur d'adoucissement
        float scoreDecreaseRate = 25;
        if (oldScoreLow > scoreLow) {
            scoreLow = oldScoreLow - scoreDecreaseRate;
        }

        if (oldScoreMid > scoreMid) {
            scoreMid = oldScoreMid - scoreDecreaseRate;
        }

        if (oldScoreHi > scoreHi) {
            scoreHi = oldScoreHi - scoreDecreaseRate;
        }

        //Volume pour toutes les fréquences à ce moment, avec les sons plus haut plus importants.
        //Cela permet à l'animation d'aller plus vite pour les sons plus aigus, qu'on remarque plus
        float scoreGlobal = 0.66f * scoreLow + 0.8f * scoreMid + 1 * scoreHi;

        //Couleur subtile de background
        background(scoreLow / 100, scoreMid / 100, scoreHi / 100);

        //Cube pour chaque bande de fréquence
        for (int i = 0; i < nbCubes; i++) {
            //Valeur de la bande de fréquence
            float bandValue = fft.getBand(i);

            //La couleur est représentée ainsi: rouge pour les basses, vert pour les sons moyens et bleu pour les hautes.
            //L'opacité est déterminée par le volume de la bande et le volume global.
            cubes[i].display(scoreLow, scoreMid, scoreHi, bandValue, scoreGlobal);
        }

        //Murs lignes, ici il faut garder la valeur de la bande précédent et la suivante pour les connecter ensemble
        float previousBandValue = fft.getBand(0);

        //Distance entre chaque point de ligne, négatif car sur la dimension z
        float dist = -25;

        //Multiplier la hauteur par cette constante
        float heightMult = 2;

        //Pour chaque bande
        for (int i = 1; i < fft.specSize(); i++) {
            //Valeur de la bande de fréquence, on multiplie les bandes plus loins pour qu'elles soient plus visibles.
            float bandValue = fft.getBand(i) * (1 + (i / 50f));

            //Selection de la couleur en fonction des forces des différents types de sons
            stroke(100 + scoreLow, 100 + scoreMid, 100 + scoreHi, 255 - i);
            strokeWeight(1 + (scoreGlobal / 100));

            //ligne inferieure gauche
            line(0, height - (previousBandValue * heightMult), dist * (i - 1), 0, height - (bandValue * heightMult), dist * i);
            line((previousBandValue * heightMult), height, dist * (i - 1), (bandValue * heightMult), height, dist * i);
            line(0, height - (previousBandValue * heightMult), dist * (i - 1), (bandValue * heightMult), height, dist * i);

            //ligne superieure gauche
            line(0, (previousBandValue * heightMult), dist * (i - 1), 0, (bandValue * heightMult), dist * i);
            line((previousBandValue * heightMult), 0, dist * (i - 1), (bandValue * heightMult), 0, dist * i);
            line(0, (previousBandValue * heightMult), dist * (i - 1), (bandValue * heightMult), 0, dist * i);

            //ligne inferieure droite
            line(width, height - (previousBandValue * heightMult), dist * (i - 1), width, height - (bandValue * heightMult), dist * i);
            line(width - (previousBandValue * heightMult), height, dist * (i - 1), width - (bandValue * heightMult), height, dist * i);
            line(width, height - (previousBandValue * heightMult), dist * (i - 1), width - (bandValue * heightMult), height, dist * i);

            //ligne superieure droite
            line(width, (previousBandValue * heightMult), dist * (i - 1), width, (bandValue * heightMult), dist * i);
            line(width - (previousBandValue * heightMult), 0, dist * (i - 1), width - (bandValue * heightMult), 0, dist * i);
            line(width, (previousBandValue * heightMult), dist * (i - 1), width - (bandValue * heightMult), 0, dist * i);

            //Sauvegarder la valeur pour le prochain tour de boucle
            previousBandValue = bandValue;
        }

        //Murs rectangles
        for (int i = 0; i < nbMurs; i++) {
            //On assigne à chaque mur une bande, et on lui envoie sa force.
            float intensity = fft.getBand(i % ((int) (fft.specSize() * specHi)));
            murs[i].display(scoreLow, scoreMid, scoreHi, intensity, scoreGlobal);
        }
    }
}

//Classe pour les cubes qui flottent dans l'espace
class Cube {
    //Position Z de "spawn" et position Z maximale
    private float startingZ = -10000;
    private float maxZ = 1000;

    //Valeurs de positions
    private float x, y, z;
    private float rotX, rotY, rotZ;
    private float sumRotX, sumRotY, sumRotZ;
    private final PApplet sketch;

    //Constructeur
    Cube(final PApplet sketch) {
        this.sketch = sketch;
        //Faire apparaitre le cube à un endroit aléatoire
        x = sketch.random(0, sketch.width);
        y = sketch.random(0, sketch.height);
        z = sketch.random(startingZ, maxZ);

        //Donner au cube une rotation aléatoire
        rotX = sketch.random(0, 1);
        rotY = sketch.random(0, 1);
        rotZ = sketch.random(0, 1);
    }

    void display(float scoreLow, float scoreMid, float scoreHi, float intensity, float scoreGlobal) {
        //Sélection de la couleur, opacité déterminée par l'intensité (volume de la bande)
//        color displayColor = sketch.color(scoreLow * 0.67f, scoreMid * 0.67f, scoreHi * 0.67f, intensity * 5f);
        sketch.fill(sketch.color(scoreLow * 0.67f, scoreMid * 0.67f, scoreHi * 0.67f, intensity * 5f), 255);

        //Couleur lignes, elles disparaissent avec l'intensité individuelle du cube
//        color strokeColor = color(255, 150 - (20 * intensity));
        sketch.stroke(sketch.color(255, 150 - (20 * intensity)));
        sketch.strokeWeight(1 + (scoreGlobal / 300));

        //Création d'une matrice de transformation pour effectuer des rotations, agrandissements
        sketch.pushMatrix();

        //Déplacement
        sketch.translate(x, y, z);

        //Calcul de la rotation en fonction de l'intensité pour le cube
        sumRotX += intensity * (rotX / 1000);
        sumRotY += intensity * (rotY / 1000);
        sumRotZ += intensity * (rotZ / 1000);

        //Application de la rotation
        sketch.rotateX(sumRotX);
        sketch.rotateY(sumRotY);
        sketch.rotateZ(sumRotZ);

        //Création de la boite, taille variable en fonction de l'intensité pour le cube
        sketch.box(100 + (intensity / 2));

        //Application de la matrice
        sketch.popMatrix();

        //Déplacement Z
        z += (1 + (intensity / 5) + (PApplet.pow((scoreGlobal / 150), 2)));

        //Replacer la boite à l'arrière lorsqu'elle n'est plus visible
        if (z >= maxZ) {
            x = sketch.random(0, sketch.width);
            y = sketch.random(0, sketch.height);
            z = startingZ;
        }
    }
}


//Classe pour afficher les lignes sur les cotés
class Mur {
    //Position minimale et maximale Z
    private float startingZ = -10000;
    private float maxZ = 50;

    //Valeurs de position
    private float x, y, z;
    private float sizeX, sizeY;
    private final PApplet sketch;

    //Constructeur
    Mur(float x, float y, float sizeX, float sizeY, final PApplet sketch) {
        //Faire apparaitre la ligne à l'endroit spécifié
        this.x = x;
        this.y = y;
        //Profondeur aléatoire
        this.z = sketch.random(startingZ, maxZ);

        //On détermine la taille car les murs au planchers ont une taille différente que ceux sur les côtés
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sketch = sketch;
    }

    //Fonction d'affichage
    void display(float scoreLow, float scoreMid, float scoreHi, float intensity, float scoreGlobal) {
        //Couleur déterminée par les sons bas, moyens et élevé
        //Opacité déterminé par le volume global
//        color displayColor = sketch.color(scoreLow * 0.67, scoreMid * 0.67, scoreHi * 0.67, scoreGlobal);

        //Faire disparaitre les lignes au loin pour donner une illusion de brouillard
        sketch.fill(sketch.color(scoreLow * 0.67f, scoreMid * 0.67f, scoreHi * 0.67f, scoreGlobal), ((scoreGlobal - 5) / 1000) * (255 + (z / 25)));
        sketch.noStroke();

        //Première bande, celle qui bouge en fonction de la force
        //Matrice de transformation
        sketch.pushMatrix();

        //Déplacement
        sketch.translate(x, y, z);

        //Agrandissement
        if (intensity > 100) intensity = 100;
        sketch.scale(sizeX * (intensity / 100), sizeY * (intensity / 100), 20);

        //Création de la "boite"
        sketch.box(1);
        sketch.popMatrix();

        //Deuxième bande, celle qui est toujours de la même taille
//        displayColor = sketch.color(scoreLow * 0.5f, scoreMid * 0.5f, scoreHi * 0.5f, scoreGlobal);
        sketch.fill(sketch.color(scoreLow * 0.5f, scoreMid * 0.5f, scoreHi * 0.5f, scoreGlobal), (scoreGlobal / 5000) * (255 + (z / 25)));
        //Matrice de transformation
        sketch.pushMatrix();

        //Déplacement
        sketch.translate(x, y, z);

        //Agrandissement
        sketch.scale(sizeX, sizeY, 10);

        //Création de la "boite"
        sketch.box(1);
        sketch.popMatrix();

        //Déplacement Z
        z += (PApplet.pow((scoreGlobal / 150), 2));
        if (z >= maxZ) {
            z = startingZ;
        }
    }
}