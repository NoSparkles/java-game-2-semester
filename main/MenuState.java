package main;

import java.awt.Color;
import java.awt.Graphics2D;

public class MenuState extends GameState{
    public MenuState(GameStateManager gsm) {
        super(gsm);
        this.init();
    }


    @Override
    public void init() {

    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        System.out.println("drawing menu");
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GamePanel.WIDTH * GamePanel.SCALE, GamePanel.HEIGHT * GamePanel.SCALE);
        g.setColor(Color.WHITE);
        g.drawString("Press any key to start", (GamePanel.PANEL_WIDTH) / 2 - 50, (GamePanel.PANEL_HEIGHT) / 2);
    }

    @Override
    public void keyPressed(int k) {
        System.out.println("pressed");
        this.gsm.setState(GameStateManager.LEVEL1STATE);
    }

    @Override
    public void keyReleased(int k) {}
}
