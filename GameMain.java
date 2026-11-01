import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameMain extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    // 地面の高さ（Y座標）
    private final int GROUND_Y = 380;

    // プレイヤー設定
    private int playerX = 100;
    private int playerY = GROUND_Y - 40;
    private final int playerSize = 40;
    private boolean isFacingRight = true;

    // 物理演算（ジャンプと重力）
    private double yVelocity = 0;
    private final double GRAVITY = 0.6;
    private final double JUMP_STRENGTH = -13.0;
    private boolean isGrounded = true;

    // スクロール制御（プレイヤーの仮想X座標）
    private int worldX = 100;
    private int cameraX = 0;

    // 武器（弾）の管理
    private class Bullet {
        int x, y, speedX;
        Bullet(int x, int y, int speedX) {
            this.x = x; this.y = y; this.speedX = speedX;
        }
    }
    private ArrayList<Bullet> bullets = new ArrayList<>();

    // 敵（ゾンビ枠）の管理
    private class Enemy {
        int x, y, size, speed;
        Enemy(int x, int y, int size, int speed) {
            this.x = x; this.y = y; this.size = size; this.speed = speed;
        }
    }
    private ArrayList<Enemy> enemies = new ArrayList<>();

    // キー入力フラグ
    private boolean leftPressed, rightPressed;
    private Thread gameThread;
    private boolean running = false;

    public GameMain() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(10, 10, 25)); // 魔界の夜空（濃い紺色）
        this.setFocusable(true);
        this.addKeyListener(this);

        // テスト用の敵を配置（マップの奥に配置）
        enemies.add(new Enemy(500, GROUND_Y - 30, 30, -2));
        enemies.add(new Enemy(800, GROUND_Y - 30, 30, -1));
        enemies.add(new Enemy(1200, GROUND_Y - 30, 30, -3));
    }

    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / 60.0;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        // --- 1. プレイヤーの左右移動 ---
        int moveSpeed = 4;
        if (leftPressed) {
            worldX -= moveSpeed;
            isFacingRight = false;
        }
        if (rightPressed) {
            worldX += moveSpeed;
            isFacingRight = true;
        }
        // スタート地点より左には戻れない制限
        if (worldX < 50) worldX = 50;

        // --- 2. 重力とジャンプの処理 ---
        playerY += yVelocity;
        if (!isGrounded) {
            yVelocity += GRAVITY; // 空中にいるときは重力を加算
        }
        // 着地判定
        if (playerY >= GROUND_Y - playerSize) {
            playerY = GROUND_Y - playerSize;
            yVelocity = 0;
            isGrounded = true;
        }

        // --- 3. カメラ（スクロール）の位置計算 ---
        // プレイヤーが画面の中心にくるようにマップをずらす
        cameraX = worldX - WIDTH / 2;
        if (cameraX < 0) cameraX = 0; // 左端の限界

        // 実際の描画用X座標
        playerX = worldX - cameraX;

        // --- 4. 武器（弾）の更新 ---
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.x += b.speedX;
            // 画面外に出たら削除
            if (Math.abs(b.x - worldX) > WIDTH) {
                bullets.remove(i);
            }
        }

        // --- 5. 敵の更新 ＆ 当たり判定 ---
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.x += e.speed; // 徘徊移動

            // 敵が画面左に消え去りすぎたら逆方向に走らせる等の簡易調整
            if (e.x < 0) e.speed = Math.abs(e.speed);

            // 弾と敵の当たり判定
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet b = bullets.get(j);
                if (b.x > e.x && b.x < e.x + e.size && b.y > e.y && b.y < e.y + e.size) {
                    enemies.remove(i);
                    bullets.remove(j);
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // --- 背景・地形の描画 ---
        // 地面（暗い緑色の禍々しい大地）
        g.setColor(new Color(30, 60, 30));
        g.fillRect(0, GROUND_Y, WIDTH, HEIGHT - GROUND_Y);

        // 墓標・背景物（スクロールに連動して動く目印）
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < 5; i++) {
            int tombX = (i * 300) - cameraX;
            g.fillRect(tombX, GROUND_Y - 50, 20, 50); // 簡易的な墓石
            g.fillRect(tombX - 10, GROUND_Y - 40, 40, 10);
        }

        // --- 敵の描画 ---
        g.setColor(new Color(100, 150, 100)); // ゾンビグリーン
        for (Enemy e : enemies) {
            int renderEnemyX = e.x - cameraX;
            g.fillRect(renderEnemyX, e.y, e.size, e.size);
        }

        // --- 武器（弾）の描画 ---
        g.setColor(Color.WHITE); // 輝く短剣・槍のイメージ
        for (Bullet b : bullets) {
            int renderBulletX = b.x - cameraX;
            g.fillRect(renderBulletX, b.y, 15, 6);
        }

        // --- プレイヤーの描画 ---
        g.setColor(Color.LIGHT_GRAY); // 騎士の鎧シルバー
        g.fillRect(playerX, playerY, playerSize, playerSize);
        // 目線（向いている方向をわかりやすく）
        g.setColor(Color.RED);
        int eyeX = isFacingRight ? playerX + 30 : playerX + 5;
        g.fillRect(eyeX, playerY + 10, 5, 5);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) leftPressed = true;
        if (code == KeyEvent.VK_RIGHT) rightPressed = true;

        // ジャンプ（地面にいるときだけ可能）
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_Z) {
            if (isGrounded) {
                yVelocity = JUMP_STRENGTH;
                isGrounded = false;
            }
        }

        // 攻撃（スペースキー）
        if (code == KeyEvent.VK_SPACE) {
            int bulletSpeed = isFacingRight ? 10 : -10;
            // プレイヤーの中心付近から弾を発射
            bullets.add(new Bullet(worldX + playerSize / 2, playerY + playerSize / 2, bulletSpeed));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_RIGHT) rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame window = new JFrame("魔界村風アクション試作");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        GameMain game = new GameMain();
        window.add(game);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        game.startGame();
    }
}
