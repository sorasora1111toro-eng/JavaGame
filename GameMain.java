import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameMain extends JPanel implements Runnable, KeyListener {
    // 画面サイズ
    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    // プレイヤーの座標
    private int playerX = 300;
    private int playerY = 220;
    private int playerSize = 40;

    // キー入力のフラグ
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    private Thread gameThread;
    private boolean running = false;

    // コンストラクタ（初期設定）
    public GameMain() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK); // 背景を黒に
        this.setFocusable(true);
        this.addKeyListener(this);
    }

    // ゲーム開始
    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ゲームループ（1秒間に約60回実行する）
    @Override
    public void run() {
        double drawInterval = 1000000000 / 60.0; // 60 FPS
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();  // 1. キャラクターの移動処理
                repaint(); // 2. 画面の再描画
                delta--;
            }
        }
    }

    // 1. 移動などのデータ更新処理
    private void update() {
        int speed = 5;
        if (upPressed)    playerY -= speed;
        if (downPressed)  playerY += speed;
        if (leftPressed)  playerX -= speed;
        if (rightPressed) playerX += speed;

        // 画面外に出ないように制限
        if (playerX < 0) playerX = 0;
        if (playerX > WIDTH - playerSize) playerX = WIDTH - playerSize;
        if (playerY < 0) playerY = 0;
        if (playerY > HEIGHT - playerSize) playerY = HEIGHT - playerSize;
    }

    // 2. 画面を描画する処理
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // プレイヤー（四角形）を赤色で描画
        g.setColor(Color.RED);
        g.fillRect(playerX, playerY, playerSize, playerSize);
    }

    // --- キーボード入力の処理 ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP)    upPressed = true;
        if (code == KeyEvent.VK_DOWN)  downPressed = true;
        if (code == KeyEvent.VK_LEFT)  leftPressed = true;
        if (code == KeyEvent.VK_RIGHT) rightPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP)    upPressed = false;
        if (code == KeyEvent.VK_DOWN)  downPressed = false;
        if (code == KeyEvent.VK_LEFT)  leftPressed = false;
        if (code == KeyEvent.VK_RIGHT) rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // メインメソッド（起動口）
    public static void main(String[] args) {
        JFrame window = new JFrame("Java 2D Game Title");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GameMain game = new GameMain();
        window.add(game);
        window.pack();

        window.setLocationRelativeTo(null); // 画面中央に表示
        window.setVisible(true);

        game.startGame(); // ゲームスタート
    }
}
