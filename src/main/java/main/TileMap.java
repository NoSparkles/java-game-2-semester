package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;

public class TileMap {
    // position
    private double x;
    private double y;

    // bounds
    private int xmin;
    private int ymin;
    private int xmax;
    private int ymax;

    private double tween;

    // map
    private int[][] map;
    private final int tileSize;
    private int numRows;
    private int numCols;
    private int width;
    private int height;

    // tileset
    private BufferedImage tileset;
    private Tile[] tiles;

    // drawing
    private int rowOffset;
    private int colOffset;
    private final int numRowsToDraw;
    private final int numColsToDraw;

    public TileMap() {
        this.tileSize = 16;
        this.numRowsToDraw = GamePanel.HEIGHT / tileSize + 2;
        this.numColsToDraw = GamePanel.WIDTH / tileSize + 2;
        this.tween = 0.07;
    }

    public TileMap(int numColsToDraw, int numRowsToDraw, int tileSize) {
        this.numColsToDraw = numColsToDraw;
        this.numRowsToDraw = numRowsToDraw;
        this.tileSize = tileSize;
    }

    public void loadTiles() {
        try {
            this.tileset = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/sheet.png"));
            this.tiles = new Tile[50];
            BufferedImage transparentTile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < tileSize; ++i) {
                for (int j = 0; j < tileSize; ++j) {
                    transparentTile.setRGB(i, j, 0x00000000);
                }
            }
            this.tiles[0] = new Tile(transparentTile, Tile.NORMAL);
            int i = 1;
            BufferedImage subimage;
            for (int row = 0; row < 1; ++row) {
                for (int col = 7; col < 8; ++col) {
                    subimage = this.tileset.getSubimage(row * this.tileSize, col * this.tileSize, this.tileSize, this.tileSize);
                    this.tiles[i] = new Tile(subimage, Tile.BLOCKED);
                    ++i;
                } 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String s) {
        // load map from json file
        try {
            InputStream in = getClass().getResourceAsStream(s);
            if (in == null) {
                throw new IOException("Resource not found: " + s);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            String jsonString = sb.toString();
            JSONObject jsonObject = new JSONObject(jsonString);
            this.numCols = jsonObject.getInt("numCols");
            this.numRows = jsonObject.getInt("numRows");
            this.map = new int[this.numRows][this.numCols];
            JSONArray mapArray = jsonObject.getJSONArray("map");
            for (int i = 0; i < this.numRows; ++i) {
                JSONArray rowArray = mapArray.getJSONArray(i);
                for (int j = 0; j < this.numCols; ++j) {
                    this.map[i][j] = rowArray.getInt(j);
                }
            }
            this.width = this.numCols * this.tileSize;
            this.height = this.numRows * this.tileSize;
            this.xmin = GamePanel.WIDTH - this.width;
            this.xmax = 0;
            this.ymin = GamePanel.HEIGHT - this.height;
            this.ymax = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTileSize() {
        return this.tileSize;
    }

    public double getx() {
        return this.x;
    }

    public double gety() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getType(int row, int col) {
        if (this.map[row][col] == 0) {
            return Tile.NORMAL;
        }
        return Tile.BLOCKED;
    }

    public void setPosition(double x, double y) {
        this.x += (x - this.x) * this.tween;
        this.y += (y - this.y) * this.tween;

        this.fixBounds();

        this.colOffset = (int)-this.x / this.tileSize;
        this.rowOffset = (int)-this.y / this.tileSize;
    }

    public void setTween(double d) {
        this.tween = d;
    }

    private void fixBounds() {
        if (this.x < this.xmin) {
            this.x = this.xmin;
        }
        if (this.y < this.ymin) {
            this.y = this.ymin;
        }
        if (this.x > this.xmax) {
            this.x = this.xmax;
        }
        if (this.y > this.ymax) {
            this.y = this.ymax;
        }
    }

    public void draw(Graphics2D g) {
        for (int row = this.rowOffset; row < this.rowOffset + this.numRowsToDraw; ++row) {
            if (row >= this.numRows) {
            break;
            }
            for (int col = this.colOffset; col < this.colOffset + this.numColsToDraw; ++col) {
            if (col >= this.numCols) {
                break;
            }
            if (this.map[row][col] == 0) {
                continue;
            }
            int rc = this.map[row][col];
            g.drawImage(this.tiles[rc].getImage(), (int)this.x + col * this.tileSize, (int)this.y + row * this.tileSize, null);
            }
        }
    }
}
