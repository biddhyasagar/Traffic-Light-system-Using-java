import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

interface TrafficLightObserver {
    void update();
}

public class TrafficLightSystem extends JFrame implements ActionListener, TrafficLightObserver {

    private JButton changeRedButton;
    private JButton changeYellowButton;
    private JButton changeGreenButton;
    private JButton pedestrianButton;
    private TrafficLightPanel trafficLightPanel1;
    private TrafficLightPanel trafficLightPanel2;
    private RoadPanel roadPanel;

    private Timer timer;
    private boolean isPedestrianButtonPressed = false;

    public TrafficLightSystem() {
        super("Traffic Light Management System");
        setLayout(null);

        trafficLightPanel1 = new TrafficLightPanel();
        trafficLightPanel1.setBounds(50, 50, 100, 250);
        trafficLightPanel1.addObserver(this);

        trafficLightPanel2 = new TrafficLightPanel();
        trafficLightPanel2.setBounds(650, 50, 100, 250);

        roadPanel = new RoadPanel();
        roadPanel.setBounds(50, 300, 700, 100);

        changeRedButton = new JButton("Change Red");
        changeRedButton.setBounds(10, 10, 100, 30);
        changeRedButton.addActionListener(this);
        changeRedButton.setBackground(new Color(135, 206, 250));

        changeYellowButton = new JButton("Change Yellow");
        changeYellowButton.setBounds(120, 10, 120, 30);
        changeYellowButton.addActionListener(this);
        changeYellowButton.setBackground(new Color(135, 206, 250));

        changeGreenButton = new JButton("Change Green");
        changeGreenButton.setBounds(250, 10, 120, 30);
        changeGreenButton.addActionListener(this);
        changeGreenButton.setBackground(new Color(135, 206, 250));

        pedestrianButton = new JButton("Pedestrian");
        pedestrianButton.setBounds(380, 10, 120, 30);
        pedestrianButton.addActionListener(this);
        pedestrianButton.setBackground(new Color(135, 206, 250));

        add(trafficLightPanel1);
        add(trafficLightPanel2);
        add(changeRedButton);
        add(changeYellowButton);
        add(changeGreenButton);
        add(pedestrianButton);
        add(roadPanel);

        getContentPane().setBackground(Color.WHITE);

        timer = new Timer(4000, this);
        timer.start();

        startAutomaticColorChange(trafficLightPanel1, trafficLightPanel2);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeRedButton) {
            trafficLightPanel1.changeLight(TrafficLightPanel.RED_INDEX);
            isPedestrianButtonPressed = false;
        } else if (e.getSource() == changeYellowButton) {
            trafficLightPanel1.changeLight(TrafficLightPanel.YELLOW_INDEX);
            isPedestrianButtonPressed = false;
        } else if (e.getSource() == changeGreenButton) {
            trafficLightPanel1.changeLight(TrafficLightPanel.GREEN_INDEX);
            isPedestrianButtonPressed = false;
        } else if (e.getSource() == pedestrianButton) {
            isPedestrianButtonPressed = true;
            trafficLightPanel1.stop();
            trafficLightPanel2.stop();
        } else if (e.getSource() == timer) {
            if (!isPedestrianButtonPressed) {
                trafficLightPanel1.changeLightAutomatically();
                trafficLightPanel2.updateBasedOnPanel1(trafficLightPanel1);
            }
        }
    }

    private void startAutomaticColorChange(final TrafficLightPanel... trafficLightPanels) {
        Timer autoTimer = new Timer(40000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isPedestrianButtonPressed) {
                    for (TrafficLightPanel panel : trafficLightPanels) {
                        panel.changeLightAutomatically();
                    }
                    trafficLightPanel2.updateBasedOnPanel1(trafficLightPanel1);
                }
            }
        });

        autoTimer.start();
    }

    public void update() {
        trafficLightPanel2.updateBasedOnPanel1(trafficLightPanel1);
    }

    private class TrafficLightPanel extends JPanel {

        private static final int RED_INDEX = 0;
        private static final int YELLOW_INDEX = 1;
        private static final int GREEN_INDEX = 2;

        private int currentLightIndex;
        private Color[] lightColors = {Color.RED, Color.YELLOW, Color.GREEN};
        private boolean[] lightStatus = {false, false, false};

        private List<TrafficLightObserver> observers = new ArrayList<>();

        private Timer lightChangeTimer;
        private CountdownPanel countdownPanel;
        private JLabel timerLabel;

        public void addObserver(TrafficLightObserver observer) {
            observers.add(observer);
        }

        public void removeObserver(TrafficLightObserver observer) {
            observers.remove(observer);
        }

        public void notifyObservers() {
            for (TrafficLightObserver observer : observers) {
                observer.update();
            }
        }

        public TrafficLightPanel() {
            currentLightIndex = RED_INDEX;
            lightStatus[currentLightIndex] = true;

            lightChangeTimer = new Timer(5000, e -> changeLightAutomatically());

            countdownPanel = new CountdownPanel();
            countdownPanel.setBounds(getWidth() - 40, getHeight() / 2 - 20, 30, 30); 
            add(countdownPanel);

            timerLabel = new JLabel("Stop");
            timerLabel.setBounds(getWidth() / 2 - 20, getHeight() - 60, 100, 30);
            add(timerLabel);
        }

        public void changeLight(int lightIndex) {
            lightChangeTimer.stop();

            for (int i = 0; i < lightStatus.length; i++) {
                lightStatus[i] = false;
            }

            lightStatus[lightIndex] = true;
            currentLightIndex = lightIndex;

            updateMessageLabel(lightIndex);
            repaint();
            notifyObservers();

            countdownPanel.startCountdown();
        }

        public void changeLightAutomatically() {
            lightChangeTimer.stop();

            for (int i = 0; i < lightStatus.length; i++) {
                lightStatus[i] = false;
            }

            currentLightIndex = (currentLightIndex + 1) % lightColors.length;
            lightStatus[currentLightIndex] = true;

            updateMessageLabel(currentLightIndex);
            repaint();
            notifyObservers();

            lightChangeTimer.restart();
            countdownPanel.startCountdown();
        }

        public void stop() {
            lightChangeTimer.stop();

            lightStatus[RED_INDEX] = true;
            lightStatus[YELLOW_INDEX] = false;
            lightStatus[GREEN_INDEX] = false;

            currentLightIndex = RED_INDEX;
            updateMessageLabel(RED_INDEX);
            repaint();
            notifyObservers();
        }

        public void updateBasedOnPanel1(TrafficLightPanel panel1) {
            int oppositeColorIndex = (panel1.currentLightIndex + 2) % lightColors.length;
            changeLight(oppositeColorIndex);
        }

        private void updateMessageLabel(int lightIndex) {
            switch (lightIndex) {
                case RED_INDEX:
                    timerLabel.setText("Stop");
                    timerLabel.setBackground(Color.RED);
                    break;
                case YELLOW_INDEX:
                    timerLabel.setText("Ready");
                    timerLabel.setBackground(Color.YELLOW);
                    break;
                case GREEN_INDEX:
                    timerLabel.setText("Go");
                    timerLabel.setBackground(Color.GREEN);
                    break;
            }
            timerLabel.setOpaque(true);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(getWidth() / 2 - 5, 0, 10, getHeight());

            for (int i = 0; i < lightColors.length; i++) {
                g.setColor(lightStatus[i] ? lightColors[i] : Color.BLACK);
                g.fillOval(getWidth() / 2 - 20, 20 + 60 * i, 40, 40);
            }
        }
    }

    private class CountdownPanel extends JPanel {
        private JLabel countdownLabel;
        private Timer countdownTimer;

        public CountdownPanel() {
            countdownLabel = new JLabel("");
            countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
            countdownLabel.setVerticalAlignment(SwingConstants.CENTER);
            setLayout(new BorderLayout());
            add(countdownLabel, BorderLayout.CENTER);

            countdownTimer = new Timer(1000, e -> {
                int countdown = Integer.parseInt(countdownLabel.getText());
                countdownLabel.setText(String.valueOf(countdown - 1));

                if (countdown <= 0) {
                    countdownTimer.stop();
                    countdownLabel.setText("");
                }
            });
        }

        public void startCountdown() {
            countdownTimer.restart();
            countdownLabel.setText("3"); // value for the countdown
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            g.setColor(new Color(135, 206, 250));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class RoadPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());

            drawZebraCrossing(g);
            drawHorizontalStrip(g);
        }

        private void drawZebraCrossing(Graphics g) {
            int stripWidth = 40;
            int stripHeight = 10;
            int stripGap = 20;

            
            int leftX = getWidth() / 16 - stripWidth / 3;
            int leftY = getHeight() / 14 - stripHeight / 2;

            g.setColor(Color.WHITE);

            for (int i = 0; i < getHeight(); i += stripHeight + stripGap) {
                g.fillRect(leftX, leftY + i, stripWidth, stripHeight);
            }

          
            int rightX = 13 * getWidth() / 14 - stripWidth / 3; 
            int rightY = getHeight() / 14 - stripHeight / 2;

            for (int i = 0; i < getHeight(); i += stripHeight + stripGap) {
                g.fillRect(rightX, rightY + i, stripWidth, stripHeight);
            }
        }

        private void drawHorizontalStrip(Graphics g) {
            int stripHeight = 10;
            int roadMiddleY = getHeight() / 2 - stripHeight / 2;
            int stripWidth = 5;
            int stripGap = 5;

            g.setColor(Color.WHITE);

            for (int i = 0; i < getWidth(); i += stripWidth + stripGap) {
                g.fillRect(i, roadMiddleY, stripWidth, stripHeight);
            }
        }
    }

    public static void main(String[] args) {
        TrafficLightSystem trafficLightSystem = new TrafficLightSystem();
        trafficLightSystem.setSize(800, 500);
        trafficLightSystem.setLocationRelativeTo(null);
        trafficLightSystem.setVisible(true);
        trafficLightSystem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
