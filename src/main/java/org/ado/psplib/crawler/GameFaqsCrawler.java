package org.ado.psplib.crawler;

import org.ado.psplib.Downloader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Andoni del Olmo
 * @since 22.05.16
 */
public class GameFaqsCrawler implements GameCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameFaqsCrawler.class);
    // http://www.gamefaqs.com/search?platform=109&game=Football+Manager+Handheld+2013&contrib=0&rating=0&genre=0&region=0&date=0&developer=&publisher=&dist=0&sort=0&link=0&res=0&title=0
    private static final String BASE_URL = "http://www.gamefaqs.com";
    private static final String SEARCH_URL_TEMPLATE =
            "http://www.gamefaqs.com/search?platform=109&game=%s&contrib=0" +
                    "&rating=0&genre=0&region=0&date=0&developer=&publisher=&dist=0&sort=0&link=0&res=0&title=0";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private final Downloader downloader;

    public GameFaqsCrawler(Downloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public GameMetadata crawl(String fileBaseName) throws Exception {
        final String downloadUrl = String.format(SEARCH_URL_TEMPLATE,
                fileBaseName.replaceAll("\\.", "+").replaceAll(" ", "+").toLowerCase());
        final Document doc = Jsoup.parse(downloader.download(downloadUrl), downloadUrl);

        final String candidate =
                doc.select("table[class=\"results\"]").select("tbody").get(0).select("td").get(1).text();
        if (!escape(candidate)
                .equals(escape(fileBaseName))) {
            throw new Exception(String.format("Not match found for \"%s\".", fileBaseName));
        }
        final String gameDetailsUrl = BASE_URL + doc.select("table[class=\"results\"]").select("a").first().attr("href");
        final Document gameDoc = Jsoup.parse(downloader.download(gameDetailsUrl), gameDetailsUrl);

        return new GameMetadata(
                gameDoc.select("h1[class=\"page-title\"]").text(),
                selectGenres(gameDoc),
                selectRating(gameDoc, "Rating"),
                selectCompany(gameDoc),
                selectReleaseDate(gameDoc),
                selectImageUrl(gameDoc));
    }

    private String escape(String candidate) {
        return candidate.toLowerCase()
                .replaceAll("\\.", " ")
                .replaceAll(":", "")
                .replaceAll("'", "")
                .replaceAll("!", "");
    }

    private String[] selectGenres(Document gameDoc) {
        return gameDoc.select("li[class=\"crumb\"]").text().split(" ");
    }

    private int selectRating(Document gameDoc, String type) {
        final Optional<String> rating = selectDetail(gameDoc, type);
        if (rating.isPresent()) {
            final String complexScore = rating.get();
            final Double scoreBaseFive = Double.valueOf(complexScore.split("/")[0]);
            return (int) Math.ceil(scoreBaseFive * 100 / 5);
        }
        return 0;
    }

    private Optional<String> selectDetail(Document gameDoc, String type) {
        final Elements select = gameDoc.select("div[class=\"pod_split gamerater_label\"]");
        for (Element element : select) {
            if (element.select("div[class=\"subsection-title\"]").text().startsWith(type)) {
                return Optional.ofNullable(element.select("div[class=\"subsection-title\"] > div").text());
            }
        }
        return Optional.empty();
    }

    private String selectCompany(Document gameDoc) {
        return gameDoc.select("div[class=\"pod pod_gameinfo\"]")
                .select("div[class=\"body\"]")
                .select("ul > li")
                .get(2).text();
    }

    private Date selectReleaseDate(Document gameDoc) {
        try {
            return DATE_FORMAT.parse(gameDoc.select("div[class=\"pod pod_gameinfo\"]")
                    .select("div[class=\"body\"]")
                    .select("ul > li")
                    .get(3).text().substring("Release: ".length(), 21));
        } catch (ParseException e) {
            final Date date = new Date();
            date.setTime(0);
            return date;
        }
    }

    private URL selectImageUrl(Document gameDoc) throws MalformedURLException {
        return new URL(gameDoc.select("img[class=\"boxshot\"]").attr("src"));
    }
}