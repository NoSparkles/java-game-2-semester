package main;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class PlatformerGame extends PApplet {
    int[][] map;
    int tileSize = 60; // Double the tile size
    PImage tileset;
    PImage playerSprites;
    PImage[] playerAnimations = new PImage[16]; // 2 idle, 9 walking, 1 jumping, 4 gliding

    final int IDLE = 0;
    final int WALKING = 1;
    final int JUMPING = 2;
    final int GLIDING = 3;

    float playerX, playerY;
    float speed = 6; // Double the speed
    float jumpSpeed = -20; // Double the jump speed
    float gravity = 1; // Double the gravity
    float playerYVelocity = 0;
    boolean isJumping = false;
    boolean[] keys = new boolean[128];
    float cameraX = 0;

    public static void main(String[] args) {
        PApplet.main("main.PlatformerGame");
    }

    public void settings() {
        size(960, 540); // Double the window size
    }

    public void setup() {
        map = loadMap("map1.json");
        tileset = loadImage("Tilesets/grasstileset.png");
        playerSprites = loadImage("Sprites/Player/playersprites.png");

        if (tileset == null || playerSprites == null) {
            println("Error: Image not loaded.");
            exit();
        } else {
            println("Tileset and player sprites loaded successfully.");
        }
        
        playerX = tileSize;
        playerY = tileSize;
        tileset.loadPixels();
        loadPlayerAnimations();
    }

    void loadPlayerAnimations() {
        // Load idle sprites
        for (int i = 0; i < 2; i++) {
            playerAnimations[i] = playerSprites.get(i * 30, 0, 30, 30);
        }
    
        // Load walking sprites
        for (int i = 0; i < 8; i++) {
            playerAnimations[2 + i] = playerSprites.get(i * 30, 30, 30, 30);
        }
    
        // Load jumping sprites
        for (int i = 0; i < 2; i++) {
            playerAnimations[10 + i] = playerSprites.get(i * 30, 60, 30, 30);
        }
    
        // Load gliding sprites
        for (int i = 0; i < 4; i++) {
            playerAnimations[12 + i] = playerSprites.get(i * 30, 120, 30, 30);
        }
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
        updateCamera();
        translate(-cameraX, 0); // Apply camera translation
        drawMap();
        updatePlayer();
        displayPlayer();
    }

    void updateCamera() {
        float targetCameraX = playerX - width / 2 + tileSize / 2;
        // Clamp the targetCameraX value to the map boundaries
        targetCameraX = max(0, min(targetCameraX, map[0].length * tileSize - width));
        // Apply linear interpolation to smooth the camera movement
        cameraX = lerp(cameraX, targetCameraX, (float) 0.1);
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
                    int sx = (tile - 1) % 5 * tileSize / 2; // Adjust tileset source X
                    int sy = (tile - 1) / 5 * tileSize / 2; // Adjust tileset source Y
                    copy(tileset, sx, sy, tileSize / 2, tileSize / 2, j * tileSize, i * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    void updatePlayer() {
        float nextX = playerX;
        float nextY = playerY;

        // Horizontal movement
        if (keys['A']) {
            nextX -= speed;
        }
        if (keys['D']) {
            nextX += speed;
        }

        // Jumping
        if (keys[' '] && !isJumping) {
            playerYVelocity = jumpSpeed;
            isJumping = true;
        }

        // Gliding
        if (keys[SHIFT] && playerYVelocity > 0) {
            playerYVelocity += gravity / 4; // Slow down the fall during gliding
        } else {
            // Apply gravity
            playerYVelocity += gravity;
        }

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

    boolean facingRight = true;

void displayPlayer() {
    PImage currentSprite;

    if (keys[' '] && isJumping) {
        currentSprite = playerAnimations[9 + (frameCount / 10) % 2]; // Cycle through 2 jumping sprites
    } else if (keys[SHIFT] && playerYVelocity > 0) {
        currentSprite = playerAnimations[12 + (frameCount / 5) % 4]; // Cycle through gliding sprites faster
    } else if (keys['A'] || keys['D']) {
        currentSprite = playerAnimations[2 + (frameCount / 5) % 8]; // Cycle through 8 walking sprites faster
    } else {
        currentSprite = playerAnimations[(frameCount / 30) % 2]; // Cycle through 2 idle sprites
    }

    // Update the facing direction based on movement
    if (keys['A']) {
        facingRight = false;
    } else if (keys['D']) {
        facingRight = true;
    }

    // Draw the player with the correct facing direction
    pushMatrix();
    translate(playerX, playerY);
    if (!facingRight) {
        scale(-1, 1); // Flip the image horizontally
        translate(-tileSize, 0); // Adjust position after flipping
    }
    image(currentSprite, 0, 0, tileSize, tileSize);
    popMatrix();
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