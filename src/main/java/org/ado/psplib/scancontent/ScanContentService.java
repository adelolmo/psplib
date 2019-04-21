package org.ado.psplib.scancontent;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.view.GameView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class ScanContentService extends Service<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanContentService.class);
    private final Gson gson;

    private ObservableList<GameView> list;

    public ScanContentService(Gson gson) {
        this.gson = gson;
    }

    public void setList(ObservableList<GameView> list) {
        this.list = list;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final String libDir = AppConfiguration.getConfiguration("lib.dir");

                final List<File> fileList =
                        FileUtils.listFilesAndDirs(new File(libDir),
                                new WildcardFileFilter(new String[]{"*.cso", "*.iso"}),
                                FileFileFilter.FILE)
                                .stream()
                                .filter(file ->
                                        file.getAbsolutePath().length()
                                                > libDir.length() + 5)
                                .filter(file -> !new File(libDir,
                                        getBaseName(file.getName()) + ".json").exists())
                                .sorted(Comparator.comparing(File::getName))
                                .collect(Collectors.toList());

                final CSVParser csv =
                        CSVParser.parse(ScanContentService.class.getResource("games.csv"),
                                Charset.forName("UTF-8"),
                                CSVFormat.newFormat(';').withFirstRecordAsHeader());
                final Map<String, Game> games = new HashMap<>();
                csv.iterator().forEachRemaining(strings ->
                        games.put(strings.get("id"),
                                Game.of(strings.get("id"),
                                        strings.get("title"),
                                        strings.get("genres"),
                                        strings.get("company"),
                                        strings.get("score"),
                                        strings.get("released_at"),
                                        ScanContentService.class.getResource(strings.get("id") + ".jpeg"))));

                for (final File file : fileList) {
                    updateMessage(format("Processing \"%s\" ...", file.getName()));
                    FileWriter fileWriter = null;
                    final String baseName = getBaseName(file.getName());
                    try {
                        final String gameId = GameIdGenerator.toId(baseName);
                        final Game game = games.get(gameId);
                        if (game == null) {
                            LOGGER.warn("Unable to get game details for \"{}\"", baseName);
                            continue;
                        }

                        fileWriter = new FileWriter(new File(libDir, baseName + ".json"));
                        gson.toJson(game, fileWriter);

                        final URL url = game.cover();
                        if (url == null) {
                            continue;
                        }
                        try (InputStream in = url.openStream()) {
                            final Path target = Paths.get(libDir, baseName + ".jpeg");
                            Files.copy(in, target, REPLACE_EXISTING);
                        }

                        list.add(new GameView(baseName, game));
                    } catch (Exception e) {
                        LOGGER.warn("Unable to get game details for \"{}\"", baseName);
                    } finally {
                        try {
                            if (fileWriter != null) {
                                fileWriter.flush();
                            }
                        } catch (IOException ignore) {
                        }
                    }
                }
                return null;
            }
        };
    }
}