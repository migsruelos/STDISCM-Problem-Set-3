import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Canvas extends JPanel implements KeyListener{
    private List<Particle> particles;
    private List<Particle> explorerParticles;
    private boolean explorerMode = false;
    private Particle explorerSprite;
    private Particle developerSprite;
    private BufferedImage spriteImage;
    private int frameCount = 0;
    private int fps;
    private long lastFPSTime = System.currentTimeMillis();
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private final int PERIPHERY_WIDTH = 33;
    private final int PERIPHERY_HEIGHT = 19;
    private final int SPRITE_SIZE = 30;
    private final int PARTICLE_SIZE = 10;
    private JFrame frame;
    private boolean explorerSpawned = false;


    Canvas() {
        particles = new ArrayList<>();
        explorerParticles = new ArrayList<>();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        // Load the sprite image
        try {
            spriteImage = ImageIO.read(new File("Particle-Simulator/Particle_Simulator/src/sprite/sprite.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::calculateFPS, 0, 500, TimeUnit.MILLISECONDS);
    }

    boolean isExplorerMode() {
        return explorerMode;
    }

    void toggleExplorerMode() {
        explorerMode = !explorerMode;
        if (!explorerSpawned) {
            explorerSprite = new Particle(100, 100, 0, 0);
            explorerSpawned = true;
        }
    }

    public void moveExplorerSprite(int dx, int dy) {
        if (explorerSprite != null) {
            explorerSprite.x += dx;
            explorerSprite.y += dy;
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (explorerMode) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (explorerSprite.y - SPRITE_SIZE > 0) {
                        moveExplorerSprite(0, -5);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (explorerSprite.y + SPRITE_SIZE < HEIGHT) {
                        moveExplorerSprite(0, 5);
                    }
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (explorerSprite.x - SPRITE_SIZE > 0) {
                        moveExplorerSprite(-5, 0);
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (explorerSprite.x + SPRITE_SIZE < WIDTH) {
                        moveExplorerSprite(5, 0);
                    }
                    break;
            }
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public void addKeyHandler(JFrame frame) {
        frame.addKeyListener(this);
    }

    public void passFrame(JFrame f){
        frame = f;
    }

    private void updateFPS() {
        fps = frameCount * 2;
        frameCount = 0;
        lastFPSTime = System.currentTimeMillis();
        if(!explorerSpawned)
            frame.setTitle("Particle Simulator | FPS: " + calculateFPS());
        else
            frame.setTitle("Particle Simulator | FPS: " + calculateFPS() + " | X: "
            + explorerSprite.x + " Y: " + explorerSprite.y);
    }

    private int calculateFPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastFPSTime;

        frameCount++;

        // update FPS every 0.5 seconds
        if (elapsedTime >= 500) {
            updateFPS();
        }

        return fps;
    }

    void addParticles(int n, double startX, double startY, double endX, double endY,
                      double initialAngle, double velocity) {
        if (!explorerMode) {
            for (int i = 0; i < n; i++) {
                double randomX = startX + Math.random() * (endX - startX);
                double randomY = startY + Math.random() * (endY - startY);
                particles.add(new Particle(randomX, randomY, initialAngle, velocity));
            }
        }
    }

    void addParticlesByAngle(int n, double startX, double startY, double velocity, double startAngle, double endAngle) {
        if (!explorerMode) {
            for (int i = 0; i < n; i++) {
                double randomAngle = startAngle + Math.random() * (endAngle - startAngle);
                particles.add(new Particle(startX, startY, randomAngle, velocity));
            }
        }
    }

    void addParticlesByVelocity(int n, double startX, double startY, double angle, double startVelocity, double endVelocity) {
        if(!explorerMode) {
            for (int i = 0; i < n; i++) {
                double randomVelocity = startX + Math.random() * (endVelocity - startVelocity);
                particles.add(new Particle(startX, startY, angle, randomVelocity));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Image offscreen = createImage(getWidth(), getHeight());
        Graphics2D offscreenGraphics = (Graphics2D) offscreen.getGraphics();
        offscreenGraphics.setColor(Color.BLACK);
        offscreenGraphics.fillRect(0,0, WIDTH, HEIGHT);

        if (explorerMode) {
            renderExplorerMode(offscreenGraphics);
        } else {
            renderDeveloperMode(offscreenGraphics);
        }

        //IDK why but this is needed for more accurate fps measurement???
        calculateFPS();

        g.drawImage(offscreen, 0, 0, this);
    }

    private void renderDeveloperMode(Graphics offscreenGraphics) {
        offscreenGraphics.setColor(Color.GREEN);
        for (Particle particle : particles) {
            offscreenGraphics.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }

        //Render sprite in actual location if spawned
        if (explorerSpawned && spriteImage != null) {
            int spriteX = (int) explorerSprite.x - SPRITE_SIZE / 2;
            int spriteY = (int) explorerSprite.y - SPRITE_SIZE / 2;
            offscreenGraphics.drawImage(spriteImage, spriteX, spriteY, SPRITE_SIZE, SPRITE_SIZE, null);
        }
    }

    private void renderExplorerMode(Graphics g) {
        // Render particles within the sprite's periphery
        g.setColor(Color.GREEN);
        for (Particle particle : particles) {
            int distanceX = (int) (particle.x - explorerSprite.x);
            int distanceY = (int) (particle.y - explorerSprite.y);
            if(Math.abs(distanceX) > PERIPHERY_WIDTH || Math.abs(distanceY) > PERIPHERY_HEIGHT)
                continue; //Skip rendering particle if distance greater than periphery

            g.fillOval((int) distanceX * (WIDTH/PERIPHERY_WIDTH),
                    (int) distanceY * (HEIGHT/PERIPHERY_HEIGHT), 10, 10);
        }

        // Render sprite image centered in the periphery
        if (explorerSpawned && spriteImage != null) {
            //Render this in center at all times
            g.drawImage(spriteImage, WIDTH / 2 - SPRITE_SIZE * 10, HEIGHT / 2 - SPRITE_SIZE * 10,
                    SPRITE_SIZE*20, SPRITE_SIZE*20, null);
        }
    }


    void update() {
        calculateFPS();
        double deltaTime = 0.05;

        // minimum target FPS: 60
        long targetFrameTime = 1000 / 60;
        long currentTime = System.currentTimeMillis();

        // submit rendering tasks for particles
        particles.forEach(particle -> particle.move(deltaTime));
        repaint();

        // calculates the time taken for the update and rendering tasks
        long elapsedTime = System.currentTimeMillis() - currentTime;

        // sleep to maintain a consistent frame rate
        long sleepTime = Math.max(0, targetFrameTime - elapsedTime);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}