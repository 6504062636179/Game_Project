import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {
    private String selectedMap;
    private int score;
    private int minutes, seconds;
    private Font font;

    public GameOverPanel(String selectedMap, int score, int minutes, int seconds, Font font) {
        this.selectedMap = selectedMap;
        this.score = score;
        this.minutes = minutes;
        this.seconds = seconds;
        this.font = font;

        setLayout(null);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (selectedMap.equals("Sky Blue")) new Background("background1.png").draw(g);
        else if (selectedMap.equals("Sunset")) new Background("background2.png").draw(g);
        else new Background("background3.png").draw(g);

        g.setFont(font.deriveFont(50f));
        g.setColor(Color.RED);
        drawCenteredString(g, "GAME OVER", getWidth(), 200);

        g.setFont(font.deriveFont(25f));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Time: " + (minutes*60 + seconds) + " sec", getWidth(), 300);
        drawCenteredString(g, "Score: " + score, getWidth(), 350);
    }

    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
