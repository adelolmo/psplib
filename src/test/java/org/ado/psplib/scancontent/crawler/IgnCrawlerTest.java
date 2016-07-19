package org.ado.psplib.scancontent.crawler;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andoni del Olmo
 * @since 21.05.16
 */
public class IgnCrawlerTest {

    private final GameCrawler crawler = new IgnCrawler();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Test
    public void crawlingBleach() throws Exception {
        final GameMetadata gameMetadata = crawler.crawl("Bleach.Soul.Carnival");

        assertEquals("title", "Bleach: Soul Carnival", gameMetadata.title());
        assertArrayEquals("genre", new String[]{"Action"}, gameMetadata.genres());
        assertEquals("score", 85, gameMetadata.score());
        assertEquals("company", "Sony Computer Entertainment", gameMetadata.company());
        assertEquals("image",
                "http://codesmedia.ign.com/codes/image/object/142/14255010/Bleach-Soul-Carnival_PSP_JPboxart_160h.jpg",
                gameMetadata.imageUrl().toExternalForm());
//        assertEquals("releaseDate", DATE_FORMAT.parse("05-10-2010"), gameMetadata.releaseDate());
    }
}