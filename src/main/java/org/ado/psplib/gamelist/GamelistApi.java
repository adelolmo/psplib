package org.ado.psplib.gamelist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Andoni del Olmo
 * @since 30.04.17
 */
public interface GamelistApi {

    @GET("/games")
    Call<GameDetails> all();

    @GET("/games/{id}")
    Call<GameDetails> get(@Path("id") String id);
}