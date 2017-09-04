package org.ado.psplib.gamelist;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Andoni del Olmo
 * @since 30.04.17
 */
public class GameDetails {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final String id;
    private final String title;
    private final String[] genres;
    private final int score;
    private final String company;
    private final Date releasedAt;
    private final URL coverUrl;

    public GameDetails(String id, String title, String[] genres, int score, String company, Date releasedAt, URL coverUrl) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.score = score;
        this.company = company;
        this.releasedAt = releasedAt;
        this.coverUrl = coverUrl;
    }

    public static GameDetails of(String id, String title, String genres, String company, String score, String releasedAt, URL cover) {
        try {
            return new GameDetails(id,
                    title,
                    genres.split(","),
                    Integer.valueOf(score),
                    company,
                    DATE_FORMAT.parse(releasedAt),
                    cover);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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

    public URL coverUrl() {
        return coverUrl;
    }

    @Override
    public String toString() {
        return title;
    }
}