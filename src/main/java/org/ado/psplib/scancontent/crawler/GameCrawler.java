package org.ado.psplib.scancontent.crawler;

public interface GameCrawler {
    GameMetadata crawl(String gameName) throws Exception;
}
