package org.ado.psplib.view;

import org.ado.psplib.scancontent.Game;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class GameView {

    private final String fileBaseName;
    private final Game gameDetails;

    public GameView(String fileBaseName, Game game) {
        this.fileBaseName = fileBaseName;
        this.gameDetails = game;
    }

    public String fileBaseName() {
        return fileBaseName;
    }

    public Game game() {
        return gameDetails;
    }

    @Override
    public String toString() {
        return gameDetails.title();
    }
}