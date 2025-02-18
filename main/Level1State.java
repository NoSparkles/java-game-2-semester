package main;

import java.awt.Color;
import java.awt.Graphics2D;

public class Level1State extends GameState{

    public Level1State(GameStateManager gsm) {
        super(gsm);
    }

    @Override
    public void init() {
        
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillRect(0, 0, GamePanel.PANEL_WIDTH, GamePanel.PANEL_HEIGHT);
    }

    @Override
    public void keyPressed(int k) {
        
    }

    @Override
    public void keyReleased(int k) {

    }
    
}
