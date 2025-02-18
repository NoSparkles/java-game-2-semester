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
    }

    @Override
    public void update() {

    }

    @Override
    public void draw(Graphics2D g) {
        this.tilemap.draw(g);
    }

    @Override
    public void keyPressed(int k) {
        
    }

    @Override
    public void keyReleased(int k) {

    }
    
}
