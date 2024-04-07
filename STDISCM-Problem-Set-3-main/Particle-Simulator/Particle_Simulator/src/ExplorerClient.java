import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;

public class ExplorerClient extends JFrame implements KeyListener {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    //UI STUFF
    JPanel panel;
    private final int FRAME_WIDTH = 1600;
    private final int FRAME_HEIGHT = 900;
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private final int PERIPHERY_WIDTH = 33;
    private final int PERIPHERY_HEIGHT = 19;

    private class ECanvas extends JPanel{

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
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 0, 0));

        add(panel);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setVisible(true);
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