import java.awt.*;
import javax.swing.*;

public class Item {
    public String type;
    public int x, y;
    public int speed = 5;
    public Image image;

    public Item(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;

        switch(type) {
            case "alcohol": image = new ImageIcon("alcohol.png").getImage(); break;
            case "apple": image = new ImageIcon("apple.png").getImage(); break;
            case "honey": image = new ImageIcon("honey.png").getImage(); break;
            case "avocado": image = new ImageIcon("avocado.png").getImage(); break;
            case "pig": image = new ImageIcon("pig.png").getImage(); break;
            case "egg": image = new ImageIcon("egg.png").getImage(); break;
            case "shimp": image = new ImageIcon("shimp.png").getImage(); break;
            case "sushi": image = new ImageIcon("sushi.png").getImage(); break;
            case "bread": image = new ImageIcon("bread.png").getImage(); break;
            case "bacon": image = new ImageIcon("bacon.png").getImage(); break;
            default: image = new ImageIcon(type + ".png").getImage(); break;
        }
    }
}
