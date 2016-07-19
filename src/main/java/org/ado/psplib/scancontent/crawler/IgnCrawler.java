package org.ado.psplib.scancontent.crawler;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 21.05.16
 */
public class IgnCrawler implements GameCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgnCrawler.class);
    // http://www.ign.com/search?q=Bleach%20Soul%20Carnival&page=0&count=10&type=object&objectType=game&filter=games
    private static final String SEARCH_URL_TEMPLATE =
            "http://www.ign.com/search?q=%s&page=0&count=10&type=object&objectType=game&filter=games";

    private final Downloader downloader;

    public IgnCrawler() {
        this.downloader = new Downloader();
    }

    @Override
    public GameMetadata crawl(String fileBaseName) throws Exception {
        final String downloadUrl = String.format(SEARCH_URL_TEMPLATE,
                fileBaseName.replaceAll("\\.", "%20").replaceAll(" ", "%20").toLowerCase());
        final Document doc = Jsoup.parse(downloader.download(downloadUrl), downloadUrl);
        final String gameUrl = findMatchUrl(doc, fileBaseName);
        final Document gameDoc = Jsoup.parse(downloader.download(gameUrl), gameUrl);
        final String title = gameDoc.select("h1[class=\"contentTitle\"] > a").text();
        final Date releaseDate = new Date();
        releaseDate.setTime(0);
        return new GameMetadata(title,
                selectDetail(gameDoc, "Genre").split(" "),
                selectRating(gameDoc),
                selectDetail(gameDoc, "Publisher"),
                releaseDate,
                selectImage(gameDoc));
    }

    private String selectDetail(Document gameDoc, String type) {
        final Elements select = gameDoc.select("div[class=\"gameInfo-list\"] > div");
        for (Element element : select) {
            if (type.equals(element.select("strong").text())) {
                return element.select("a").text();
            }
        }
        return "";
    }

    private String findMatchUrl(Document doc, String fileBaseName) throws Exception {
        final List<Element> list = doc.select("div[class=\"search-item-title\"] > a").stream()
                .filter(element -> cleanString(element.text())
                        .equals(cleanString(fileBaseName)))
                .filter(element -> element.attr("href").contains("psp-"))
                .collect(Collectors.toList());
        if (list.size() > 0) {
            return list.get(0).attr("href");
        } else {
            throw new Exception(String.format("Unable to get game details for \"%s\".", fileBaseName));
        }
    }

    private int selectRating(Document gameDoc) {
        final Elements select = gameDoc.select("div[class=\"ratingValue\"]");
        for (Element element : select) {
            final String number = element.text().replaceAll("\\.", "");
            try {
                return Integer.valueOf(number);
            } catch (NumberFormatException ignore) {
            }
        }
        return 0;
    }

    private String cleanString(String text) {
        return text.toLowerCase().replaceAll("\\.", " ").replaceAll(":", "");
    }

    private URL selectImage(Document gameDoc) throws MalformedURLException {
        final String src = gameDoc.select("img[class=\"highlight-boxArt\"]").attr("src");
        if (StringUtils.isNotEmpty(src)) {
            return new URL(src);
        } else {
            return null;
        }
    }
}