package org.ado.psplib.scancontent;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.view.GameView;
import org.ado.psplib.gamelist.GameDetails;
import org.ado.psplib.gamelist.GamelistApi;
import org.ado.psplib.gamelist.GamelistDao;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class ScanContentService extends Service<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanContentService.class);
    private final Gson gson;
    private final GamelistDao gamelistDao;

    private ObservableList<GameView> list;

    public ScanContentService() {
        gson = new Gson();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://gamelist-adoorg.rhcloud.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        final GamelistApi gamelistApi = retrofit.create(GamelistApi.class);
        gamelistDao = new GamelistDao(gamelistApi);
    }

    public void setList(ObservableList<GameView> list) {
        this.list = list;
    }

    @Override
    protected Task<File> createTask() {
        return new Task<File>() {
            @Override
            protected File call() throws Exception {
                final List<File> fileList =
                        FileUtils.listFilesAndDirs(new File(AppConfiguration.getConfigurationProperty("lib.dir")),
                                new WildcardFileFilter(new String[]{"*.cso", "*.iso"}),
                                FileFileFilter.FILE)
                                .stream()
                                .filter(file ->
                                        file.getAbsolutePath().length()
                                                > AppConfiguration.getConfigurationProperty("lib.dir").length() + 5)
                                .filter(file -> !new File(AppConfiguration.getConfigurationProperty("lib.dir"),
                                        FilenameUtils.getBaseName(file.getName()) + ".json").exists())
                                .sorted(Comparator.comparing(File::getName))
                                .collect(Collectors.toList());

                for (File file : fileList) {
                    updateValue(file);
                    FileWriter fileWriter = null;
                    final String baseName = FilenameUtils.getBaseName(file.getName());
                    try {
                        final String gameId = baseName.replaceAll("\\.", "-").replaceAll(" ", "-").toLowerCase();
                        final GameDetails gameDetails = gamelistDao.get(gameId);
                        final Game game = new Game(gameId,
                                gameDetails.title(),
                                gameDetails.genres(),
                                gameDetails.score(),
                                gameDetails.company(),
                                gameDetails.releaseDate());

                        fileWriter =
                                new FileWriter(
                                        new File(
                                                AppConfiguration.getConfigurationProperty("lib.dir"),
                                                baseName + ".json"));
                        gson.toJson(game, fileWriter);

                        final URL url = new URL(gameDetails.coverUrl());
                        try (InputStream in = url.openStream()) {
                            Files.copy(in, Paths.get(AppConfiguration.getConfigurationProperty("lib.dir"),
                                    baseName + ".jpg"),
                                    StandardCopyOption.REPLACE_EXISTING);
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