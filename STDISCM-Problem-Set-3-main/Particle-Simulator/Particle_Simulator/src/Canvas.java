import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Canvas extends JPanel implements KeyListener {
    private List<Particle> particles;
    private List<Particle> explorers;
    private List<ExplorerHandler> explorerHandlers;
    private boolean explorerMode = false;
    private Particle explorerSprite;
    private BufferedImage spriteImage;
    private int frameCount = 0;
    private int fps;
    private long lastFPSTime = System.currentTimeMillis();
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private final int SPRITE_SIZE = 30;
    private JFrame frame;
    private boolean explorerSpawned = false;

    private static final int PORT = 12345;
    private ServerSocket serverSocket;

    private final int PERIPHERY_WIDTH = 33;
    private final int PERIPHERY_HEIGHT = 19;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Canvas::new);
    }

    Canvas() {
        particles = new ArrayList<>();
        explorerHandlers = new ArrayList<>();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        // Load the sprite image
        try {
            spriteImage = ImageIO.read(new File("STDISCM-Problem-Set-3-main/Particle-Simulator/Particle_Simulator/src/sprite/sprite.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::calculateFPS, 0, 500, TimeUnit.MILLISECONDS);

        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");
                    ExplorerHandler explorerHandler = new ExplorerHandler(clientSocket);
                    explorerHandler.start();
                    explorerHandlers.add(explorerHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    private void sendDataToExplorers(String data) {
        for (ExplorerHandler handler : explorerHandlers) {
            handler.sendData(data);
        }
    }

    void removeExplorerHandler(ExplorerHandler handler) {
        explorerHandlers.remove(handler);
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
                        sendDataToExplorers("MOVE_UP");
                    }
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (explorerSprite.y + SPRITE_SIZE < HEIGHT) {
                        moveExplorerSprite(0, 5);
                        sendDataToExplorers("MOVE_DOWN");
                    }
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (explorerSprite.x - SPRITE_SIZE > 0) {
                        moveExplorerSprite(-5, 0);
                        sendDataToExplorers("MOVE_LEFT");
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (explorerSprite.x + SPRITE_SIZE < WIDTH) {
                        moveExplorerSprite(5, 0);
                        sendDataToExplorers("MOVE_RIGHT");
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

        calculateFPS();

        g.drawImage(offscreen, 0, 0, this);
    }

    private void renderDeveloperMode(Graphics offscreenGraphics) {
        offscreenGraphics.setColor(Color.GREEN);
        for (Particle particle : particles) {
            offscreenGraphics.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }

        if (explorerSpawned && spriteImage != null) {
            int spriteX = (int) explorerSprite.x - SPRITE_SIZE / 2;
            int spriteY = (int) explorerSprite.y - SPRITE_SIZE / 2;
            offscreenGraphics.drawImage(spriteImage, spriteX, spriteY, SPRITE_SIZE, SPRITE_SIZE, null);
        }
    }

    private void renderExplorerMode(Graphics g) {
        g.setColor(Color.GREEN);
        for (Particle particle : particles) {
            int distanceX = (int) (particle.x - explorerSprite.x);
            int distanceY = (int) (particle.y - explorerSprite.y);
            if(Math.abs(distanceX) > PERIPHERY_WIDTH || Math.abs(distanceY) > PERIPHERY_HEIGHT)
                continue;

            g.fillOval((int) distanceX * (WIDTH/PERIPHERY_WIDTH) + (WIDTH/2),
                    (int) distanceY * (HEIGHT/PERIPHERY_HEIGHT) + (HEIGHT/2), 10, 10);
        }

        if (explorerSpawned && spriteImage != null) {
            g.drawImage(spriteImage, WIDTH / 2 - SPRITE_SIZE * 10, HEIGHT / 2 - SPRITE_SIZE * 10,
                SPRITE_SIZE*20, SPRITE_SIZE*20, null);
        }
    }
            /*if(Math.abs(distanceX) > PERIPHERY_WIDTH || Math.abs(distanceY) > PERIPHERY_HEIGHT)
                continue;

            g.fillOval((int) distanceX * (WIDTH/PERIPHERY_WIDTH),
                (int) distanceY * (HEIGHT/PERIPHERY_HEIGHT), 10, 10);
        }

        if (explorerSpawned && spriteImage != null) {
            g.drawImage(spriteImage, WIDTH / 2 - SPRITE_SIZE * 10, HEIGHT / 2 - SPRITE_SIZE * 10,
                SPRITE_SIZE*20, SPRITE_SIZE*20, null);*/

    private void updateFPS() {
        fps = frameCount;
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

        if (elapsedTime >= 1000) {
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

    private class ExplorerHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        ExplorerHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("EXIT")) {
                        System.out.println("Client disconnected.");
                        removeExplorerHandler(this);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void sendData(String data) {
            out.println(data);
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
