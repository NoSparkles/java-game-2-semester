package main;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class PlatformerGame extends PApplet {

    int[][] map;
    int tileSize = 30;
    PImage tileset;
    float playerX, playerY;
    float speed = 2;
    float jumpSpeed = -10;
    float gravity = 0.5f;
    float playerYVelocity = 0;
    boolean isJumping = false;

    boolean[] keys = new boolean[128];

    public static void main(String[] args) {
        PApplet.main("main.PlatformerGame");
    }

    public void settings() {
        size(480, 270);
    }

    public void setup() {
        map = loadMap("map1.json");
        tileset = loadImage("Tilesets/grasstileset.png");
        
        if (tileset == null) {
            println("Error: Tileset image not loaded.");
            exit();
        } else {
            println("Tileset image loaded successfully.");
        }
        
        playerX = tileSize;
        playerY = tileSize;
        tileset.loadPixels();
    }

    public void keyPressed() {
        if (keyCode < 128) {
            keys[keyCode] = true;
        }
    }
    
    public void keyReleased() {
        if (keyCode < 128) {
            keys[keyCode] = false;
        }
    }
    

    public void draw() {
        background(0);
        drawMap();
        updatePlayer();
        displayPlayer();
    }

    int[][] loadMap(String filename) {
        JSONObject json = loadJSONObject(filename);
        int w = json.getInt("width");
        int h = json.getInt("height");
        JSONArray mapArray = json.getJSONArray("map");
        int[][] result = new int[h][w];
        for (int i = 0; i < h; i++) {
            JSONArray row = mapArray.getJSONArray(i);
            for (int j = 0; j < w; j++) {
                result[i][j] = row.getInt(j);
            }
        }
        return result;
    }

    void drawMap() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                int tile = map[i][j];
                if (tile != 0) {
                    int sx = (tile - 1) % 5 * tileSize;
                    int sy = (tile - 1) / 5 * tileSize;
                    copy(tileset, sx, sy, tileSize, tileSize, j * tileSize, i * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    void updatePlayer() {
        float nextX = playerX;
        float nextY = playerY;
    
        // Horizontal movement
        if (keys[LEFT]) {
            nextX -= speed;
        }
        if (keys[RIGHT]) {
            nextX += speed;
        }
    
        // Jumping
        if (keys[' '] && !isJumping) {
            playerYVelocity = jumpSpeed;
            isJumping = true;
        }
    
        // Apply gravity
        playerYVelocity += gravity;
        nextY += playerYVelocity;
    
        // Handle horizontal movement
        if (!checkCollision(nextX, playerY)) {
            playerX = nextX;
        }
    
        // Handle vertical movement
        if (!checkCollision(playerX, nextY)) {
            playerY = nextY;
        } else {
            playerYVelocity = 0;
            isJumping = false;
        }
    
        // Check if the player is still falling or jumping
        if (playerYVelocity != 0) {
            isJumping = true;
        }
    }

    void displayPlayer() {
        fill(255);
        rect(playerX, playerY, tileSize, tileSize);
    }

    boolean checkCollision(float x, float y) {
        int left = (int)(x / tileSize);
        int right = (int)((x + tileSize - 1) / tileSize);
        int top = (int)(y / tileSize);
        int bottom = (int)((y + tileSize - 1) / tileSize);

        if (map[top][left] != 0 || map[top][right] != 0 || map[bottom][left] != 0 || map[bottom][right] != 0) {
            return true;
        }
        return false;
    }
}
