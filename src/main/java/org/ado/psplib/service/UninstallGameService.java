package org.ado.psplib.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.core.GameView;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

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
                    FileUtils.deleteQuietly(new File(new File(AppConfiguration.getConfigurationProperty("psp.dir"), "ISO"), gameView.fileBaseName() + ".cso"));
                }
                return null;
            }
        };
    }
}