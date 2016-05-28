package org.ado.psplib.core;

import org.ado.psplib.Game;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class GameView {

    private final String fileBaseName;
    private final Game game;

    public GameView(String fileBaseName, Game game) {
        this.fileBaseName = fileBaseName;
        this.game = game;
    }

    public String fileBaseName() {
        return fileBaseName;
    }

    public Game game() {
        return game;
    }

    @Override
    public String toString() {
        return game.title();
    }
}