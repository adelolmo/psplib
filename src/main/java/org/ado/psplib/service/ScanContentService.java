package org.ado.psplib.service;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.Downloader;
import org.ado.psplib.Game;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.core.GameView;
import org.ado.psplib.crawler.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class ScanContentService extends Service<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanContentService.class);

    private List<GameCrawler> gameCrawlers;
    private ObservableList<GameView> list;

    public ScanContentService() {
        gameCrawlers = new ArrayList<>();
        gameCrawlers.add(new MetaCriticCrawler(new Downloader()));
        gameCrawlers.add(new IgnCrawler(new Downloader()));
        gameCrawlers.add(new GameFaqsCrawler(new Downloader()));
    }

    public void setList(ObservableList<GameView> list) {
        this.list = list;
    }

    @Override
    protected Task<File> createTask() {
        return new Task<File>() {
            @Override
            protected File call() throws Exception {
                final Gson gson = new Gson();
                final List<File> fileList =
                        FileUtils.listFilesAndDirs(new File(AppConfiguration.getConfigurationProperty("lib.dir")),
                                new WildcardFileFilter("*.cso"),
                                FileFileFilter.FILE)
                                .stream()
                                .filter(file ->
                                        file.getAbsolutePath().length()
                                                > AppConfiguration.getConfigurationProperty("lib.dir").length() + 5)
                                .filter(file -> !new File(AppConfiguration.getConfigurationProperty("lib.dir"),
                                        FilenameUtils.getBaseName(file.getName()) + ".json").exists())
                                .sorted((i1, i2) -> i1.getName().compareTo(i2.getName()))
                                .collect(Collectors.toList());

                for (File file : fileList) {
                    updateValue(file);
                    GameMetadata gameMetadata = null;
                    int i = 0;
                    FileWriter fileWriter = null;
                    final String baseName = FilenameUtils.getBaseName(file.getName());
                    while (gameMetadata == null && i < gameCrawlers.size()) {
                        try {
                            gameMetadata = gameCrawlers.get(i).crawl(baseName);
                            final String gameId = baseName.replaceAll("\\.", "-").replaceAll(" ", "-").toLowerCase();
                            final Game game = new Game(gameId,
                                    gameMetadata.title(),
                                    gameMetadata.genres(),
                                    gameMetadata.score(),
                                    gameMetadata.company(),
                                    gameMetadata.releaseDate());

                            fileWriter =
                                    new FileWriter(
                                            new File(
                                                    AppConfiguration.getConfigurationProperty("lib.dir"),
                                                    baseName + ".json"));
                            gson.toJson(game, fileWriter);

                            final URL url = gameMetadata.imageUrl();
                            if (url != null) {
                                try (InputStream in = url.openStream()) {
                                    Files.copy(in, Paths.get(AppConfiguration.getConfigurationProperty("lib.dir"),
                                            baseName + ".jpg"),
                                            StandardCopyOption.REPLACE_EXISTING);
                                }
                            }

                            list.add(new GameView(baseName, game));
                        } catch (Exception e) {
//                            LOGGER.warn(e.getMessage(), e);
                        } finally {
                            try {
                                if (fileWriter != null) {
                                    fileWriter.flush();
                                }
                            } catch (IOException ignore) {
                            }
                        }
                        i++;
                    }
                    if (i >= gameCrawlers.size()) {
                        LOGGER.warn("Unable to get game details for \"{}\"", baseName);
                    }
                }
                return null;
            }
        };
    }
}