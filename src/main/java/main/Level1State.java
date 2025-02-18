package main;

import java.awt.Graphics2D;

public class Level1State extends GameState{
    private TileMap tilemap;

    public Level1State(GameStateManager gsm) {
        super(gsm);
        this.init();
    }

    @Override
    public void init() {
        this.tilemap = new TileMap();
        this.tilemap.loadTiles();
        this.tilemap.loadMap("/Maps/map1.json");
        this.tilemap.setPosition(0, 0);
        this.tilemap.setTween(1);
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(Graphics2D g) {
        //g.setColor(Color.RED);
        //g.fillRect(0, 0, GamePanel.WIDTH * GamePanel.SCALE, GamePanel.HEIGHT * GamePanel.SCALE);
        this.tilemap.draw(g);
    }

    @Override
    public void keyPressed(int k) {
        
    }

    @Override
    public void keyReleased(int k) {

    }
    
}
