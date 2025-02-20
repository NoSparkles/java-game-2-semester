package main;

import processing.core.PApplet;
import processing.core.PImage;

public class App extends PApplet {
    PImage tileSheet;
    int[][] tileMap;
    int currentTile = 0;
    int mapTileSize = 32; // Map tile size is 32x32 pixels
    int toolbarTileSize = 16; // Toolbar tile size is 16x16 pixels
    boolean inMenu = true;
    boolean editingMode = true; // Start in editing mode
    int selectedBlock = 0;
    int toolbarItemsPerRow = 17; // Number of items per row in the toolbar

    public static void main(String[] args) {
        PApplet.main("main.App");
    }

    public void settings() {
        size(1280, 640); // Increased window size to accommodate larger map
    }

    public void setup() {
        tileSheet = loadImage("Resources/sheet.png");
        if (tileSheet == null) {
            println("Error loading image. Please check the file path.");
            exit(); // Stop the program if the image is not found
        }
        tileMap = new int[width / mapTileSize][(height - (toolbarTileSize * 10)) / mapTileSize]; // Adjusted to leave space for the toolbar
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                tileMap[i][j] = -1; // Empty tile
            }
        }
    }
    
    

    public void draw() {
        if (inMenu) {
            background(0);
            fill(255);
            textAlign(CENTER, CENTER);
            textSize(32);
            text("Press any key to start", width / 2, height / 2);
        } else {
            background(200);
            drawTileMap();
            if (editingMode) {
                drawToolbar();
            }
        }
    }

    public void keyPressed() {
        if (inMenu) {
            inMenu = false;
        } else if (key == 'P' || key == 'p') {
            editingMode = !editingMode; // Toggle editing mode
        }
    }

    public void mousePressed() {
        if (!inMenu && editingMode) {
            handleMouse();
        }
    }

    public void mouseDragged() {
        if (!inMenu && editingMode) {
            handleMouse();
        }
    }

    public void handleMouse() {
        int toolbarHeight = toolbarTileSize * 10;
        if (mouseY > height - toolbarHeight && mouseX < toolbarItemsPerRow * toolbarTileSize) {
            // Clicked on the toolbar
            int row = (mouseY - (height - toolbarHeight)) / toolbarTileSize;
            int col = mouseX / toolbarTileSize;
            if (col >= 0 && col < toolbarItemsPerRow && row * toolbarItemsPerRow + col < (tileSheet.width / toolbarTileSize) * (tileSheet.height / toolbarTileSize)) {
                selectedBlock = row * toolbarItemsPerRow + col;
            }
        } else {
            // Clicked on the tile map
            int x = mouseX / mapTileSize;
            int y = mouseY / mapTileSize;
            if (x >= 0 && x < tileMap.length && y >= 0 && y < tileMap[0].length) {
                tileMap[x][y] = selectedBlock;
            }
        }
    }
    

    void drawTileMap() {
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length; j++) {
                if (tileMap[i][j] != -1) {
                    int tileIndex = tileMap[i][j];
                    int sx = (tileIndex % (tileSheet.width / toolbarTileSize)) * toolbarTileSize;
                    int sy = (tileIndex / (tileSheet.width / toolbarTileSize)) * toolbarTileSize;
                    image(tileSheet, i * mapTileSize, j * mapTileSize, mapTileSize, mapTileSize, sx, sy, sx + toolbarTileSize, sy + toolbarTileSize);
                }
            }
        }
    }

    void drawToolbar() {
        int itemsPerRow = toolbarItemsPerRow;
        int totalTiles = (tileSheet.width / toolbarTileSize) * (tileSheet.height / toolbarTileSize);
        int toolbarX = 0; // Toolbar starting at the leftmost side
        int toolbarY = height - toolbarTileSize * 10; // Positioned at the bottom
    
        for (int i = 0; i < totalTiles; i++) {
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int sx = (i % (tileSheet.width / toolbarTileSize)) * toolbarTileSize;
            int sy = (i / (tileSheet.width / toolbarTileSize)) * toolbarTileSize;
            image(tileSheet, toolbarX + col * toolbarTileSize, toolbarY + row * toolbarTileSize, toolbarTileSize, toolbarTileSize, sx, sy, sx + toolbarTileSize, sy + toolbarTileSize);
            if (i == selectedBlock) {
                noFill();
                stroke(255, 0, 0);
                rect(toolbarX + col * toolbarTileSize, toolbarY + row * toolbarTileSize, toolbarTileSize, toolbarTileSize);
            }
        }
    }
}
