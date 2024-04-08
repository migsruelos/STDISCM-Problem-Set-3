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
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExplorerClient extends JFrame implements KeyListener {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ServerSocket serverSocket;
    private Socket replySocket;

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
            explorers = new ArrayList<>();
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setFocusable(true);
            requestFocusInWindow();

            try {
                spriteImage = ImageIO.read(getClass().getResource("/sprite/sprite.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(this::calculateFPS, 0, 500, TimeUnit.MILLISECONDS);
        }

        private void renderExplorerMode(Graphics g) {
            try {
                g.setColor(Color.GREEN);
                for (Particle particle : particles) {
                    int distanceX = (int) (particle.x - explorerSprite.x);
                    int distanceY = (int) (particle.y - explorerSprite.y);
                    distanceX = (int) distanceX * (WIDTH/PERIPHERY_WIDTH) + (WIDTH/2);
                    distanceY = (int) distanceY * (HEIGHT/PERIPHERY_HEIGHT) + (HEIGHT/2);

                    //Extra assurance against improper rendering
                    if(distanceX > WIDTH || distanceX < 0)
                        continue;
                    if(distanceY > HEIGHT || distanceY < 0)
                        continue;

                    g.fillOval(distanceX, distanceY, 10, 10);
                }

                //Code that draws other explorer sprites
                for(Particle explorer: explorers){
                    int distanceX = (int) (explorer.x - explorerSprite.x);
                    int distanceY = (int) (explorer.y - explorerSprite.y);
                    distanceX = (int) distanceX * (WIDTH/PERIPHERY_WIDTH) + (WIDTH/2);
                    distanceY = (int) distanceY * (HEIGHT/PERIPHERY_HEIGHT) + (HEIGHT/2);

                    g.drawImage(spriteImage, distanceX - SPRITE_SIZE * 4, distanceY - SPRITE_SIZE * 4,
                            SPRITE_SIZE*8, SPRITE_SIZE*8, null);
                }

                if (explorerSpawned && spriteImage != null) {
                    g.drawImage(spriteImage, WIDTH / 2 - SPRITE_SIZE * 4, HEIGHT / 2 - SPRITE_SIZE * 4,
                            SPRITE_SIZE*8, SPRITE_SIZE*8, null);
                }
            } catch (Exception ignored) {

            }
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
            offscreenGraphics.clipRect(0, 0, WIDTH, HEIGHT);
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

    public ExplorerClient(int clientPort, String serverAddress, int serverPort, int startX, int startY) {
        //Connection
        try {
            //Setup reply server
            new Thread(() -> {
                System.out.println("Listening on port: " + clientPort);
                try {
                    serverSocket = new ServerSocket(clientPort);
                    replySocket = serverSocket.accept(); //Get socket for server replies
                    in = new BufferedReader(new InputStreamReader(replySocket.getInputStream()));
                    start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server contaning explorer spawn point and port
            out.println(clientPort + " " + startX + " " + startX);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Inputs
        addKeyListener(this);

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

        //Load Explorer client particle
        explorerSprite = new Particle(startX, startY, 0, 0);

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

                //Clear loaded particles and explorers before getting new data
                if(particles != null)
                    particles.clear();
                if(explorers != null)
                    explorers.clear();

                String[] temp = inputLine.split(" ");

                //Determine message type and update accordingly
                switch (temp[0]){
                    case "MOVE": //Message involves explorer movement
                        explorerSprite.x = Double.parseDouble(temp[1]);
                        explorerSprite.y = Double.parseDouble(temp[2]);
                        break;
                    case "STATE": //Message involves state of sim
                        for(int i = 1; i < temp.length; i += 3){
                            //Check if data is a particle or an explorer
                            if(temp[i].equals("P")){
                                particles.add(new Particle(Double.parseDouble(temp[i+1]), Double.parseDouble(temp[i+2]), 0, 0));
                            }
                            else if(temp[i].equals("E")){
                                explorers.add(new Particle(Double.parseDouble(temp[i+1]), Double.parseDouble(temp[i+2]), 0, 0));
                            }
                        }
                        break;
                }
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
        Scanner s = new Scanner(System.in);

        //Get inputs on IP, Port, and starting coords
        System.out.print("Enter Client Port: ");
        int port = s.nextInt();
        System.out.print("Enter Server IP: ");
        String serverAddress = s.next(); // change to the actual server address
        System.out.print("Enter Server Port: ");
        int serverPort = s.nextInt(); // change to the actual server port
        System.out.print("Enter Starting X-coordinate: ");
        int x = s.nextInt();
        System.out.print("Enter Starting Y-coordinate: ");
        int y = s.nextInt();

        //Error prevention on coords
        if(x > 1280)
            x = 1280;
        else if(x < 0)
            x = 0;
        if(y > 720)
            y = 720;
        else if(y < 0)
            y = 0;

        ExplorerClient client = new ExplorerClient(port, serverAddress, serverPort, x, y);
    }
}