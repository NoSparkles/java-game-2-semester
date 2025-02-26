package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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

    HashSet<Integer> passableTiles = new HashSet<>(Arrays.asList(
        1, 2, 3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38
    ));

    // Define the new WIN state
    final int MENU = 0, LEVEL1 = 1, LEVEL2 = 2, LEVEL3 = 3, WIN = 4;
    int gameState = MENU;
    int currentLevel = 1;

    boolean editingMode = false;

    float playerX, playerY, speed = 5, jumpSpeed = -13, gravity = 1, playerYVelocity = 0;
    boolean facingRight = true, isJumping = false;
    boolean[] keys = new boolean[128];
    float cameraX = 0;

    // Define player dimensions relative to tile size
    int playerWidth = tileSize / 3 * 2;
    int playerHeight = tileSize / 3 * 2;


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
            if (!editingMode) {
                // Snap camera back to player when exiting editing mode
                float targetCameraX = playerX - width / 2 + tileSize / 2;
                targetCameraX = max(0, min(targetCameraX, map[0].length * tileSize - width));
                cameraX = targetCameraX;
                
                // Save the map when exiting editing mode
                String filename = "map" + currentLevel + ".json";
                saveMap(map, filename);
                println("Map saved to " + filename);
            }
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

    void saveMap(int[][] map, String filename) {
        JSONObject json = new JSONObject();
        json.setInt("width", map[0].length);
        json.setInt("height", map.length);
        
        JSONArray mapArray = new JSONArray();
        for (int i = 0; i < map.length; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < map[i].length; j++) {
                row.append(map[i][j]);
            }
            mapArray.append(row);
        }
        json.setJSONArray("map", mapArray);
        
        saveJSONObject(json, filename);
    }
    

    public void draw() {
        background(0);
        if (gameState == MENU) {
            drawMenu();
        } else if (gameState == WIN) {
            drawWinScreen();
        } else {
            updateCamera();
            translate(-cameraX, 0);  // Translate the camera view
            drawMap();
            updatePlayer();
            displayPlayer();
            drawLevelLabel();
            translate(cameraX, 0);  // Reset translation before drawing the toolbar
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
    
            boolean clickedOnToolbar = mouseX >= toolbarX && mouseX < width;
    
            if (clickedOnToolbar) {
                int col = (mouseX - toolbarX) / columnWidth;
                int row = mouseY / columnWidth;
                int index = row * cols + col;
    
                if (col >= 0 && col < cols && row >= 0 && index < tileImages.size()) {
                    selectedTileIndex = index;
                    println("Selected tile index: " + (selectedTileIndex + 1)); // Print the selected tile number
                    redraw(); // Ensure the toolbar gets redrawn immediately
                } else {
                    selectedTileIndex = -1;  // Deselect tile if clicking outside valid tile area
                }
            } else {
                int tileX = (mouseX + (int)cameraX) / tileSize;
                int tileY = mouseY / tileSize;
    
                // Extend the map width if necessary and set new tiles to 1
                if (tileX >= map[0].length) {
                    for (int i = 0; i < map.length; i++) {
                        int[] newRow = new int[tileX + 1];
                        System.arraycopy(map[i], 0, newRow, 0, map[i].length);
                        Arrays.fill(newRow, map[i].length, newRow.length, 1); // Set new tiles to 1
                        map[i] = newRow;
                    }
                }
    
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
        try {
            String filename = "map" + level + ".json";
            map = loadMap(filename);
            playerX = tileSize;
            playerY = tileSize;
        } catch (Exception e) {
            println("Error loading level: " + e.getMessage());
            gameState = MENU; // Return to menu on error
        }
    }
    

    void changeLevel(int direction) {
        currentLevel += direction;
        if (currentLevel < 1) currentLevel = 3;
        if (currentLevel > 3) {
            gameState = WIN;  // Switch to WIN state after level 3
            return;
        }
    
        if (currentLevel == 1) gameState = LEVEL1;
        else if (currentLevel == 2) gameState = LEVEL2;
        else if (currentLevel == 3) gameState = LEVEL3;
    
        loadLevel(currentLevel);
    }

    void drawWinScreen() {
        background(255);  // White background
        fill(0);  // Black text
        textSize(48);
        textAlign(CENTER, CENTER);
        text("You Won!", width / 2, height / 2);
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
        if (editingMode) {
            // Allow camera to move freely to the right, but restrict movement to the left
            if (keys['A']) cameraX -= speed;
            if (keys['D']) cameraX += speed;
            cameraX = max(0, cameraX); // Ensure camera doesn't go left beyond the left border
        } else {
            // Camera follows the player in playing mode
            float targetCameraX = playerX - width / 2 + tileSize / 2;
            targetCameraX = max(0, min(targetCameraX, map[0].length * tileSize - width));
            cameraX = lerp(cameraX, targetCameraX, 0.1f);
        }
    }
    
    
    
    void drawMap() {
        int startX = (int) cameraX / tileSize;
        int endX = startX + (width / tileSize) + 1;
        for (int i = 0; i < map.length; i++) {
            for (int j = startX; j < endX; j++) {
                if (j < map[0].length) {
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
    }
    
    void respawnPlayer() {
        playerX = tileSize; // Reset to starting position
        playerY = tileSize;
        playerYVelocity = 0;
        isJumping = false;
    
        if (currentLevel > 0) {
            loadLevel(currentLevel); // Reload the current level safely
        }
    }
    
    
    
    void updatePlayer() {
        if (editingMode) return;
    
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
    
        // Check if the player goes out of bounds
        if (playerX < 0 || playerX + playerWidth > map[0].length * tileSize || 
            playerY < 0 || playerY + playerHeight > map.length * tileSize) {
            respawnPlayer(); // Respawn player if out of bounds
        }
    
        if (playerYVelocity != 0) isJumping = true;
    
        if (checkTileType(playerX, playerY) == 17) {
            changeLevel(1);
        }
    }
    
    

    int checkTileType(float x, float y) {
        int tileX = (int) (x / tileSize);
        int tileY = (int) (y / tileSize);
    
        if (tileX >= 0 && tileX < map[0].length && tileY >= 0 && tileY < map.length) {
            return map[tileY][tileX];
        }
        return -1;  // Return -1 if out of bounds
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
            translate(-playerWidth, 0);
        }
        image(currentSprite, 0, 0, playerWidth, playerHeight);
        popMatrix();
    }
    
    
    boolean checkCollision(float x, float y) {
        int left = (int)(x / tileSize);
        int right = (int)((x + playerWidth - 1) / tileSize);
        int top = (int)(y / tileSize);
        int bottom = (int)((y + playerHeight - 1) / tileSize);

        if (left < 0 || right >= map[0].length || top < 0 || bottom >= map.length) {
            respawnPlayer();
            return false;
        }
    
        // Check collision with non-passable tiles
        return !passableTiles.contains(map[top][left]) || 
               !passableTiles.contains(map[top][right]) || 
               !passableTiles.contains(map[bottom][left]) || 
               !passableTiles.contains(map[bottom][right]);
    }
    
    
    
}
