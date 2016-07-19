package org.ado.psplib.scancontent.crawler;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andoni del Olmo
 * @since 21.05.16
 */
public class MetaCriticCrawlerTest {

    private final GameCrawler crawler = new MetaCriticCrawler();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Test
    public void crawlingRome() throws Exception {
        final GameMetadata gameMetadata = crawler.crawl("Great.Battles.Of.Rome");
        assertEquals("title", "The History Channel: Great Battles of Rome", gameMetadata.title());
        assertArrayEquals("genre", new String[]{"Strategy"}, gameMetadata.genres());
        assertEquals("score", 0, gameMetadata.score());
        assertEquals("company", "Slitherine", gameMetadata.company());
//        assertEquals("releaseDate", DATE_FORMAT.parse("01-01-1970"), gameMetadata.releaseDate());
    }

    @Test
    public void crawlingBen10() throws Exception {
        final GameMetadata gameMetadata = crawler.crawl("Ben 10 Ultimate Alien: Cosmic Destruction");
        assertEquals("title", "Ben 10 Ultimate Alien: Cosmic Destruction", gameMetadata.title());
        assertArrayEquals("genre", new String[]{"Action"}, gameMetadata.genres());
        assertEquals("score", 0, gameMetadata.score());
        assertEquals("company", "Papaya Studios", gameMetadata.company());
        assertEquals("releaseDate", DATE_FORMAT.parse("05-10-2010"), gameMetadata.releaseDate());
    }

    @Test
    public void crawlingHalfBlood() throws Exception {
        final GameMetadata gameMetadata = crawler.crawl("Harry Potter and the Half-Blood Prince");
        assertEquals("title", "Harry Potter and the Half-Blood Prince", gameMetadata.title());
        assertArrayEquals("genre", new String[]{"Action", "Adventure"}, gameMetadata.genres());
        assertEquals("score", 66, gameMetadata.score());
        assertEquals("company", "EA Bright Light", gameMetadata.company());
        assertEquals("releaseDate", DATE_FORMAT.parse("30-06-2009"), gameMetadata.releaseDate());
    }

    @Test
    public void crawlingSoulCalibur() throws Exception {
        final GameMetadata gameMetadata = crawler.crawl("SoulCalibur: Broken Destiny");
        assertEquals("title", "SoulCalibur: Broken Destiny", gameMetadata.title());
        assertArrayEquals("genre", new String[]{"Action"}, gameMetadata.genres());
        assertEquals("score", 80, gameMetadata.score());
        assertEquals("company", "Bandai Namco Games", gameMetadata.company());
        assertEquals("releaseDate", DATE_FORMAT.parse("01-09-2009"), gameMetadata.releaseDate());
    }
}