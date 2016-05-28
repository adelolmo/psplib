package org.ado.psplib.crawler;

public interface GameCrawler {
    GameMetadata crawl(String gameName) throws Exception;
}
