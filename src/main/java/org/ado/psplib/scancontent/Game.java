package org.ado.psplib.scancontent;

import com.google.gson.annotations.Expose;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class Game {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @Expose
    private final String id;

    @Expose
    private final String title;

    @Expose
    private final String[] genres;

    @Expose
    private final int metaScore;

    @Expose
    private final String company;

    @Expose
    private final Date releaseDate;

    private final URL cover;

    public Game(String id, String title, String[] genres, int metaScore, String company, Date releaseDate, URL cover) {
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.metaScore = metaScore;
        this.company = company;
        this.releaseDate = releaseDate;
        this.cover = cover;
    }

    public static Game of(String id, String title, String genres, String company, String score, String releasedAt, URL cover) {
        try {
            return new Game(id,
                    title,
                    genres.split(","),
                    Integer.parseInt(score),
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
        return metaScore;
    }

    public String company() {
        return company;
    }

    public Date releaseDate() {
        return releaseDate;
    }

    public URL cover() {
        return cover;
    }

    @Override
    public String toString() {
        return title;
    }
}
