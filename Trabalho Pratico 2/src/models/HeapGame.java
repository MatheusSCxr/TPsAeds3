package models;

public class HeapGame {
    private SteamGame game;
    private int peso;

    public HeapGame(SteamGame game, int peso) {
        this.game = game;
        this.peso = peso;
    }

    public int getPeso(){
        return peso;
    }

    public int getId(){
        return game.getId();
    }

    public String getName(){
        return game.getName();
    }

    public SteamGame getGame(){
        return game;
    }
}
