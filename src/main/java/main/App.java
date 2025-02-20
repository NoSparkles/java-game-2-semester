package main;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class App extends PApplet {
    PImage tileSheet;
    int[][] tileMap;
    int currentTile = 0;
    int mapTileSize = 32; // Map tile size is 32x32 pixels
    int toolbarTileSize = 16; // Toolbar tile size is 16x16 pixels
    boolean inMenu = true;
    boolean editingMode = true; // Start in editing mode
    int selectedBlock = 0;
    int toolbarItemsPerRow = 7; // Number of items per row in the toolbar

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
        loadTileMap(); // Load the tile map from the JSON file
    }
    
    void saveTileMap() {
        JSONObject json = new JSONObject();
        JSONArray mapArray = new JSONArray();
        
        for (int i = 0; i < tileMap.length; i++) {
            JSONArray rowArray = new JSONArray();
            for (int j = 0; j < tileMap[i].length; j++) {
                rowArray.append(tileMap[i][j]);
            }
            mapArray.append(rowArray);
        }
        
        json.setJSONArray("tileMap", mapArray);
        saveJSONObject(json, "map.json");
        println("Tile map saved to map.json");
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

    void loadTileMap() {
        JSONObject json = loadJSONObject("map.json");
        if (json == null) {
            println("Error loading map.json. File not found or invalid.");
            return;
        }
        JSONArray mapArray = json.getJSONArray("tileMap");
        for (int i = 0; i < tileMap.length; i++) {
            JSONArray rowArray = mapArray.getJSONArray(i);
            for (int j = 0; j < tileMap[i].length; j++) {
                tileMap[i][j] = rowArray.getInt(j);
            }
        }
        println("Tile map loaded from map.json");
    }
    

    public void keyPressed() {
        if (inMenu) {
            inMenu = false;
        } else if (key == 'P' || key == 'p') {
            if (editingMode) {
                saveTileMap(); // Save the tile map when exiting editing mode
            }
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
            int index = row * toolbarItemsPerRow + col;
    
            int startRow = 0; // Starting row
            int startCol = 7; // Starting column
            int endCol = 14; // Ending column
    
            int tileIndex = (startRow + index / (endCol - startCol)) * (tileSheet.width / toolbarTileSize) + (startCol + index % (endCol - startCol));
    
            if (col >= 0 && col < toolbarItemsPerRow && row * toolbarItemsPerRow + col < 7 * 8) { // Adjusted range check
                selectedBlock = tileIndex;
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
        int totalTiles = (7 * 8); // Number of tiles to read (7 rows by 8 columns)
        int toolbarX = 0; // Toolbar starting at the leftmost side
        int toolbarY = height - toolbarTileSize * 10; // Positioned at the bottom
    
        int startRow = 0; // Starting row
        int startCol = 7; // Starting column
        int endCol = 14; // Ending column
    
        for (int i = 0; i < totalTiles; i++) {
            int row = startRow + i / (endCol - startCol);
            int col = startCol + i % (endCol - startCol);
            int sx = col * toolbarTileSize;
            int sy = row * toolbarTileSize;
            int toolbarIndex = (i % itemsPerRow) + (i / itemsPerRow) * itemsPerRow; // Calculate toolbar index
    
            image(tileSheet, toolbarX + (i % itemsPerRow) * toolbarTileSize, toolbarY + (i / itemsPerRow) * toolbarTileSize, toolbarTileSize, toolbarTileSize, sx, sy, sx + toolbarTileSize, sy + toolbarTileSize);
    
            if (toolbarIndex == selectedBlock) {
                noFill();
                stroke(255, 0, 0);
                rect(toolbarX + (i % itemsPerRow) * toolbarTileSize, toolbarY + (i / itemsPerRow) * toolbarTileSize, toolbarTileSize, toolbarTileSize);
            }
        }
    }
    
    
}
