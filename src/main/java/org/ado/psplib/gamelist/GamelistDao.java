package org.ado.psplib.gamelist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;

/**
 * @author Andoni del Olmo
 * @since 30.04.17
 */
public class GamelistDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamelistDao.class);
    private final GamelistApi gamelistApi;

    public GamelistDao(GamelistApi gamelistApi) {
        this.gamelistApi = gamelistApi;
    }

    public GameDetails get(String gameId) {
        try {
            final Response<GameDetails> response = gamelistApi.get(gameId).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Cannot establish connection to server.");
            }
            final GameDetails gameDetails = response.body();
            return gameDetails;
        } catch (IOException e) {
            throw new RuntimeException("Cannot establish connection to server.", e);
        }
    }
}