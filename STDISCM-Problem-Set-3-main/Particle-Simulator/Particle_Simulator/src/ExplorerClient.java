import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExplorerClient extends JFrame implements KeyListener {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    //UI STUFF
    JPanel panel;
    private final int FRAME_WIDTH = 1600;
    private final int FRAME_HEIGHT = 900;
    private final int PERIPHERY_WIDTH = 33;
    private final int PERIPHERY_HEIGHT = 19;

    //Rendering stuff
    private List<Particle> particles;
    private List<Particle> explorers;
    private BufferedImage spriteImage;
    private ECanvas canvas;
    private Particle explorerSprite;
    private boolean explorerSpawned = true;
    private final int SPRITE_SIZE = 30;

    private class ECanvas extends JPanel{
        private final int WIDTH = 1280;
        private final int HEIGHT = 720;

        public ECanvas(){
            particles = new ArrayList<>();
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setFocusable(true);
            requestFocusInWindow();

            try {
                spriteImage = ImageIO.read(new File("STDISCM-Problem-Set-3-main/Particle-Simulator/Particle_Simulator/src/sprite/sprite.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(this::calculateFPS, 0, 500, TimeUnit.MILLISECONDS);
        }

        private void renderExplorerMode(Graphics g) {
            g.setColor(Color.GREEN);
            for (Particle particle : particles) {
                int distanceX = (int) (particle.x - explorerSprite.x);
                int distanceY = (int) (particle.y - explorerSprite.y);
                if(Math.abs(distanceX) > PERIPHERY_WIDTH || Math.abs(distanceY) > PERIPHERY_HEIGHT)
                    continue;

                g.fillOval((int) distanceX * (WIDTH/PERIPHERY_WIDTH),
                        (int) distanceY * (HEIGHT/PERIPHERY_HEIGHT), 10, 10);
            }

            if (explorerSpawned && spriteImage != null) {
                g.drawImage(spriteImage, WIDTH / 2 - SPRITE_SIZE * 4, HEIGHT / 2 - SPRITE_SIZE * 4,
                        SPRITE_SIZE*8, SPRITE_SIZE*8, null);
            }

            //TODO: Add code that draws other explorer sprites
        }

        private void updateFPS() {
            fps = frameCount;
            frameCount = 0;
            lastFPSTime = System.currentTimeMillis();

            setTitle("Particle Simulator | FPS: " + calculateFPS() + " | X: "
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Image offscreen = createImage(getWidth(), getHeight());
            Graphics2D offscreenGraphics = (Graphics2D) offscreen.getGraphics();
            offscreenGraphics.setColor(Color.BLACK);
            offscreenGraphics.fillRect(0,0, WIDTH, HEIGHT);

            renderExplorerMode(offscreenGraphics);

            calculateFPS();

            g.drawImage(offscreen, 0, 0, this);
        }

        void update() {
            calculateFPS();
            double deltaTime = 0.05;

            // minimum target FPS: 60
            long targetFrameTime = 1000 / 60;
            long currentTime = System.currentTimeMillis();

            // submit rendering tasks for particles
            //TODO: Get particle coords from server
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

    //FPS stuff
    private int frameCount = 0;
    private int fps;
    private long lastFPSTime = System.currentTimeMillis();

    public ExplorerClient(String serverAddress, int serverPort) {
        //Connection
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server to indicate that the client wants to enter explorer mode
            out.println("ENTER_EXPLORER_MODE");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //UI
        setTitle("Explorer Client | FPS: 0");
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 120, 0, 0));
        canvas = new ECanvas();
        panel.add(canvas);
        setFocusable(true);
        requestFocusInWindow();

        add(panel);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setVisible(true);

        //Load Explorer client particle TODO: Maybe add a promt for coords on where to spawn?
        explorerSprite = new Particle(500, 300, 0, 0);

        Timer timer = new Timer(15, e -> {
            canvas.update();
        });
        timer.start();
    }

    public void start() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Process the received data
                // For example, you might receive messages indicating explorer movement or other updates
                // Update the client's explorer sprite accordingly
                updateExplorerSprite(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateExplorerSprite(String data) {
        if (data.equals("MOVE_UP")) {
            // Update explorer sprite position to move up
        } else if (data.equals("MOVE_DOWN")) {
            // Update explorer sprite position to move down
        } else if (data.equals("MOVE_LEFT")) {
            // Update explorer sprite position to move left
        } else if (data.equals("MOVE_RIGHT")) {
            // Update explorer sprite position to move right
        }
    }

    // Method to send movement commands to the server
    public void sendMovementCommand(String command) {
        out.println(command);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                sendMovementCommand("MOVE_UP");
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                sendMovementCommand("MOVE_DOWN");
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                sendMovementCommand("MOVE_LEFT");
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                sendMovementCommand("MOVE_RIGHT");
                break;
        }
    }


    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        String serverAddress = "localhost"; // change to the actual server address
        int serverPort = 12345; // change to the actual server port
        ExplorerClient client = new ExplorerClient(serverAddress, serverPort);
        client.start();
    }
}