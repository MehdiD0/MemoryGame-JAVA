import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ColoredRectangle {
    private Rectangle rectangle;
    private Color realColor;
    private Color fakeColor = Color.white;
    private Color currentColor;

    public ColoredRectangle(int x, int y, int width, int height) {
        this.rectangle = new Rectangle(x, y, width, height);
        this.currentColor = fakeColor;
        this.realColor = getRandomColor();
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Color getColor() {
        return currentColor;
    }

    public void setColor(Color color) {
        this.currentColor = color;
    }

    public Color getRealColor() {
        return this.realColor;
    }

    public Color getRandomColor() {
        ArrayList<Color> colors = new ArrayList<>(Arrays.asList(Color.green, Color.red, Color.orange, Color.magenta));
        Random r = new Random();
        int randomInt = r.nextInt(4);
        return colors.get(randomInt);
    }

    public void showRealColor() {
        this.currentColor = realColor;
    }

    public void revertToFakeColor() {
        this.currentColor = fakeColor;
    }

    public Color getFakeColor() {
        return fakeColor;
    }
}
