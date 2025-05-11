import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class TrafficSignalGUI extends JFrame {
    private JPanel signalPanel;
    private JPanel controlPanel;
    private JLabel timerLabel;
    private Color currentColor = Color.RED;
    private boolean isEmergencyMode = false;
    private Random random = new Random();
    private static final long SIGNAL_DURATION = 3000; // 3 seconds per signal
    private static final long EMERGENCY_DURATION = 10; // 10 seconds for emergency
    private static final long MIN_SIGNAL_DURATION = 2000; // Minimum 2 seconds per signal
    private Thread normalOperationThread;
    private volatile boolean isRunning = true;
    private Image backgroundImage;
    
    // Overlay components
    private JPanel overlayPanel;
    private JLabel overlayTimerLabel;

    public TrafficSignalGUI() {
        setTitle("Traffic Signal System");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Load background image
        try {
            backgroundImage = ImageIO.read(new File("traffic.jpg"));
            backgroundImage = backgroundImage.getScaledInstance(800, 550, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            System.out.println("Error loading background image: " + e.getMessage());
        }

        // Create main panel with background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Create title panel
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0,
                    new Color(70, 130, 180),
                    getWidth(), 0,
                    new Color(100, 149, 237)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titlePanel.setPreferredSize(new Dimension(800, 50));
        titlePanel.setLayout(new BorderLayout());

        // Create title label
        JLabel titleLabel = new JLabel("EMERGENCY VEHICLE PRIORITIZE SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Create timer label with better styling
        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setPreferredSize(new Dimension(800, 40));
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setOpaque(true);
        timerLabel.setBackground(new Color(255, 255, 255));
        
        // Create striped background panel for timer
        JPanel timerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                int stripeHeight = 10;
                
                for (int y = 0; y < height; y += stripeHeight * 2) {
                    // White stripe
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, y, width, stripeHeight);
                    
                    // Black stripe
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.fillRect(0, y + stripeHeight, width, stripeHeight);
                }
            }
        };
        timerPanel.setLayout(new BorderLayout());
        timerPanel.add(timerLabel, BorderLayout.CENTER);

        // Create signal panel with enhanced styling
        signalPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw traffic signal box with gradient
                GradientPaint gradient = new GradientPaint(
                    width/8, height/4,
                    new Color(60, 60, 60),
                    width*3/4, height/2,
                    new Color(40, 40, 40)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(width/8, height/4, width*3/4, height/2, 20, 20);
                
                // Draw three circles for signals horizontally
                int circleSize = Math.min(width/8, height/3);
                int spacing = width/4;
                int startX = width/6;
                int y = height/3;
                
                // Add glow effect for active light
                if (currentColor != Color.GRAY) {
                    g2d.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 100));
                    g2d.fillOval(startX + (currentColor == Color.YELLOW ? spacing : currentColor == Color.GREEN ? spacing * 2 : 0) - 5,
                               y - 5, circleSize + 10, circleSize + 10);
                }
                
                // Red light
                g2d.setColor(currentColor == Color.RED ? Color.RED : new Color(100, 100, 100));
                g2d.fillOval(startX, y, circleSize, circleSize);
                
                // Yellow light
                g2d.setColor(currentColor == Color.YELLOW ? Color.YELLOW : new Color(100, 100, 100));
                g2d.fillOval(startX + spacing, y, circleSize, circleSize);
                
                // Green light
                g2d.setColor(currentColor == Color.GREEN ? Color.GREEN : new Color(100, 100, 100));
                g2d.fillOval(startX + spacing * 2, y, circleSize, circleSize);
            }
        };
        signalPanel.setPreferredSize(new Dimension(800, 250));
        signalPanel.setOpaque(false);

        // Create control panel with enhanced styling
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 3, 15, 15));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setOpaque(false);
        
        String[] vehicleTypes = {
            "🚑 Ambulance",
            "🚒 Fire Truck",
            "🚓 Police Vehicle",
            "🚐 Government Vehicle",
            "⏯️ No Emergency",
            "❌ Exit"
        };

        for (String vehicle : vehicleTypes) {
            JButton button = createStyledButton(vehicle);
            button.addActionListener(e -> handleButtonClick(vehicle));
            controlPanel.add(button);
        }

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(timerPanel, BorderLayout.CENTER);
        mainPanel.add(signalPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Initialize overlay
        initOverlay();
        
     // Ensure overlay resizes with the window
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (overlayPanel != null) {
                    overlayPanel.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });

        // Start normal operation in a separate thread
        startNormalOperation();
        
        // Add window listener to properly clean up threads
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isRunning = false;
                if (normalOperationThread != null) {
                    normalOperationThread.interrupt();
                }
            }
        });
    }

    private void initOverlay() {
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlayPanel.setLayout(new GridBagLayout());
        overlayPanel.setOpaque(false);
        overlayPanel.setVisible(false);

        overlayTimerLabel = new JLabel("", SwingConstants.CENTER);
        overlayTimerLabel.setFont(new Font("Arial", Font.BOLD, 72));
        overlayTimerLabel.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        overlayPanel.add(overlayTimerLabel, gbc);

        
        // Add overlay to the frame's layered pane
        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(overlayPanel, JLayeredPane.POPUP_LAYER);
        overlayPanel.setBounds(0, 0, getWidth(), getHeight());
    }
    

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (overlayPanel != null) {
            overlayPanel.setBounds(0, 0, width, height);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        
        return button;
    }

    private void startNormalOperation() {
        if (normalOperationThread != null && normalOperationThread.isAlive()) {
            return; // Don't start a new thread if one is already running
        }

        normalOperationThread = new Thread(() -> {
            while (isRunning) {
                if (!isEmergencyMode) {
                    cycleThroughSignals();
                } else {
                    try {
                        Thread.sleep(100); // Sleep while in emergency mode
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        normalOperationThread.setDaemon(true); // Make it a daemon thread
        normalOperationThread.start();
    }

    private void cycleThroughSignals() {
        try {
            // Define the sequence: RED -> YELLOW -> GREEN -> RED...
            Color nextColor;
            if (currentColor == Color.RED) {
                nextColor = Color.YELLOW;
            } else if (currentColor == Color.YELLOW) {
                nextColor = Color.GREEN;
            } else {
                nextColor = Color.RED;
            }

            currentColor = nextColor;
            signalPanel.repaint();

            // Ensure minimum duration for each signal
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < MIN_SIGNAL_DURATION) {
                if (isEmergencyMode) {
                    break;
                }
                Thread.sleep(100);
            }

            // Complete the remaining duration if not in emergency mode
            if (!isEmergencyMode) {
                Thread.sleep(SIGNAL_DURATION - MIN_SIGNAL_DURATION);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleButtonClick(String vehicleType) {
        if (vehicleType.contains("Exit")) {
            isRunning = false;
            if (normalOperationThread != null) {
                normalOperationThread.interrupt();
            }
            System.exit(0);
            return;
        }

        if (!vehicleType.contains("No Emergency")) {
            isEmergencyMode = true;
            currentColor = Color.GREEN;
            signalPanel.repaint();
            
            JOptionPane.showMessageDialog(this,
                "Emergency Vehicle: " + vehicleType + "\nSetting signal to GREEN for 10 seconds",
                "Emergency Alert",
                JOptionPane.WARNING_MESSAGE);

            // Start countdown timer
            new Thread(() -> {
                overlayPanel.setVisible(true);
                for (int i = (int)EMERGENCY_DURATION; i > 0; i--) {
                    final int count = i;
                    SwingUtilities.invokeLater(() -> {
                        timerLabel.setText("Emergency Mode: " + count + " seconds remaining");
                        timerLabel.setForeground(Color.RED);
                        overlayTimerLabel.setText(String.valueOf(count));
                    });
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    timerLabel.setText("");
                    overlayPanel.setVisible(false);
                    isEmergencyMode = false;
                });
            }).start();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            TrafficSignalGUI gui = new TrafficSignalGUI();
            gui.setLocationRelativeTo(null); // Center on screen
            gui.setVisible(true);
        });
    }
}