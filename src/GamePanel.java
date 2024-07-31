import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements Runnable {

    final int screenWidth = 1200;
    final int screenHeight = 800;
    
    private List<ColoredRectangle> rectangles; // List to store rectangle information
    private Timer colorTimer; // Timer for reverting colors
    private final long colorDisplayDuration = 10000; // 10 seconds in milliseconds
    private Timer updateTimer; // Timer for updating display
    private long startTime; // Start time of the 10 seconds
    private long elapsedTime; // Elapsed time since the start
    private Image backgroundImage;

    Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setDoubleBuffered(true);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                blockClicked(e.getX(), e.getY());
            }
        });

        rectangles = new ArrayList<>(); // Initialize the list of rectangles
        initializeRectangles(); // Populate the list with rectangles

        // Initialize the timer for reverting colors
        colorTimer = new Timer((int) colorDisplayDuration, e -> revertColors());
        colorTimer.setRepeats(false); // Timer should only run once

        // Initialize the timer for updating display
        updateTimer = new Timer(1000, e -> updateElapsedTime()); // Update every second
        updateTimer.start(); // Start the update timer
        
        // Display real colors initially
        showRealColors();

        backgroundImage = new ImageIcon("./background.jpg").getImage();
    }

    // Method to initialize rectangles
    private void initializeRectangles() {
        for (int x = screenWidth / 4; x <= screenWidth * 2 / 3; x += 100) {
            for (int y = screenHeight / 4; y <= screenHeight * 2 / 3; y += 100) {
                rectangles.add(new ColoredRectangle(x, y, 80, 80));
            }
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final int FPS = 60; // Desired frames per second
        final long optimalTime = 1000000000 / FPS; // Time per frame in nanoseconds

        long lastFrameTime = System.nanoTime();
        long now;
        long updateLength;
        long waitTime;
        long totalTime = 0;

        while (true) {
            now = System.nanoTime();
            updateLength = now - lastFrameTime;
            lastFrameTime = now;
            totalTime += updateLength;

            waitTime = optimalTime - updateLength;

            if (waitTime < 0) {
                waitTime = 5; // Avoid negative sleep time
            }

            try {
                Thread.sleep(waitTime / 1000000L); // Convert nanoseconds to milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (totalTime >= optimalTime) {
                totalTime = 0;
                repaint(); // Redraw the panel
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Ensure proper painting

        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        
        for (ColoredRectangle coloredRect : rectangles) {
            g.setColor(coloredRect.getColor()); // Set color for the rectangle
            g.fillRect(coloredRect.getRectangle().x, coloredRect.getRectangle().y, 
                       coloredRect.getRectangle().width, coloredRect.getRectangle().height);
            g.setColor(Color.BLACK); // Set color for the border
            g.drawRect(coloredRect.getRectangle().x, coloredRect.getRectangle().y, 
                       coloredRect.getRectangle().width, coloredRect.getRectangle().height);
        }

        // Draw the timer
        drawTimer(g);
    }

    // Method to display all rectangles in their real color
    private void showRealColors() {
        startTime = System.currentTimeMillis(); // Record the start time
        for (ColoredRectangle rect : rectangles) {
            rect.showRealColor();
        }
        repaint(); // Request a redraw

        // Start the timer to revert colors after 10 seconds
        colorTimer.start();
    }

    private void revertColors() {
        for (ColoredRectangle rect : rectangles) {
            rect.revertToFakeColor();
        }
        repaint(); // Request a redraw
    }

    private void updateElapsedTime() {
        elapsedTime = System.currentTimeMillis() - startTime;
        
        if (elapsedTime >= colorDisplayDuration) {
            elapsedTime = colorDisplayDuration; // Ensure it doesn't exceed the display duration
            updateTimer.stop(); // Stop the update timer
        }
        
        repaint(); // Request a redraw to update the timer display
    }

    private void drawTimer(Graphics g) {
        long remainingTime = (colorDisplayDuration - elapsedTime) / 1000; // Time remaining in seconds
        String timerText;
        
        if (elapsedTime >= colorDisplayDuration) {
            timerText = "Time's Up";
        } else {
            timerText = "Remaining: " + remainingTime + "s";
        }
        
        g.setColor(Color.WHITE); // Timer color
        g.setFont(new Font("Arial", Font.BOLD, 20)); // Timer font and size
        g.drawString(timerText, screenWidth - 150, 30); // Draw the timer text at the top right corner
    }

    public List<ColoredRectangle> getRectangles() {
        return rectangles;
    }

    public void blockClicked(int x, int y) {
        // Determine which rectangle was clicked
        for (ColoredRectangle rect : rectangles) {
            if (rect.getRectangle().contains(x, y)) {
                rect.showRealColor();
                break; // Stop after the first matching rectangle
            }
        }
        repaint(); // Request a redraw
    }
}
