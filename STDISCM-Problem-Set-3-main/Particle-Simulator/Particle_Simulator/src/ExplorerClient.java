import java.io.*;
import java.net.*;

public class ExplorerClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ExplorerClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void main(String[] args) {
        String serverAddress = "localhost"; // change to the actual server address
        int serverPort = 12345; // change to the actual server port
        ExplorerClient client = new ExplorerClient(serverAddress, serverPort);
        client.start();
    }
}
