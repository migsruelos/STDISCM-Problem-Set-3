import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Canvas extends JPanel {
    private List<Particle> particles;
    private List<ExplorerHandler> explorerHandlers;
    private BufferedImage spriteImage;
    private int frameCount = 0;
    private int fps;
    private long lastFPSTime = System.currentTimeMillis();
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private final int SPRITE_SIZE = 30;
    private JFrame frame;

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
            int counter = 0;
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");
                    ExplorerHandler explorerHandler = new ExplorerHandler(clientSocket, counter);
                    counter++;
                    explorerHandler.start();
                    explorerHandlers.add(explorerHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendDataToExplorers(String data) {
        for (ExplorerHandler handler : explorerHandlers) {
            handler.sendData(data);
        }
    }

    void removeExplorerHandler(ExplorerHandler handler) {
        explorerHandlers.remove(handler);
    }

    public void passFrame(JFrame f){
        frame = f;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Image offscreen = createImage(getWidth(), getHeight());
        Graphics2D offscreenGraphics = (Graphics2D) offscreen.getGraphics();
        offscreenGraphics.clipRect(0, 0, WIDTH, HEIGHT);
        offscreenGraphics.setColor(Color.BLACK);
        offscreenGraphics.fillRect(0,0, WIDTH, HEIGHT);

        renderDeveloperMode(offscreenGraphics);

        calculateFPS();

        g.drawImage(offscreen, 0, 0, this);
    }

    private void renderDeveloperMode(Graphics offscreenGraphics) {
        offscreenGraphics.setColor(Color.GREEN);
        //Render Particles
        for (Particle particle : particles) {
            offscreenGraphics.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }

        //Render explorers
        if(!explorerHandlers.isEmpty()) //Wait for explorers to join
            for(ExplorerHandler explorerHandler : explorerHandlers){
                if(explorerHandler.explorer == null)
                    continue;
                int spriteX = (int) explorerHandler.explorer.x - SPRITE_SIZE / 2;
                int spriteY = (int) explorerHandler.explorer.y - SPRITE_SIZE / 2;
                offscreenGraphics.drawImage(spriteImage, spriteX, spriteY, SPRITE_SIZE, SPRITE_SIZE, null);
            }
    }

    private void updateFPS() {
        fps = frameCount;
        frameCount = 0;
        lastFPSTime = System.currentTimeMillis();

        frame.setTitle("Particle Simulator | FPS: " + calculateFPS());
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
        for (int i = 0; i < n; i++) {
            double randomX = startX + Math.random() * (endX - startX);
            double randomY = startY + Math.random() * (endY - startY);
            particles.add(new Particle(randomX, randomY, initialAngle, velocity));
        }
    }

    void addParticlesByAngle(int n, double startX, double startY, double velocity, double startAngle, double endAngle) {
        for (int i = 0; i < n; i++) {
            double randomAngle = startAngle + Math.random() * (endAngle - startAngle);
            particles.add(new Particle(startX, startY, randomAngle, velocity));
        }
    }

    void addParticlesByVelocity(int n, double startX, double startY, double angle, double startVelocity, double endVelocity) {
        for (int i = 0; i < n; i++) {
            double randomVelocity = startX + Math.random() * (endVelocity - startVelocity);
            particles.add(new Particle(startX, startY, angle, randomVelocity));
        }
    }

    private class ExplorerHandler extends Thread {
        public int ID; //To prevent sending own data
        public Particle explorer; //Particle of current explorer
        private Socket socket, replySocket;
        private PrintWriter out;

        ExplorerHandler(Socket socket, int id) {
            this.socket = socket;
            ID = id;
        }

        @Override
        public void run() {
            BufferedReader in;

            //Create a thread that sends particle and explorer coords to client
            new Thread(() -> {
                StringBuilder res = new StringBuilder();
                while(true){
                    try {
                        res.append("STATE ");
                        //Add particles
                        for(Particle p: particles){
                            //Check if in periphery before adding to message
                            int distanceX = (int) (p.x - explorer.x);
                            int distanceY = (int) (p.y - explorer.y);

                            if(Math.abs(distanceX) > PERIPHERY_WIDTH || Math.abs(distanceY) > PERIPHERY_HEIGHT)
                                continue;

                            res.append("P "); //Add data type
                            res.append(p.x).append(" "); //Add x-coord
                            res.append(p.y).append(" "); //Add y-coord
                        }
                        //Add explorers
                        for(int i = 0; i < explorerHandlers.size(); i++){
                            ExplorerHandler e = explorerHandlers.get(i);

                            //Check if handler id is the same, skip if so
                            if(e.ID == ID)
                                continue;
                            //Check if in periphery before adding to message
                            int distanceX = (int) (e.explorer.x - explorer.x);
                            int distanceY = (int) (e.explorer.y - explorer.y);

                            if(Math.abs(distanceX) > PERIPHERY_WIDTH || Math.abs(distanceY) > PERIPHERY_HEIGHT)
                                continue;

                            res.append("E "); //Add data type
                            res.append(e.explorer.x).append(" "); //Add x-coord
                            res.append(e.explorer.y).append(" "); //Add y-coord
                        }

                        //Send to client
                        sendData(res.toString());

                        //Clear res
                        res = new StringBuilder();

                        //Sleep so data is not sent too often
                        try {
                            Thread.sleep(1000/60);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (RuntimeException ignored) {

                    }
                }
            }).start();

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;
                boolean spawned = false;

                while ((inputLine = in.readLine()) != null) {
                    if(!spawned){
                        //Get init data
                        String[] temp = inputLine.split(" ");

                        //Connect reply socket
                        replySocket = new Socket(socket.getInetAddress(), Integer.parseInt(temp[0])); //Use socket addr and port in message
                        out = new PrintWriter(replySocket.getOutputStream(), true);

                        //Get starting coords of explorer and create explorer
                        explorer = new Particle(Integer.parseInt(temp[1]), Integer.parseInt(temp[2]) , 0, 0);
                        spawned = true;
                    }

                    //Read movement inputs
                    switch (inputLine){
                        case "MOVE_UP":{
                            moveExplorerSprite(0, -5);
                            break;
                        }
                        case "MOVE_DOWN":{
                            moveExplorerSprite(0, 5);
                            break;
                        }
                        case "MOVE_LEFT":{
                            moveExplorerSprite(-5, 0);
                            break;
                        }
                        case "MOVE_RIGHT":{
                            moveExplorerSprite(5, 0);
                            break;
                        }
                    }

                    if (inputLine.equals("EXIT")) {
                        System.out.println("Client disconnected.");
                        removeExplorerHandler(this);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                removeExplorerHandler(this);
            }
        }

        public void moveExplorerSprite(int dx, int dy) {
            if (explorer != null) {
                explorer.x += dx;
                explorer.y += dy;

                //Bounds
                if(explorer.x > 1280)
                    explorer.x = 1280;
                if(explorer.x < 0)
                    explorer.x = 0;
                if(explorer.y > 720)
                    explorer.y = 720;
                if(explorer.y < 0)
                    explorer.y = 0;

                //send coords data back to client
                sendData("MOVE " + explorer.x + " " + explorer.y);
            }
        }

        void sendData(String data) {
            if(out != null)
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
