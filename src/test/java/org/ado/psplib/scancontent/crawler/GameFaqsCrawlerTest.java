package org.ado.psplib.scancontent.crawler;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andoni del Olmo
 * @since 22.05.16
 */
public class GameFaqsCrawlerTest {
    private final GameCrawler crawler = new GameFaqsCrawler();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Test
    public void crawlingFootballManager() throws Exception {
        final GameMetadata gameMetadata = crawler.crawl("Football.Manager.Handheld.2013");

        assertEquals("title", "Football Manager Handheld 2013", gameMetadata.title());
        assertArrayEquals("genre", new String[]{"Sports", "Team", "Soccer", "Management"}, gameMetadata.genres());
        assertEquals("score", 66, gameMetadata.score());
        assertEquals("company", "Sports Interactive / Sega", gameMetadata.company());
        assertEquals("releaseDate", DATE_FORMAT.parse("30-11-2012"), gameMetadata.releaseDate());
        assertEquals("image",
                "https://gamefaqs.akamaized.net/box/5/4/5/253545_thumb.jpg",
                gameMetadata.imageUrl().toExternalForm());
    }
}