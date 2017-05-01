package org.ado.psplib.gamelist;

import java.util.Date;

/**
 * @author Andoni del Olmo
 * @since 30.04.17
 */
public class GameDetails {

    private final String id;
    private final String title;
    private final String[] genres;
    private final int score;
    private final String company;
    private final Date releasedAt;
    private final String coverUrl;

    public GameDetails(String id, String title, String[] genres, int score, String company, Date releasedAt, String coverUrl) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.score = score;
        this.company = company;
        this.releasedAt = releasedAt;
        this.coverUrl = coverUrl;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String[] genres() {
        return genres;
    }

    public int score() {
        return score;
    }

    public String company() {
        return company;
    }

    public Date releaseDate() {
        return releasedAt;
    }

    public String coverUrl() {
        return coverUrl;
    }

    @Override
    public String toString() {
        return title;
    }
}