import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class ParticleSimulator extends JFrame implements KeyListener {
    private Canvas canvas;
    private JButton particleByDistanceButton;
    private JButton particleByAngleButton;
    private JButton particleByVelocityButton;

    public static final int FRAME_WIDTH = 1600;
    public static final int FRAME_HEIGHT = 900;

    ParticleSimulator() {
        setTitle("Particle Simulator | FPS: 0");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 0, 0));
        canvas = new Canvas();
        canvas.passFrame(this);

        panel.add(canvas);

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));

        particleByDistanceButton = new JButton("Add Particle (Distance)");
        particleByDistanceButton.setFocusable(false);
        particleByDistanceButton.addActionListener(e -> {
            ParticleByDistanceInputDialog particleByDistanceDialog = new ParticleByDistanceInputDialog(this);
            particleByDistanceDialog.setVisible(true);
        });
        particleByAngleButton = new JButton("Add Particle (Angle)");
        particleByAngleButton.setFocusable(false);
        particleByAngleButton.addActionListener(e -> {
            ParticleByAngleInputDialog particleByAngleDialog = new ParticleByAngleInputDialog(this);
            particleByAngleDialog.setVisible(true);
        });
        particleByVelocityButton = new JButton("Add Particle (Velocity)");
        particleByVelocityButton.setFocusable(false);
        particleByVelocityButton.addActionListener(e -> {
            ParticleByVelocityInputDialog particleByVelocityDialog = new ParticleByVelocityInputDialog(this);
            particleByVelocityDialog.setVisible(true);
        });

        buttonPanel.add(particleByDistanceButton);
        buttonPanel.add(particleByAngleButton);
        buttonPanel.add(particleByVelocityButton);

        panel.add(buttonPanel);
        add(panel);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setVisible(true);

        Timer timer = new Timer(15, e -> {
            canvas.update();
        });
        timer.start();
    }

    private void toggleMode() {
        particleByDistanceButton.setEnabled(true);
        particleByDistanceButton.setVisible(true);
        particleByAngleButton.setEnabled(true);
        particleByAngleButton.setVisible(true);
        particleByVelocityButton.setEnabled(true);
        particleByVelocityButton.setVisible(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            toggleMode();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public Canvas getCanvas() {
        return canvas;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParticleSimulator());
    }
}

class ParticleByDistanceInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startXField;
    private JTextField startYField;
    private JTextField endXField;
    private JTextField endYField;
    private JTextField angleField;
    private JTextField velocityField;

    ParticleByDistanceInputDialog(JFrame parent) {
        super(parent, "Particle Input", true);
        setLocationRelativeTo(parent);
        setSize(400, 400);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        particleCountField = createInputField();
        startXField = createInputField();
        startYField = createInputField();
        endXField = createInputField();
        endYField = createInputField();
        angleField = createInputField();
        velocityField = createInputField();


        addRow(panel, gbc, "Particle Count:", particleCountField);
        addRow(panel, gbc, "Start X:", startXField);
        addRow(panel, gbc, "Start Y:", startYField);
        addRow(panel, gbc, "End X:", endXField);
        addRow(panel, gbc, "End Y:", endYField);
        addRow(panel, gbc, "Angle:", angleField);
        addRow(panel, gbc, "Velocity:", velocityField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // get user input
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startX = Double.parseDouble(startXField.getText());
            double startY = Double.parseDouble(startYField.getText());
            double endX = Double.parseDouble(endXField.getText());
            double endY = Double.parseDouble(endYField.getText());
            double angle = Double.parseDouble(angleField.getText());
            double velocity = Double.parseDouble(velocityField.getText());

            // add particles to canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticles(particleCount, startX, startY, endX, endY, -angle, velocity);

            // close the dialog
            setVisible(false);
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
    }

    private JTextField createInputField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 25));
        return textField;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }
}

class ParticleByAngleInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startAngleField;
    private JTextField endAngleField;

    ParticleByAngleInputDialog(JFrame parent) {
        super(parent, "Particle Input", true);
        setLocationRelativeTo(parent);
        setSize(400, 400);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        particleCountField = createInputField();
        startAngleField = createInputField();
        endAngleField = createInputField();
        JTextField startXField = createInputField();
        JTextField startYField = createInputField();
        JTextField velocityField = createInputField();

        addRow(panel, gbc, "Particle Count:", particleCountField);
        addRow(panel, gbc, "Start Angle:", startAngleField);
        addRow(panel, gbc, "End Angle:", endAngleField);
        addRow(panel, gbc, "X:", startXField);
        addRow(panel, gbc, "Y:", startYField);
        addRow(panel, gbc, "Velocity:", velocityField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // get user input
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startAngle = Double.parseDouble(startAngleField.getText());
            double endAngle = Double.parseDouble(endAngleField.getText());
            double x = Double.parseDouble(startXField.getText());
            double y = Double.parseDouble(startYField.getText());
            double v = Double.parseDouble(velocityField.getText());

            // add particles to canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticlesByAngle(particleCount, x, y, v, -startAngle, -endAngle);

            // close the dialog
            setVisible(false);
        });

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
    }

    private JTextField createInputField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 25));
        return textField;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }
}

class ParticleByVelocityInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startVelocityField;
    private JTextField endVelocityField;
    private JTextField angleField;
    private JTextField startXField, startYField;

    ParticleByVelocityInputDialog(JFrame parent) {
        super(parent, "Particle Input", true);
        setLocationRelativeTo(parent);
        setSize(400, 400);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        particleCountField = createInputField();
        startVelocityField = createInputField();
        endVelocityField = createInputField();
        angleField = createInputField();
        startXField = createInputField();
        startYField = createInputField();

        addRow(panel, gbc, "Particle Count:", particleCountField);
        addRow(panel, gbc, "Start Velocity:", startVelocityField);
        addRow(panel, gbc, "End Velocity:", endVelocityField);
        addRow(panel, gbc, "Angle:", angleField);
        addRow(panel, gbc, "X:", startXField);
        addRow(panel, gbc, "Y:", startYField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // get user input
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startVelocity= Double.parseDouble(startVelocityField.getText());
            double endVelocity = Double.parseDouble(endVelocityField.getText());
            double a = Double.parseDouble(angleField.getText());
            double x = Double.parseDouble(startXField.getText());
            double y = Double.parseDouble(startYField.getText());

            // add particles to canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticlesByVelocity(particleCount, x, y, a, startVelocity, endVelocity);

            // close the dialog
            setVisible(false);
        });

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
    }

    private JTextField createInputField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 25));
        return textField;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }
}

