package org.ado.psplib.crawler;

import org.ado.psplib.Downloader;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class MetaCriticCrawler implements GameCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaCriticCrawler.class);
    private static final String BASE_URL = "http://www.metacritic.com";
    private static final String GAME_DETAILS_URL_TEMPLATE = "%s/game/psp/%s/details";
    private static final String SEARCH_URL_TEMPLATE = "%s/search/game/%s/results";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private final Downloader downloader;

    public MetaCriticCrawler(Downloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public GameMetadata crawl(String gameName) throws Exception {
        final Document doc = getGameDetails(gameName);
        try {
            return new GameMetadata(doc.select("h1[class=\"product_title\"]").select("a").text(),
                    getGenre(doc),
                    getScore(doc),
                    selectProductDetail(doc, "Developer:"),
                    parseDate(selectReleaseDate(doc)),
                    new URL(doc.select("img[class=\"product_image large_image\"]").attr("src")));
        } catch (Exception e) {
            throw new Exception(String.format("Unable to get game details for \"%s\".", gameName), e);
        }
    }

    private Document getGameDetails(String baseName) throws IOException {
        try {
            final String downloadUrl = String.format(GAME_DETAILS_URL_TEMPLATE, BASE_URL,
                    baseName.replaceAll("\\.", "-").replaceAll(" ", "-").toLowerCase());
            return Jsoup.parse(downloader.download(downloadUrl), downloadUrl);
        } catch (IOException e) {
            final String downloadUrl = String.format(SEARCH_URL_TEMPLATE, BASE_URL,
                    baseName.replaceAll("\\.", "-").replaceAll(" ", "%20").toLowerCase());

            final Optional<String> gameLink = Jsoup.parse(downloader.download(downloadUrl), downloadUrl)
                    .select("div[class=\"module search_results\"]").select("a")
                    .stream()
                    .filter(element -> element.attr("href").startsWith("/game/psp"))
                    .map(element -> element.attr("href"))
                    .findFirst();
            if (gameLink.isPresent()) {
                final String gameDetailsUrl = BASE_URL + gameLink.get() + "/details";
                return Jsoup.parse(downloader.download(gameDetailsUrl), gameDetailsUrl);
            } else {
                throw new IOException(String.format("Can't find game \"%s\"", baseName));
            }
        }
    }

    private String[] getGenre(Document doc) {
        final String genre = selectProductDetail(doc, "Genre(s):");
        if (!StringUtils.isEmpty(genre)) {
            return genre.split(" ");
        } else {
            return null;
        }
    }

    private Integer getScore(Document doc) throws IOException {
        final String rating = doc.select("span[itemprop=\"ratingValue\"]").text();
        return StringUtils.isNotEmpty(rating) ? Integer.valueOf(rating) : (int) ((Double.valueOf(getUserRating(doc))) * 10);
    }

    private String getUserRating(Document doc) throws IOException {
        final String positiveRating = getTextOrEmpty(doc.select("div[class=\"metascore_w user large game positive\"]"));
        final String negativeRating = getTextOrEmpty(doc.select("div[class=\"metascore_w user large game negative\"]"));
        final String mixedRating = getTextOrEmpty(doc.select("div[class=\"metascore_w user large game mixed\"]"));
        if (!"".equals(positiveRating)) {
            return positiveRating;
        } else if (!"".equals(negativeRating)) {
            return negativeRating;
        } else if (!"".equals(mixedRating)) {
            return mixedRating;
        }
        return "0";
    }

    private String selectReleaseDate(Document doc) {
        return doc.select("li[class=\"summary_detail release_data\"]")
                .select("span[class=\"data\"").text();
    }

    private String selectProductDetail(Document doc, String type) {
        final Elements elements = doc.select("div[class=\"product_details\"]")
                .select("table > tbody > tr > th");
        for (Element element : elements) {
            if (type.endsWith(element.text())) {
                return element.parent().select("td").text();
            }
        }
        return "";
    }

    private String getTextOrEmpty(Elements select) {
        return select != null && select.size() > 0 ? select.first().text() : "";
    }

    private Date parseDate(String stringDate) {
        try {
            return DATE_FORMAT.parse(stringDate);
        } catch (ParseException e) {
            final Date date = new Date();
            date.setTime(0);
            return date;
        }
    }
}
