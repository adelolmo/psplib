package org.ado.psplib.crawler;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class GameMetadata {

    private final String title;
    private final String[] genres;
    private final int score;
    private final String company;
    private final Date releaseDate;
    private final URL imageUrl;

    GameMetadata(String title, String[] genres, int score, String company, Date releaseDate, URL imageUrl) {
        this.title = title;
        this.genres = genres;
        this.score = score;
        this.company = company;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
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
        return releaseDate;
    }

    public URL imageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameMetadata{");
        sb.append("title='").append(title).append('\'');
        sb.append(", genres=").append(Arrays.toString(genres));
        sb.append(", score=").append(score);
        sb.append(", company='").append(company).append('\'');
        sb.append(", releaseDate=").append(releaseDate);
        sb.append(", imageUrl=").append(imageUrl);
        sb.append('}');
        return sb.toString();
    }
}
