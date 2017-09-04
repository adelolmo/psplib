package org.ado.psplib.uninstall;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.view.GameView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.apache.commons.io.FileUtils.deleteQuietly;

/**
 * @author Andoni del Olmo
 * @since 16.05.16
 */
public class UninstallGameService extends Service<GameView> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UninstallGameService.class);

    private List<GameView> games;

    public void setGames(final List<GameView> games) {
        this.games = games;
    }

    @Override
    protected Task<GameView> createTask() {
        return new Task<GameView>() {
            @Override
            protected GameView call() throws Exception {
                for (GameView gameView : games) {
                    updateValue(gameView);
                    final String pspDirectoryName = getConfiguration("psp.dir");
                    final File pspIsoDirectory = new File(pspDirectoryName, "ISO");

                    deleteQuietly(new File(pspIsoDirectory, gameView.fileBaseName() + ".cso"));
                    deleteQuietly(new File(pspIsoDirectory, gameView.fileBaseName() + ".iso"));
                }
                return null;
            }
        };
    }
}