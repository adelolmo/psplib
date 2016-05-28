package org.ado.psplib;

import java.util.Date;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class Game {

    private final String id;
    private final String title;
    private final String[] genres;
    private final int metaScore;
    private final String company;
    private final Date releaseDate;

    public Game(String id, String title, String[] genres, int metaScore, String company, Date releaseDate) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.metaScore = metaScore;
        this.company = company;
        this.releaseDate = releaseDate;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String[] genre() {
        return genres;
    }

    public int metaScore() {
        return metaScore;
    }

    public String company() {
        return company;
    }

    public Date releaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return title;
    }
}