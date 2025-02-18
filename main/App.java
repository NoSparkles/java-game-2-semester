package main;

import javax.swing.JFrame;

public class App extends JFrame{
    public App() {
        super("Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(new GamePanel());
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }
    public static void main(String[] args) {
        new App();
    }
    
}