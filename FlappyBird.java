import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Game Dimensions & Assets
    int boardWidth = 360, boardHeight = 640;
    Image bgImgDefault, bgImgUnderwater, bgImgSpace, currentBgImg, oldBgImg;
    Image birdImgDefault, fishImg, spaceBirdImg;
    Image pipeTopDefault, pipeBottomDefault, pipeTopSeaweed, pipeBottomSeaweed, pipeTopAsteroid, pipeBottomAsteroid;
    Image currentTopPipeImg, currentBottomPipeImg;
    Image shieldIconImg, slowMoIconImg;

    // Game Object Classes
    class Bird { int x, y, width, height; Image img; Bird(Image img){this.x=birdX; this.y=birdY; this.width=birdWidth; this.height=birdHeight; this.img=img;}}
    // MODIFIED: Removed vertical movement variables from Pipe
    class Pipe { int x, y, width, height; Image img; boolean passed=false; Pipe(Image img){this.x=pipeX; this.y=pipeY; this.width=pipeWidth; this.height=pipeHeight; this.img=img;}}
    class PowerUp { int x, y, width, height; Image img; boolean collected = false; PowerUp(int x, int y, int w, int h, Image img){this.x=x; this.y=y; this.width=w; this.height=h; this.img=img;}}
    
    // Bird & Pipe Properties
    int birdX = boardWidth/8, birdY = boardHeight/2;
    int birdWidth = 44, birdHeight = 31;
    int pipeX = boardWidth, pipeY = 0, pipeWidth = 64, pipeHeight = 512;
    
    // Game Logic Variables
    Bird bird;
    int velocityX;
    double velocityY = 0, gravity;
    ArrayList<Pipe> pipes;
    ArrayList<PowerUp> powerUps;
    Random random = new Random();
    Timer gameLoop, placePipesTimer;
    double score = 0;
    int highScoreEasy = 0, highScoreNormal = 0, highScoreHard = 0;
    boolean shieldActive = false;
    int pipesSinceLastPowerUp = 0;
    int openingSpace;
    
    private enum GameState { SPLASH, HOME, RULES, PLAYING, PAUSED, GAME_OVER }
    private GameState currentState;

    // UI Components
    List<Component> homeScreenUI = new ArrayList<>();
    List<Component> rulesScreenUI = new ArrayList<>();
    List<Component> pauseMenuUI = new ArrayList<>();
    List<Component> gameOverUI = new ArrayList<>();
    JButton onScreenPauseButton;
    String playerName = "Player";

    // Animation Variables
    private double birdAngle = 0.0;
    private Timer transitionTimer, blinkTimer, slowMotionTimer;
    private boolean isTransitioning = false, showRestartText = true, isSlowMotion = false;
    private float transitionAlpha = 0.0f;
    
    public FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null); 

        loadImages();
        bird = new Bird(null); 
        pipes = new ArrayList<>();
        powerUps = new ArrayList<>();
        
        setupUI();
        
        blinkTimer = new Timer(500, e -> showRestartText = !showRestartText);
        placePipesTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

        changeState(GameState.SPLASH);
        Timer splashTimer = new Timer(2000, e -> changeState(GameState.HOME));
        splashTimer.setRepeats(false);
        splashTimer.start();
    }

    private void loadImages() {
        birdImgDefault = new ImageIcon(getClass().getResource("/resources/flappybird.png")).getImage();
        fishImg = new ImageIcon(getClass().getResource("/resources/fish.png")).getImage();
        spaceBirdImg = new ImageIcon(getClass().getResource("/resources/spacebird.png")).getImage();
        bgImgDefault = new ImageIcon(getClass().getResource("/resources/flappybirdbg.png")).getImage();
        bgImgUnderwater = new ImageIcon(getClass().getResource("/resources/underwaterbg.png")).getImage();
        bgImgSpace = new ImageIcon(getClass().getResource("/resources/spacebg.png")).getImage();
        pipeTopDefault = new ImageIcon(getClass().getResource("/resources/toppipe.png")).getImage();
        pipeBottomDefault = new ImageIcon(getClass().getResource("/resources/bottompipe.png")).getImage();
        shieldIconImg = new ImageIcon(getClass().getResource("/resources/shield_icon.png")).getImage();
        slowMoIconImg = new ImageIcon(getClass().getResource("/resources/slowmo_icon.png")).getImage();


        URL seaweedTopUrl = getClass().getResource("/resources/seaweed_top.png");
        pipeTopSeaweed = (seaweedTopUrl != null) ? new ImageIcon(seaweedTopUrl).getImage() : pipeTopDefault;
        URL seaweedBottomUrl = getClass().getResource("/resources/seaweed_bottom.png");
        pipeBottomSeaweed = (seaweedBottomUrl != null) ? new ImageIcon(seaweedBottomUrl).getImage() : pipeBottomDefault;
        URL asteroidTopUrl = getClass().getResource("/resources/asteroid_top.png");
        pipeTopAsteroid = (asteroidTopUrl != null) ? new ImageIcon(asteroidTopUrl).getImage() : pipeTopDefault;
        URL asteroidBottomUrl = getClass().getResource("/resources/asteroid_bottom.png");
        pipeBottomAsteroid = (asteroidBottomUrl != null) ? new ImageIcon(asteroidBottomUrl).getImage() : pipeBottomDefault;
    }

    private void setupUI() {
        JLabel titleLabel = new JLabel("Flappy Bird");
        homeScreenUI.add(titleLabel);

        JTextField nameField = new JTextField("Player");
        nameField.setBounds(boardWidth/2 - 75, 150, 150, 40);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setFont(new Font("Tahoma", Font.BOLD, 20));
        homeScreenUI.add(nameField);

        JButton easyButton = new JButton("Easy");
        easyButton.setBounds(boardWidth/2 - 60, 210, 120, 40);
        easyButton.addActionListener(e -> startGame(nameField.getText(), boardHeight / 3));
        styleButton(easyButton, new Color(96, 151, 75));
        homeScreenUI.add(easyButton);

        JButton normalButton = new JButton("Normal");
        normalButton.setBounds(boardWidth/2 - 60, 260, 120, 40);
        normalButton.addActionListener(e -> startGame(nameField.getText(), boardHeight / 4));
        styleButton(normalButton, new Color(204, 122, 0));
        homeScreenUI.add(normalButton);

        JButton hardButton = new JButton("Hard");
        hardButton.setBounds(boardWidth/2 - 60, 310, 120, 40);
        hardButton.addActionListener(e -> startGame(nameField.getText(), boardHeight / 5));
        styleButton(hardButton, new Color(180, 0, 0));
        homeScreenUI.add(hardButton);
        
        JButton rulesButton = new JButton("How to Play");
        rulesButton.setBounds(boardWidth/2 - 60, 360, 120, 40);
        rulesButton.addActionListener(e -> changeState(GameState.RULES));
        styleButton(rulesButton, new Color(0, 102, 204));
        homeScreenUI.add(rulesButton);

        JTextArea rulesText = new JTextArea(
            "--- HOW TO PLAY ---\n\nPress SPACE to jump.\n\n" +
            "--- LEVELS (Score) ---\n0-9: Underwater\n10-19: Ground\n20+: Space\n(Cycle Repeats)\n\n" +
            "--- HARD MODE (Score 30+) ---\nGame gets faster!\n\n" +
            "--- POWER-UPS ---\nShield: Survive one hit.\nClock: Slows time for 3 seconds."
        );
        rulesText.setBounds(30, 80, boardWidth - 60, 400);
        rulesText.setWrapStyleWord(true); rulesText.setLineWrap(true);
        rulesText.setOpaque(false); rulesText.setForeground(Color.WHITE);
        rulesText.setFont(new Font("Tahoma", Font.BOLD, 16));
        rulesText.setEditable(false);
        rulesScreenUI.add(rulesText);
        
        JButton backButton = new JButton("Back to Home");
        backButton.setBounds(boardWidth/2 - 75, 500, 150, 50);
        backButton.addActionListener(e -> changeState(GameState.HOME));
        styleButton(backButton, new Color(0, 102, 204));
        rulesScreenUI.add(backButton);

        JButton continueButton = new JButton("Continue");
        continueButton.setBounds(boardWidth/2 - 75, 200, 150, 50);
        continueButton.addActionListener(e -> togglePause());
        styleButton(continueButton, new Color(96, 151, 75));
        pauseMenuUI.add(continueButton);

        JButton restartPauseButton = new JButton("Restart");
        restartPauseButton.setBounds(boardWidth/2 - 75, 260, 150, 50);
        restartPauseButton.addActionListener(e -> restartGame());
        styleButton(restartPauseButton, new Color(204, 122, 0));
        pauseMenuUI.add(restartPauseButton);

        JButton homeButton = new JButton("Home Screen");
        homeButton.setBounds(boardWidth/2 - 75, 320, 150, 50);
        homeButton.addActionListener(e -> goToHome());
        styleButton(homeButton, new Color(180, 0, 0));
        pauseMenuUI.add(homeButton);
        
        onScreenPauseButton = new JButton("||");
        onScreenPauseButton.setFont(new Font("Impact", Font.BOLD, 20));
        onScreenPauseButton.setBounds(boardWidth/2 - 25, 10, 50, 40);
        onScreenPauseButton.addActionListener(e -> togglePause());
        styleButton(onScreenPauseButton, new Color(0,0,0,100));

        JButton restartButton = new JButton("Restart");
        restartButton.setBounds(boardWidth/2 - 50, boardHeight/2 + 20, 100, 50);
        restartButton.addActionListener(e -> restartGame());
        styleButton(restartButton, new Color(96, 151, 75));
        gameOverUI.add(restartButton);

        homeScreenUI.forEach(this::add);
        rulesScreenUI.forEach(this::add);
        pauseMenuUI.forEach(this::add);
        gameOverUI.forEach(this::add);
        add(onScreenPauseButton);
    }

    private void changeState(GameState newState) {
        currentState = newState;
        
        homeScreenUI.forEach(c -> c.setVisible(newState == GameState.HOME));
        rulesScreenUI.forEach(c -> c.setVisible(newState == GameState.RULES));
        pauseMenuUI.forEach(c -> c.setVisible(newState == GameState.PAUSED));
        gameOverUI.forEach(c -> c.setVisible(newState == GameState.GAME_OVER));
        onScreenPauseButton.setVisible(newState == GameState.PLAYING);

        placePipesTimer.stop();
        if (newState == GameState.PLAYING) {
            placePipesTimer.start();
        }
        
        blinkTimer.stop();
        if (newState == GameState.GAME_OVER) {
            blinkTimer.start();
        }
        
        if (newState == GameState.HOME || newState == GameState.RULES) {
            currentBgImg = bgImgDefault;
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color); button.setForeground(Color.WHITE);
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setFocusable(false); button.setOpaque(true); button.setBorderPainted(false);
    }
    
    private void startGame(String playerName, int gapSize) {
        this.playerName = playerName;
        if (this.playerName == null || this.playerName.trim().isEmpty()) { this.playerName = "Player"; }
        openingSpace = gapSize;
        resetGame();
        changeState(GameState.PLAYING);
    }

    private void restartGame() {
        resetGame();
        changeState(GameState.PLAYING);
    }
    
    private void goToHome() {
        resetGame();
        changeState(GameState.HOME);
    }

    private void resetGame() {
        bird.y = birdY; velocityY = 0; pipes.clear(); powerUps.clear();
        shieldActive = false; pipesSinceLastPowerUp = 0;
        if (transitionTimer != null) transitionTimer.stop();
        if (slowMotionTimer != null) slowMotionTimer.stop();
        isTransitioning = false; isSlowMotion = false;
        transitionAlpha = 0.0f; showRestartText = true;
        score = 0; birdAngle = 0.0;
        updateLevelState(true);
        requestFocusInWindow();
    }
    
    private void togglePause() {
        if (currentState == GameState.PLAYING) changeState(GameState.PAUSED);
        else if (currentState == GameState.PAUSED) changeState(GameState.PLAYING);
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw((Graphics2D) g);
    }

    public void draw(Graphics2D g){
        boolean shouldDrawGame = (currentState == GameState.PLAYING || currentState == GameState.PAUSED || currentState == GameState.GAME_OVER);
        
        g.drawImage(currentBgImg, 0, 0, boardWidth, boardHeight, null);
        if (isTransitioning) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transitionAlpha));
            g.drawImage(oldBgImg, 0, 0, boardWidth, boardHeight, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        if (shouldDrawGame) {
            AffineTransform old = g.getTransform();
            g.rotate(birdAngle, bird.x + bird.width / 2, bird.y + bird.height / 2);
            if (bird.img == spaceBirdImg) drawJetpackFlame(g);
            g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
            g.setTransform(old);

            if (shieldActive) {
                g.setColor(new Color(100, 150, 255, 100));
                g.fillOval(bird.x - 5, bird.y - 5, bird.width + 10, bird.height + 10);
            }
            
            for (Pipe pipe : pipes){ g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null); }
            for (PowerUp powerUp : powerUps) { g.drawImage(powerUp.img, powerUp.x, powerUp.y, powerUp.width, powerUp.height, null); }
        }

        if (currentState == GameState.SPLASH) {
            g.setColor(new Color(96, 151, 75));
            g.fillRect(0, 0, boardWidth, boardHeight);
            drawTextWithShadow(g, "FlappyFloppy", new Font("Impact", Font.BOLD, 60), boardHeight / 2 - 50);
        }
        else if (currentState == GameState.HOME) {
            drawTextWithShadow(g, "Flappy Bird", new Font("Impact", Font.BOLD, 50), 125);
        }
        else if (currentState == GameState.RULES) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, boardWidth, boardHeight);
        }
        else if (currentState == GameState.GAME_OVER) {
            g.setColor(new Color(255, 0, 0, 200));
            g.setFont(new Font("Impact", Font.BOLD, 60));
            String gameOverStr = "Game Over";
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int x = (boardWidth - metrics.stringWidth(gameOverStr)) / 2;
            g.drawString(gameOverStr, x, boardHeight / 2 - 100);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Impact", Font.PLAIN, 30));
            String scoreStr = "Score: " + String.valueOf((int) score);
            metrics = g.getFontMetrics(g.getFont());
            x = (boardWidth - metrics.stringWidth(scoreStr)) / 2;
            g.drawString(scoreStr, x, boardHeight / 2 - 50);
            String highScoreStr = "High Score: " + getCurrentHighScore();
            metrics = g.getFontMetrics(g.getFont());
            x = (boardWidth - metrics.stringWidth(highScoreStr)) / 2;
            g.drawString(highScoreStr, x, boardHeight / 2 - 15);
            if (showRestartText) {
                g.setFont(new Font("Tahoma", Font.PLAIN, 18));
                String restartStr = "Press 'K' to Restart";
                metrics = g.getFontMetrics(g.getFont());
                x = (boardWidth - metrics.stringWidth(restartStr)) / 2;
                g.drawString(restartStr, x, boardHeight/2 + 100);
            }
        } 
        else if (currentState == GameState.PLAYING || currentState == GameState.PAUSED) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Tahoma", Font.BOLD, 22));
            g.drawString(playerName + ": " + String.valueOf((int) score), 10, 35);
            
            String highScoreStr = "High: " + getCurrentHighScore();
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            g.drawString(highScoreStr, boardWidth - metrics.stringWidth(highScoreStr) - 10, 35);
        }

        if (currentState == GameState.PAUSED) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, boardWidth, boardHeight);
            drawTextWithShadow(g, "PAUSED", new Font("Impact", Font.BOLD, 50), 150);
        }
    }
    
    private void drawTextWithShadow(Graphics2D g, String text, Font font, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (boardWidth - metrics.stringWidth(text)) / 2;
        g.setFont(font);
        g.setColor(Color.DARK_GRAY);
        g.drawString(text, x + 3, y + 3);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }
    
    private void drawJetpackFlame(Graphics2D g) {
        int flameLength = 15;
        if (velocityY < 0) { flameLength = 25; }
        int birdCenterY = bird.y + bird.height / 2;
        Point tip = new Point(bird.x - flameLength, birdCenterY);
        Point base1 = new Point(bird.x, bird.y);
        Point base2 = new Point(bird.x, bird.y + bird.height);
        g.setColor(new Color(255, 170, 0, 200));
        g.fillPolygon(new int[]{tip.x, base1.x, base2.x}, new int[]{tip.y, base1.y, base2.y}, 3);
        g.setColor(new Color(255, 255, 0, 220));
        g.fillPolygon(new int[]{tip.x + 5, base1.x, base2.x}, new int[]{tip.y, base1.y + 5, base2.y - 5}, 3);
    }
    
    private void setGameOver() {
        if (currentState != GameState.GAME_OVER) {
            int currentScore = (int) score;
            if (openingSpace == boardHeight / 3 && currentScore > highScoreEasy) { highScoreEasy = currentScore; }
            else if (openingSpace == boardHeight / 4 && currentScore > highScoreNormal) { highScoreNormal = currentScore; }
            else if (openingSpace == boardHeight / 5 && currentScore > highScoreHard) { highScoreHard = currentScore; }
            changeState(GameState.GAME_OVER);
        }
    }

    private int getCurrentHighScore() {
        if (openingSpace == boardHeight / 3) return highScoreEasy;
        if (openingSpace == boardHeight / 4) return highScoreNormal;
        if (openingSpace == boardHeight / 5) return highScoreHard;
        return 0; // Should not happen
    }

    private void updateLevelState(boolean instant) {
        int currentScore = (int) score;
        int level = currentScore / 10;
        int levelType = level % 3;
        Image nextBg = currentBgImg;
        boolean hardMode = currentScore >= 30;
        switch (levelType) {
            case 0:
                nextBg = bgImgUnderwater; bird.img = fishImg; velocityX = hardMode ? -5 : -4;
                gravity = 0.4; birdWidth = 44; birdHeight = 31;
                currentTopPipeImg = pipeTopSeaweed; currentBottomPipeImg = pipeBottomSeaweed;
                break;
            case 1:
                nextBg = bgImgDefault; bird.img = birdImgDefault; velocityX = hardMode ? -8 : -6;
                gravity = 0.4; birdWidth = 44; birdHeight = 31;
                currentTopPipeImg = pipeTopDefault; currentBottomPipeImg = pipeBottomDefault;
                break;
            case 2:
                nextBg = bgImgSpace; bird.img = spaceBirdImg; velocityX = hardMode ? -8 : -6;
                gravity = 0.25; birdWidth = 48; birdHeight = 34;
                currentTopPipeImg = pipeTopAsteroid; currentBottomPipeImg = pipeBottomAsteroid;
                break;
        }
        if (!instant && currentBgImg != nextBg) {
            startTransition(nextBg);
        } else {
            currentBgImg = nextBg;
        }
    }
    
    private void startTransition(Image nextBg) {
        if (isTransitioning) return;
        isTransitioning = true;
        oldBgImg = currentBgImg;
        currentBgImg = nextBg;
        transitionAlpha = 1.0f;
        transitionTimer = new Timer(50, e -> {
            transitionAlpha -= (1.0f / 30.0f);
            if (transitionAlpha <= 0.0f) {
                transitionAlpha = 0.0f; isTransitioning = false;
                ((Timer)e.getSource()).stop();
            }
        });
        transitionTimer.start();
    }
    
    public void move(){
        double timeScale = isSlowMotion ? 0.5 : 1.0;
        velocityY += gravity * timeScale;
        bird.y += velocityY * timeScale;
        bird.y = Math.max(bird.y, 0);

        if (velocityY < 0) birdAngle = Math.toRadians(-45);
        else birdAngle = Math.min(Math.toRadians(45), birdAngle + Math.toRadians(2.0 * timeScale));

        for(Pipe pipe : pipes) {
            pipe.x += velocityX * timeScale;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
                updateLevelState(false);
            }
            if (collision(bird, pipe)) {
                if (shieldActive) {
                    shieldActive = false;
                    pipe.x = -pipeWidth * 2;
                } else {
                    setGameOver();
                    return;
                }
            }
        }
        
        powerUps.removeIf(p -> p.collected || p.x < -p.width);
        for (PowerUp p : powerUps) {
            p.x += velocityX * timeScale;
            if (collision(bird, p)) {
                if (p.img == shieldIconImg) {
                    shieldActive = true;
                } else if (p.img == slowMoIconImg) {
                    isSlowMotion = true;
                    if (slowMotionTimer != null && slowMotionTimer.isRunning()) slowMotionTimer.stop();
                    slowMotionTimer = new Timer(3000, e -> isSlowMotion = false);
                    slowMotionTimer.setRepeats(false);
                    slowMotionTimer.start();
                }
                p.collected = true;
            }
        }

        if (bird.y > boardHeight) setGameOver();
    }

    public boolean collision(Bird a, PowerUp b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }
    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.PLAYING) { move(); }
        repaint();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) { 
            if (currentState == GameState.PLAYING) { 
                if (gravity < 0.3) velocityY = -4.5;
                else velocityY = -6;
            }
        } 
        else if (e.getKeyCode() == KeyEvent.VK_K) { if (currentState == GameState.GAME_OVER) restartGame(); }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { togglePause(); }
    }
    
    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        
        Pipe topPipe = new Pipe(currentTopPipeImg); 
        topPipe.y = randomPipeY;

        Pipe bottomPipe = new Pipe(currentBottomPipeImg); 
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        
        pipes.add(topPipe);
        pipes.add(bottomPipe);

        pipesSinceLastPowerUp++;
        if (pipesSinceLastPowerUp >= 5) {
            int powerUpY = topPipe.y + topPipe.height + (openingSpace / 2) - 14;
            if (pipesSinceLastPowerUp % 10 == 0) {
                 powerUps.add(new PowerUp(pipeX + pipeWidth, powerUpY, 28, 28, slowMoIconImg));
            } else {
                 powerUps.add(new PowerUp(pipeX + pipeWidth, powerUpY, 28, 28, shieldIconImg));
            }
            pipesSinceLastPowerUp = 0;
        }
    }
    
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}