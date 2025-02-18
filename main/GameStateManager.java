package main;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class GameStateManager {
    private final ArrayList<GameState> gameStates;
    private int currentState;

    public static final int MENUSTATE = 0;
    public static final int LEVEL1STATE = 1;
    public static final int LEVEL2STATE = 2;
    public static final int LEVEL3STATE = 3;

    public GameStateManager() {
        this.gameStates = new ArrayList<>();
        this.currentState = GameStateManager.MENUSTATE;

        this.gameStates.add(new MenuState(this));
        this.gameStates.add(new Level1State(this));
    }

    public GameStateManager(ArrayList<GameState> gameStates) {
        this.gameStates = gameStates;
    }

    public void setState(int state) {
        this.currentState = state;
        gameStates.get(this.currentState).init();
    }

    public void update() {
        gameStates.get(this.currentState).update();
    }

    public void draw(Graphics2D g) {
        gameStates.get(this.currentState).draw(g);
    }

    public void keyPressed(int k) {
        gameStates.get(this.currentState).keyPressed(k);
    }

    public void keyReleased(int k) {
        gameStates.get(this.currentState).keyReleased(k);
    }
}
