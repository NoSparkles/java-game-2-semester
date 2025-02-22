package main;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class PlatformerGame extends PApplet {
    int[][] map;
    int tileSize = 60;
    PImage tileset, playerSprites;
    PImage[] playerAnimations = new PImage[16];
    ArrayList<PImage> tileImages = new ArrayList<>();
    int selectedTileIndex = -1;

    final int MENU = 0, LEVEL1 = 1, LEVEL2 = 2, LEVEL3 = 3;
    int gameState = MENU;
    int currentLevel = 1;

    boolean editingMode = false;

    float playerX, playerY, speed = 5, jumpSpeed = -13, gravity = 1, playerYVelocity = 0;
    boolean facingRight = true, isJumping = false;
    boolean[] keys = new boolean[128];
    float cameraX = 0;

    public static void main(String[] args) {
        PApplet.main("main.PlatformerGame");
    }

    public void settings() {
        size(1080, 540);
    }

    public void setup() {
        tileset = loadImage("Tilesets/grasstileset.png");
        playerSprites = loadImage("Sprites/Player/playersprites.png");
        loadPlayerAnimations();
        loadTiles();
    }

    void loadPlayerAnimations() {
        for (int i = 0; i < 2; i++) {
            playerAnimations[i] = playerSprites.get(i * 30, 0, 30, 30);
        }
        for (int i = 0; i < 8; i++) {
            playerAnimations[2 + i] = playerSprites.get(i * 30, 30, 30, 30);
        }
        for (int i = 0; i < 2; i++) {
            playerAnimations[10 + i] = playerSprites.get(i * 30, 60, 30, 30);
        }
        for (int i = 0; i < 4; i++) {
            playerAnimations[12 + i] = playerSprites.get(i * 30, 120, 30, 30);
        }
    }

    void loadTiles() {
        int cols = tileset.width / (tileSize / 2);
        int rows = tileset.height / (tileSize / 2);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                tileImages.add(tileset.get(x * (tileSize / 2), y * (tileSize / 2), tileSize / 2, tileSize / 2));
            }
        }
    }
    

    public void keyPressed() {
        if (keyCode < 128) keys[keyCode] = true;
        if (key == 'P' || key == 'p') {
            editingMode = !editingMode;
        }
        if (gameState == MENU && key == ' ') startGame();
        if (gameState != MENU) {
            if (key == 'J' || key == 'j') changeLevel(-1);
            if (key == 'K' || key == 'k') changeLevel(1);
        }
    }
    

    public void keyReleased() {
        if (keyCode < 128) keys[keyCode] = false;
    }

    public void draw() {
        background(0);
        if (gameState == MENU) {
            drawMenu();
        } else {
            updateCamera();
            translate(-cameraX, 0);
            drawMap();
            updatePlayer();
            displayPlayer();
            drawLevelLabel();
            if (editingMode) {
                drawToolbar();
            }
        }
    }

    public void mousePressed() {
        if (editingMode) {
            int toolbarX = width - 120;
            int columnWidth = tileSize / 2;
            int cols = 2;  // Two columns for the toolbar
            int rows = (int) ceil((float)tileImages.size() / cols);
    
            if (mouseX >= toolbarX && mouseX < width) {
                int col = (mouseX - toolbarX) / columnWidth;
                int row = mouseY / columnWidth;
                int index = row * cols + col;
    
                if (index < tileImages.size()) {
                    selectedTileIndex = index;
                    println("Selected tile number: " + (selectedTileIndex + 1)); // Print the selected tile number
                    redraw(); // Ensure the toolbar gets redrawn immediately
                }
            } else {
                int tileX = (mouseX + (int)cameraX) / tileSize;
                int tileY = mouseY / tileSize;
                if (selectedTileIndex != -1 && tileX < map[0].length && tileY < map.length) {
                    map[tileY][tileX] = selectedTileIndex + 1; // Correctly place the selected tile
                    println("Placed tile number: " + (selectedTileIndex + 1)); // Print the placed tile number
                }
            }
        }
    }
    
    
    
    
    void drawToolbar() {
        int toolbarX = width - 120;
        int columnWidth = tileSize / 2;
        int cols = 2;  // Two columns for the toolbar
        int rows = (int) ceil((float)tileImages.size() / cols);
    
        fill(200);  // Light gray color for the background
        noStroke();
        rect(toolbarX, 0, 120, height);  // Draw the background
    
        for (int i = 0; i < tileImages.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            float x = toolbarX + col * columnWidth;
            float y = row * columnWidth;
    
            image(tileImages.get(i), x, y, columnWidth, columnWidth);
            
            if (selectedTileIndex == i) {
                stroke(255, 0, 0);  // Red outline for the selected tile
                strokeWeight(3);
                noFill();
                rect(x, y, columnWidth, columnWidth);
            }
        }
        noStroke();
    }
    
    

    void drawMenu() {
        fill(255);
        textSize(32);
        textAlign(CENTER, CENTER);
        text("Press SPACE to start", width / 2, height / 2);
    }

    void startGame() {
        gameState = LEVEL1;
        loadLevel(currentLevel);
    }

    void loadLevel(int level) {
        String filename = "map" + level + ".json";
        map = loadMap(filename);
        playerX = tileSize;
        playerY = tileSize;
    }

    void changeLevel(int direction) {
        currentLevel += direction;
        if (currentLevel < 1) currentLevel = 3;
        if (currentLevel > 3) currentLevel = 1;

        if (currentLevel == 1) gameState = LEVEL1;
        else if (currentLevel == 2) gameState = LEVEL2;
        else if (currentLevel == 3) gameState = LEVEL3;

        loadLevel(currentLevel);
    }

    void drawLevelLabel() {
        fill(255, 255, 255);
        textSize(32);
        textAlign(CENTER, CENTER);
        text("Level " + currentLevel, width / 2, 30);
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

    void updateCamera() {
        float targetCameraX = playerX - width / 2 + tileSize / 2;
        targetCameraX = max(0, min(targetCameraX, map[0].length * tileSize - width));
        cameraX = lerp(cameraX, targetCameraX, 0.1f);
    }
    
    void drawMap() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                int tile = map[i][j];
                if (tile != 0) {
                    int tileIndex = tile - 1; // Adjust the tile index
                    int sx = (tileIndex % (tileset.width / (tileSize / 2))) * (tileSize / 2);
                    int sy = (tileIndex / (tileset.width / (tileSize / 2))) * (tileSize / 2);
                    copy(tileset, sx, sy, tileSize / 2, tileSize / 2, j * tileSize, i * tileSize, tileSize, tileSize);
                }
            }
        }
    }
    
    
    
    void updatePlayer() {
        float nextX = playerX;
        float nextY = playerY;
    
        if (keys['A']) nextX -= speed;
        if (keys['D']) nextX += speed;
        if (keys[' '] && !isJumping) {
            playerYVelocity = jumpSpeed;
            isJumping = true;
        }
        if (keys[SHIFT] && playerYVelocity > 0) playerYVelocity += gravity / 8;
        else playerYVelocity += gravity;
    
        nextY += playerYVelocity;
    
        if (!checkCollision(nextX, playerY)) playerX = nextX;
        if (!checkCollision(playerX, nextY)) playerY = nextY;
        else {
            playerYVelocity = 0;
            isJumping = false;
        }
    
        if (playerYVelocity != 0) isJumping = true;
    }
    
    void displayPlayer() {
        PImage currentSprite = keys[' '] && isJumping ? playerAnimations[9 + (frameCount / 10) % 2] :
                              keys[SHIFT] && playerYVelocity > 0 ? playerAnimations[12 + (frameCount / 5) % 4] :
                              (keys['A'] || keys['D']) ? playerAnimations[2 + (frameCount / 5) % 8] :
                              playerAnimations[(frameCount / 30) % 2];
    
        if (keys['A']) facingRight = false;
        if (keys['D']) facingRight = true;
    
        pushMatrix();
        translate(playerX, playerY);
        if (!facingRight) {
            scale(-1, 1);
            translate(-tileSize, 0);
        }
        image(currentSprite, 0, 0, tileSize, tileSize);
        popMatrix();
    }
    
    boolean checkCollision(float x, float y) {
        int left = (int)(x / tileSize);
        int right = (int)((x + tileSize - 1) / tileSize);
        int top = (int)(y / tileSize);
        int bottom = (int)((y + tileSize - 1) / tileSize);
    
        // Check collision with non-zero tiles
        return (map[top][left] != 0 && map[top][left] != 1) || 
               (map[top][right] != 0 && map[top][right] != 1) || 
               (map[bottom][left] != 0 && map[bottom][left] != 1) || 
               (map[bottom][right] != 0 && map[bottom][right] != 1);
    }
    
    
}
