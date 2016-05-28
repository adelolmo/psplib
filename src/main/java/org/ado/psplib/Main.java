package org.ado.psplib;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 13.05.16
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static Downloader downloader = new Downloader();
    private static final String LIBRARY_DIR = "/media/andoni/riker/psp";
    private static final String BASE_URL = "http://www.metacritic.com";
    private static final String GAME_DETAILS_URL_TEMPLATE = "/game/psp/%s/details";
    // http://www.metacritic.com/game/psp/army-of-two-the-40th-day

    public static void main(String[] args) throws Exception {

        final List<File> fileList = FileUtils.listFilesAndDirs(new File(LIBRARY_DIR),
                new WildcardFileFilter("*.cso"),
                TrueFileFilter.INSTANCE).stream()
                .filter(file -> !file.getAbsolutePath().equals(LIBRARY_DIR))
                .collect(Collectors.toList());

        for (File file : fileList) {

            try {
                final String baseName = FilenameUtils.getBaseName(file.getName());
                final Document doc = getGameDoc(baseName);
                // class="product_title"
                final String title = doc.select("h1[class=\"product_title\"]").select("a").text();
//                System.out.println(title);
                // <span itemprop="ratingValue"
                final String score = doc.select("span[itemprop=\"ratingValue\"]").text();
//                System.out.println(score);
                // div class="metascore_w user large game mixed"
                final String userScore = doc.select("div[class=\"metascore_w user large game mixed\"]").text();
//                System.out.println(userScore );
                // <div class="product_details"> 4th row, 2nd col
                final String genre = doc.select("div[class=\"product_details\"]").select("table").select("tr").eq(3).select("td").text();
//                System.out.println(genre);
//                final Game game = new Game("", title, genre, Integer.valueOf(score));
//                System.out.println(game);
                final String imageSrc = doc.select("img[class=\"product_image large_image\"]").attr("src");
                try (InputStream in = new URL(imageSrc).openStream()) {
                    Files.copy(in, Paths.get("/tmp/psp/",
                            baseName.replaceAll("\\.", "-").replaceAll(" ", "-").toLowerCase() + ".jpg"),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
//                LOGGER.warn(e.getMessage());
                System.out.println(e.getMessage());
            }
        }
    }

    private static Document getGameDoc(String baseName) throws IOException {
        try {

            final String name = baseName.replaceAll("\\.", "-").replaceAll(" ", "-").toLowerCase();
            String downloadUrl = BASE_URL + String.format(GAME_DETAILS_URL_TEMPLATE, name);
            return Jsoup.parse(downloader.download(downloadUrl), downloadUrl);
            // class="product_image large_image"
        } catch (IOException e) {
//                LOGGER.error(String.format("Can't find game %s", name), e);
//            LOGGER.error(String.format("Can't find game %s", baseName));

            // http://www.metacritic.com/search/all/the-secret-saturdays/results
            try {
                final String name = baseName.replaceAll("\\.", "-").replaceAll(" ", "%20").toLowerCase();
                String downloadUrl = BASE_URL + String.format("/search/game/%s/results", name);
                final Document searchDocument = Jsoup.parse(downloader.download(downloadUrl), downloadUrl);

                // <div class="module search_results">

                final Optional<String> gameLink = searchDocument.select("div[class=\"module search_results\"]").select("a")
                        .stream()
                        .filter(element -> element.attr("href").startsWith("/game/psp"))
                        .map(element -> element.attr("href"))
                        .findFirst();
                if (gameLink.isPresent()) {
                    return Jsoup.parse(downloader.download(BASE_URL + gameLink.get()), BASE_URL + gameLink.get());
                } else {
//                    LOGGER.error(String.format("Can't find game %s", baseName));
                    throw new IOException(String.format("Can't find game \"%s\"", baseName));
                }

            } catch (IOException e1) {
                e1.printStackTrace();
                throw e1;
            }
        }
    }
}