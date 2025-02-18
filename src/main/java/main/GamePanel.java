package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener{
    // dimensions
    public static final int WIDTH = 176;
    public static final int HEIGHT = 112;
    public static final int SCALE = 4;

    // game thread
    private Thread thread;
    private boolean running;
    private final int FPS = 60;
    private final long targetTime = 1000 / FPS;

    // image
    private BufferedImage image;
    private Graphics2D g;

    // game state manager
    private GameStateManager gsm;
    
    public GamePanel() {
        super();
        this.setPreferredSize(new Dimension(GamePanel.WIDTH * GamePanel.SCALE, GamePanel.HEIGHT * GamePanel.SCALE));
        this.setFocusable(true);
        this.requestFocus();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (this.thread == null) {
            this.thread = new Thread(this);
            addKeyListener(this);
            thread.start();
        }
    }

    private void init() {
        this.image = new BufferedImage(GamePanel.WIDTH, GamePanel.HEIGHT, BufferedImage.TYPE_INT_RGB);
        this.g = (Graphics2D) this.image.getGraphics();
        this.running = true;

        this.gsm = new GameStateManager();
    }

    @Override
    public void run() {
        this.init();

        long start;
        long elapsed;
        long wait;

        // game loop
        while (this.running) {
            start = System.nanoTime();

            this.update();
            this.draw();
            this.drawToScreen();

            elapsed = System.nanoTime() - start;
            wait = this.targetTime - elapsed / 1000000;

            try {
                if (wait > 0) {
                    Thread.sleep(wait);
                }
            } 
            catch (InterruptedException e) {
            }
        }
    }

    private void update() {
        this.gsm.update();
    }

    private void draw() {
        this.gsm.draw(g);
    }

    private void drawToScreen() {
        Graphics g2 = (Graphics) this.getGraphics();
        g2.drawImage(this.image, 0, 0, GamePanel.WIDTH * GamePanel.SCALE, GamePanel.HEIGHT * GamePanel.SCALE, null);
        g2.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        this.gsm.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        this.gsm.keyReleased(e.getKeyCode());
    }
    
}
