package org.ado.psplib.view;

import java.io.File;
import java.util.Comparator;

/**
 * @author Andoni del Olmo
 * @since 31/12/2019
 */
public class GameViewComparator implements Comparator<GameView> {

    private final SortType sortType;
    private final String libDir;

    GameViewComparator(SortType sortType, String libDir) {
        this.sortType = sortType;
        this.libDir = libDir;
    }

    @Override
    public int compare(GameView gw1, GameView gw2) {
        if (sortType == null) {
            return 1;
        }

        switch (sortType) {
            case SCORE:
                return Integer.compare(gw2.game().score(), gw1.game().score());
            case SIZE:
                return Long.compare(gameFile(libDir, gw2).length(), gameFile(libDir, gw1).length());
            case TITLE:
            default:
                return gw1.game().title().compareTo(gw2.game().title());
        }
    }

    private File gameFile(String libDir, GameView gameView) {
        final File csoFile = new File(libDir, gameView.csoFilename());
        final File isoFile = new File(libDir, gameView.isoFilename());
        return isoFile.exists() ? isoFile : csoFile;
    }
}
