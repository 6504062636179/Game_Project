import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private CardLayout cardLayout;
    private JPanel menuPanel, mapPanel, gamePanel;

    private Background menuBg, mapBg, gameBg;
    private Player player;

    private Timer gameTimer, timerClock, speedTimer;
    private int seconds = 0, minutes = 0;

    private ArrayList<Item> items = new ArrayList<>();
    private Random rand = new Random();

    private int score = 0;
    private int scoreMultiplier = 1; // 1 or 2 when apple active
    private Font gameFont;
    private String selectedMap = "Sky Blue";

    // Active Effects (uses ActiveEffect class with expireTime)
    private ArrayList<ActiveEffect> activeEffects = new ArrayList<>();

    // Effect-derived global states
    private boolean alcoholActive = false;
    private boolean appleActive = false;
    private int itemSpeedBase = 5;           // base speed progression (unchanged)
    private double itemSpeedMultiplier = 1;  // 1.0 normally, 0.5 when alcohol active

    private int itemSpeed = 5; // used when spawning items (kept for compatibility)

    private boolean movingLeft = false, movingRight = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(1280, 720));
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        // Load Font
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("FontPress.ttf")).deriveFont(30f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(gameFont);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            gameFont = new Font("Arial", Font.BOLD, 30);
        }

        menuBg = new Background("background3.png");
        mapBg = new Background("background3.png");
        gameBg = new Background("background3.png");

        player = new Player();
        player.width = player.baseWidth;
        player.height = 30;
        player.x = 640 - player.width / 2;
        player.y = 650;

        setupMenu();
        setupMap();
        setupGame();

        this.add(menuPanel, "Menu");
        this.add(mapPanel, "Map");
        this.add(gamePanel, "Game");

        cardLayout.show(this, "Menu");

        this.setFocusable(true);
        this.addKeyListener(this);
    }

    // =================== Setup Panels ===================
    private void setupMenu() {
        menuPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                menuBg.draw(g);
                g.setColor(Color.WHITE);
                g.setFont(gameFont.deriveFont(50f));
                drawCenteredString(g, "Sky Catcher", getWidth(), 200);
            }
        };

        JButton startBtn = new JButton("Start");
        startBtn.setBounds(490, 350, 300, 70);
        ButtonUI.style(startBtn, gameFont.deriveFont(25f), Color.WHITE);
        startBtn.addActionListener(e -> cardLayout.show(this, "Map"));
        menuPanel.add(startBtn);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBounds(490, 450, 300, 70);
        ButtonUI.style(exitBtn, gameFont.deriveFont(25f), Color.WHITE);
        exitBtn.addActionListener(e -> System.exit(0));
        menuPanel.add(exitBtn);
    }

    private void setupMap() {
        mapPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                mapBg.draw(g);
                g.setColor(Color.WHITE);
                g.setFont(gameFont.deriveFont(40f));
                drawCenteredString(g, "Select Map", getWidth(), 100);
            }
        };

        addMapButton(mapPanel, "Sky Blue", "background1.png", 200, 200, 350, 250);
        addMapButton(mapPanel, "Sunset", "background2.png", 700, 200, 350, 250);
    }

    private void addMapButton(JPanel panel, String mapName, String imgPath, int x, int y, int width, int height) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(new ImageIcon(imgPath).getImage(), 0, 0, getWidth(), getHeight() - 40, null);
                g.setColor(Color.WHITE);
                g.setFont(gameFont.deriveFont(20f));
                drawCenteredString(g, mapName, getWidth(), getHeight() - 10);
            }
        };
        btn.setBounds(x, y, width, height);
        ButtonUI.style(btn, gameFont.deriveFont(20f), Color.WHITE);
        btn.addActionListener(e -> {
            selectedMap = mapName;
            startGame(selectedMap);
        });
        panel.add(btn);
    }

    private void setupGame() {
        gamePanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                gameBg.draw(g);
                Image bowl = new ImageIcon("bowl.png").getImage();
                g.drawImage(bowl, player.x, player.y, player.width, player.height, null);

                for (Item item : items) {
                    g.drawImage(item.image, item.x, item.y, 50, 50, null);
                }

                g.setColor(Color.WHITE);
                g.setFont(gameFont.deriveFont(20f));
                g.drawString("Score: " + score, 50, 50);

                g.setFont(gameFont.deriveFont(30f));
                g.drawString(String.format("%02d:%02d", minutes, seconds), 600, 50);

                // วาดเอฟเฟกต์ (ไอคอน + เวลาที่เหลือแบบเรียลไทม์)
                int effectX = 1000, effectY = 50, gap = 60;
                for (ActiveEffect ae : activeEffects) {
                    if (ae.image != null) g.drawImage(ae.image, effectX, effectY, 50, 50, null);
                    g.setColor(Color.WHITE);
                    g.setFont(gameFont.deriveFont(20f));
                    g.drawString(ae.getRemainingTime() + "s", effectX + 15, effectY + 60);
                    effectX += gap;
                }
            }
        };
    }

    // =================== Game Logic ===================
    private void startGame(String map) {
        cardLayout.show(this, "Game");
        resetGame();

        if (map.equals("Sky Blue")) gameBg = new Background("background1.png");
        else if (map.equals("Sunset")) gameBg = new Background("background2.png");
        else gameBg = new Background("background3.png");

        player.level = 2;
        player.updateWidth();
        player.height = 30;
        player.x = 640 - player.width / 2;
        player.y = 650;

        gameTimer = new Timer(30, e -> gameLoop());
        gameTimer.start();

        timerClock = new Timer(1000, e -> updateTime());
        timerClock.start();

        speedTimer = new Timer(10000, e -> itemSpeed++);
        speedTimer.start();
    }

    private void resetGame() {
        items.clear();
        activeEffects.clear();
        score = 0;
        seconds = 0;
        minutes = 0;
        player.level = 2;
        itemSpeed = 5;
        itemSpeedMultiplier = 1.0;
        scoreMultiplier = 1;
        alcoholActive = false;
        appleActive = false;
    }

    private void updateTime() {
        // นาฬิกาเกม
        seconds++;
        if (seconds >= 60) {
            minutes++;
            seconds = 0;
        }

        // ลบเอฟเฟกต์ที่หมดเวลา (ใช้เวลาจริงจาก ActiveEffect)
        boolean removedAny = false;
        for (int i = activeEffects.size() - 1; i >= 0; i--) {
            ActiveEffect ae = activeEffects.get(i);
            if (ae.isExpired()) {
                activeEffects.remove(i);
                removedAny = true;
            }
        }

        // ถ้ามีการเปลี่ยนแปลงใน activeEffects -> รีเฟรชสถานะรวม
        if (removedAny) refreshEffectStates();

        repaint();
    }

    private void gameLoop() {
        if (movingLeft) player.x -= 10;
        if (movingRight) player.x += 10;
        if (player.x < 0) player.x = 0;
        if (player.x + player.width > 1280) player.x = 1280 - player.width;

        if (rand.nextInt(20) == 0)
            items.add(new Item(randItemType(), rand.nextInt(1230), -50));

        // เคลื่อนไอเท็มโดยคูณด้วย multiplier (สำหรับ alcohol effect)
        for (int i = items.size() - 1; i >= 0; i--) {
            Item it = items.get(i);
            // cast ให้เป็น int ผลคำนวณความเร็ว
            it.y += (int) Math.max(1, Math.round((it.speed + itemSpeed * 0.2) * itemSpeedMultiplier));

            if (it.y > 720) items.remove(i);
            else if (collision(it)) {
                handleItemEffect(it);
                items.remove(i);
            }
        }

        repaint();
    }

    private String randItemType() {
        int n = rand.nextInt(10);
        switch (n) {
            case 0: return "alcohol";
            case 1: return "apple";
            case 2: return "honey";
            case 3: return "avocado";
            case 4: return "pig";
            case 5: return "egg";
            case 6: return "shimp";
            case 7: return "sushi";
            case 8: return "bread";
            case 9: return "bacon";
        }
        return "normal";
    }

    private boolean collision(Item it) {
        return it.x + 50 > player.x && it.x < player.x + player.width &&
               it.y + 50 > player.y && it.y < player.y + player.height;
    }

    private void handleItemEffect(Item it) {
        switch (it.type) {
            case "alcohol":
                addOrExtendEffectRealTime("alcohol", 5);
                // ไม่ต้องเปลี่ยน item.speed ของทุกตัวตรงนี้ — เราใช้ multiplier
                score += 100 * scoreMultiplier;
                break;

            case "apple":
                addOrExtendEffectRealTime("apple", 5);
                // apple effect จะถูกจัดการใน refreshEffectStates()
                score += 100 * scoreMultiplier;
                break;

            case "honey":
                addOrExtendEffectRealTime("honey", 5);
                // เมื่อเพิ่ม effect แล้ว จะ recompute level ใน refreshEffectStates()
                score += 100 * scoreMultiplier;
                break;

            case "avocado":
                addOrExtendEffectRealTime("avocado", 5);
                score += 100 * scoreMultiplier;
                break;

            case "pig":
                gameTimer.stop();
                timerClock.stop();
                if (speedTimer != null) speedTimer.stop();

                GameOverPanel gop = new GameOverPanel(selectedMap, score, minutes, seconds, gameFont);
                JButton homeBtn = new JButton("MAIN MENU");
                homeBtn.setBounds(440, 450, 400, 50);
                ButtonUI.style(homeBtn, gameFont.deriveFont(20f), Color.YELLOW);
                homeBtn.addActionListener(e -> cardLayout.show(this, "Menu"));
                gop.add(homeBtn);

                JButton retryBtn = new JButton("PLAY AGAIN");
                retryBtn.setBounds(440, 520, 400, 50);
                ButtonUI.style(retryBtn, gameFont.deriveFont(20f), Color.WHITE);
                retryBtn.addActionListener(e -> startGame(selectedMap));
                gop.add(retryBtn);

                this.add(gop, "GameOver");
                cardLayout.show(this, "GameOver");
                break;

            default:
                score += 100 * scoreMultiplier;
                break;
        }
        // หลังเพิ่มเอฟเฟกต์ ให้รีเฟรชสถานะ (จะถูกเรียกใน addOrExtendEffectRealTime แต่เรียกอีกครั้งก็ไม่มีปัญหา)
        refreshEffectStates();
    }

    // ========== Add / Extend ActiveEffect (ใช้เวลาจริง) ==========
    private void addOrExtendEffectRealTime(String type, int durationSeconds) {
        for (ActiveEffect ae : activeEffects) {
            if (ae.type.equals(type)) {
                ae.extend(durationSeconds);
                refreshEffectStates();
                return;
            }
        }
        activeEffects.add(new ActiveEffect(type, durationSeconds));
        refreshEffectStates();
    }

    // ========== รีคัลคูลสถานะรวม (เรียกเมื่อเพิ่ม/ลบ/ต่อเวลา) ==========
    private void refreshEffectStates() {
        // นับจำนวน active effect แต่ละชนิด
        int alcoholCount = 0, appleCount = 0, honeyCount = 0, avocadoCount = 0;
        for (ActiveEffect ae : activeEffects) {
            if (ae.type.equals("alcohol") && !ae.isExpired()) alcoholCount++;
            if (ae.type.equals("apple") && !ae.isExpired()) appleCount++;
            if (ae.type.equals("honey") && !ae.isExpired()) honeyCount++;
            if (ae.type.equals("avocado") && !ae.isExpired()) avocadoCount++;
        }

        // alcohol -> multiplier
        boolean prevAlcohol = alcoholActive;
        alcoholActive = alcoholCount > 0;
        if (alcoholActive && !prevAlcohol) {
            itemSpeedMultiplier = 0.5; // ชะลอของที่ร่วง
        } else if (!alcoholActive && prevAlcohol) {
            itemSpeedMultiplier = 1.0; // คืนค่า
        }

        // apple -> scoreMultiplier
        boolean prevApple = appleActive;
        appleActive = appleCount > 0;
        if (appleActive && !prevApple) {
            scoreMultiplier = 2;
        } else if (!appleActive && prevApple) {
            scoreMultiplier = 1;
        }

        // level (base 2) +/- honey/avocado, clamp 1..3
        int baseLevel = 2;
        int newLevel = baseLevel + Math.min(2, honeyCount) - Math.min(2, avocadoCount);
        if (newLevel < 1) newLevel = 1;
        if (newLevel > 3) newLevel = 3;
        if (player.level != newLevel) {
            player.level = newLevel;
            player.updateWidth();
        }
    }

    // =================== Key Listener ===================
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) movingLeft = true;
        if (code == KeyEvent.VK_D) movingRight = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_A) movingLeft = false;
        if (code == KeyEvent.VK_D) movingRight = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void actionPerformed(ActionEvent e) {}

    // =================== Utility ===================
    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
