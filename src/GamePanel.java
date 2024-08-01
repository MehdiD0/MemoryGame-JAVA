import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements Runnable {

    private static final int SCREEN_WIDTH = 1200;
    private static final int SCREEN_HEIGHT = 800;
    private static final int RECT_WIDTH = 70;
    private static final int RECT_HEIGHT = 90;
    private static final int COLOR_DISPLAY_DURATION = 10000; // 10 seconds in milliseconds
    private static final int RECT_TOP_LEFT_SIZE = 70; // Size of the rectangle shown after time is up

    private List<ColoredRectangle> rectangles; // List to store rectangle information
    private Timer colorTimer; // Timer for reverting colors
    private Timer updateTimer; // Timer for updating display
    private long startTime; // Start time of the 10 seconds
    private long elapsedTime; // Elapsed time since the start
    private Image backgroundImage;
    private Thread gameThread;
    private boolean showTopLeftRectangle; // Flag to show the top left rectangle
    private Color topLeftRectangleColor; // Color of the top left rectangle
    private JButton restartButton; // Button to restart the game

    public GamePanel() {
        backgroundImage = new ImageIcon("./background.jpg").getImage();
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setLayout(null); // Use absolute positioning
        setDoubleBuffered(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                blockClicked(e.getX(), e.getY());
            }
        });

        rectangles = new ArrayList<>();
        initializeRectangles();

        colorTimer = new Timer(COLOR_DISPLAY_DURATION, e -> revertColors());
        colorTimer.setRepeats(false);

        updateTimer = new Timer(1000, e -> updateElapsedTime());
        updateTimer.start();

        showRealColors();

        // Initialize and add the restart button
        restartButton = new JButton("Play Again");
        restartButton.setBounds(SCREEN_WIDTH - SCREEN_WIDTH/2 - 60 , SCREEN_HEIGHT - 100, 120, 30);
        restartButton.setVisible(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        add(restartButton);
    }

    private void initializeRectangles() {
        for (int x = SCREEN_WIDTH / 4; x <= SCREEN_WIDTH * 2 / 3; x += 100) {
            for (int y = SCREEN_HEIGHT / 4; y <= SCREEN_HEIGHT * 2 / 3; y += 100) {
                rectangles.add(new ColoredRectangle(x, y, RECT_WIDTH, RECT_HEIGHT));
            }
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        colorTimer = new Timer(COLOR_DISPLAY_DURATION, e -> revertColors());
        colorTimer.setRepeats(false);

        updateTimer = new Timer(1000, e -> updateElapsedTime());
        updateTimer.start();

        showRealColors();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        for (ColoredRectangle coloredRect : rectangles) {
            g.setColor(coloredRect.getColor());
            g.fillRect(coloredRect.getRectangle().x, coloredRect.getRectangle().y,
                    coloredRect.getRectangle().width, coloredRect.getRectangle().height);
            g.setColor(Color.BLACK);
            g.drawRect(coloredRect.getRectangle().x, coloredRect.getRectangle().y,
                    coloredRect.getRectangle().width, coloredRect.getRectangle().height);
        }

        if (showTopLeftRectangle) {
            drawTopLeftRectangle(g);
        }

        drawTimer(g);
    }

    private void showRealColors() {
        startTime = System.currentTimeMillis();
        for (ColoredRectangle rect : rectangles) {
            rect.showRealColor();
        }
        repaint();

        colorTimer.start();
    }

    private void revertColors() {
        for (ColoredRectangle rect : rectangles) {
            rect.revertToFakeColor();
        }
        showTopLeftRectangle = true;

        // Select and store the random color once
        Random r = new Random();
        int randomInt = r.nextInt(rectangles.size());
        topLeftRectangleColor = rectangles.get(randomInt).getRealColor();

        repaint();
    }

    private void updateElapsedTime() {
        elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime >= COLOR_DISPLAY_DURATION) {
            elapsedTime = COLOR_DISPLAY_DURATION;
            updateTimer.stop();
        }

        repaint();
    }

    private void drawTimer(Graphics g) {
        long remainingTime = (COLOR_DISPLAY_DURATION - elapsedTime) / 1000;
        String timerText = (elapsedTime >= COLOR_DISPLAY_DURATION) ? "Time's Up" : "Remaining: " + remainingTime + "s";

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(timerText, SCREEN_WIDTH - 200, 80);
    }

    private void drawTopLeftRectangle(Graphics g) {
        g.setColor(topLeftRectangleColor);
        g.fillRect(40, 40, RECT_TOP_LEFT_SIZE, RECT_TOP_LEFT_SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(40, 40, RECT_TOP_LEFT_SIZE, RECT_TOP_LEFT_SIZE);
    }

    public List<ColoredRectangle> getRectangles() {
        return rectangles;
    }

    private void blockClicked(int x, int y) {
        for (ColoredRectangle rect : rectangles) {
            if (rect.getRectangle().contains(x, y)) {
                if (rect.isColorMatching(topLeftRectangleColor)) {
                    restartGame();
                } else {
                    showIncorrectSelection();
                }
                break;
            }
        }
        repaint();
    }

    private void restartGame() {
        showTopLeftRectangle = false;
        rectangles.clear();
        initializeRectangles();
        showRealColors();
        updateTimer.start();
        restartButton.setVisible(false);

    }

    private void showIncorrectSelection() {
        JOptionPane.showMessageDialog(this, "Wrong choice! Here are the correct rectangles.", "Incorrect", JOptionPane.ERROR_MESSAGE);

        for (ColoredRectangle rect : rectangles) {
            if (rect.isColorMatching(topLeftRectangleColor)) {
                rect.showRealColor();
            }
        }

        repaint();
        restartButton.setVisible(true);
    }
}
