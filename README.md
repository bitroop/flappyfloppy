```markdown
# 🐦 Flappy Bird — Java Edition

A fun and lightweight **Flappy Bird clone built entirely in Java (Swing + AWT)**.  
This project demonstrates 2D animation, event handling, collision detection, and resource management — all coded from scratch.

---

## 🎮 Features

- 🐤 Classic Flappy Bird gameplay — click or press **Space** to flap
- 🌄 Multiple background themes (Underwater, Space, etc.)
- 🚧 Moving pipes and obstacles
- 🔰 Power-ups (Shield, Slow Motion)
- 🎯 Smooth motion with Java Timers
- 🖼️ Custom PNG resources for visuals

---

## 🗂️ Project Structure

```

Flappy-Bird-Java/
│
├── App.java                # Main class with entry point
├── FlappyBird.java         # Core game logic, graphics, and bird control
│
├── resources/              # Game assets (images)
│   ├── flappybird.png
│   ├── flappybirdbg.png
│   ├── toppipe.png
│   ├── bottompipe.png
│   ├── spacebg.png
│   ├── spacebird.png
│   ├── underwaterbg.png
│   ├── seaweed_top.png
│   ├── seaweed_bottom.png
│   ├── shield_icon.png
│   └── slowmo_icon.png
│
├── manifest.txt            # Manifest file for .jar
└── FlappyBird.jar          # Runnable JAR file (optional)

````

---

## ⚙️ How to Run the Game

### 🧩 Option 1 — Run from Java Files

1. Open a terminal inside the project folder.
2. Compile the files:
   ```bash
   javac App.java FlappyBird.java
````

3. Run the game:

   ```bash
   java App
   ```

---

### 📦 Option 2 — Run from a JAR File

1. Create a `manifest.txt` file containing:

   ```text
   Main-Class: App
   Class-Path: .
   ```

   *(Make sure to include a blank line at the end of the file!)*

2. Compile all Java files (including inner classes):

   ```bash
   javac App.java FlappyBird.java
   ```

3. Create a JAR file:

   ```bash
   jar cfm FlappyBird.jar manifest.txt App.class FlappyBird*.class resources/
   ```

4. Verify contents:

   ```bash
   jar tf FlappyBird.jar
   ```

   It should show:

   ```
   META-INF/
   App.class
   FlappyBird.class
   FlappyBird$Bird.class
   resources/
   ```

5. Run the game:

   ```bash
   java -jar FlappyBird.jar
   ```

## 🧠 Concepts Used

* Java Swing & AWT for 2D graphics
* Event handling (keyboard & mouse)
* Game loop and animation timers
* Collision detection logic
* Resource loading from JAR classpath
* Packaging executable JAR files

---

## 🖼️ Screenshots

*(Add screenshots here later — e.g., gameplay, start screen, etc.)*

---

## 👨‍💻 Author

**Roopesh Singhal**
🎓 *SRM IST, Delhi NCR*
💻 Passionate about Java, Game Dev, and Creative Coding.

---

## 📜 License

This project is open-source and free to use for educational and personal use.
Feel free to modify, share, or improve upon it! 🚀

---

## ⭐ How to Contribute

If you’d like to improve this project:

1. Fork the repository
2. Create a new branch (`feature-xyz`)
3. Commit your changes
4. Push and open a Pull Request

---

### 🏁 Quick Summary

```bash
# Compile
javac App.java FlappyBird.java

# Run directly
java App

# OR Package & Run as JAR
jar cfm FlappyBird.jar manifest.txt App.class FlappyBird*.class resources/
java -jar FlappyBird.jar
```

Enjoy playing! 🕹️🐦

```
```
