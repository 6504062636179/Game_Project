import java.awt.*;
import javax.swing.*;

public class ActiveEffect {
    public String type;
    private long expireTime;
    public Image image;

    public ActiveEffect(String type, int durationSeconds) {
        this.type = type;
        this.expireTime = System.currentTimeMillis() + durationSeconds * 1000L;

        switch (type) {
            case "alcohol": image = new ImageIcon("alcohol.png").getImage(); break;
            case "apple": image = new ImageIcon("apple.png").getImage(); break;
            case "honey": image = new ImageIcon("honey.png").getImage(); break;
            case "avocado": image = new ImageIcon("avocado.png").getImage(); break;
        }
    }

    public int getRemainingTime() {
        long remain = (expireTime - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remain);
    }

    public void extend(int addSeconds) {
        expireTime += addSeconds * 1000L;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}
