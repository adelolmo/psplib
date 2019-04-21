package org.ado.psplib.gameloader;

import com.google.gson.Gson;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.scancontent.Game;
import org.ado.psplib.scancontent.ScanContentService;
import org.ado.psplib.view.GameView;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.apache.commons.io.FileUtils.listFilesAndDirs;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * @author Andoni del Olmo
 * @since 25.05.16
 */
public class GameLoaderService extends Service<List<GameView>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoaderService.class);

    private final Gson gson;
    private String libraryDir;

    public GameLoaderService(Gson gson) {
        this.gson = gson;
        libraryDir = getConfiguration("lib.dir");
    }

    public void setLibraryDirectory(String libraryDirectory) {
        this.libraryDir = libraryDirectory;
    }

    @Override
    protected Task<List<GameView>> createTask() {
        return new Task<List<GameView>>() {
            @Override
            protected List<GameView> call() {
                final List<GameView> list = new ArrayList<>();

                if (libraryDir != null && new File(libraryDir).exists()) {
                    listFilesAndDirs(new File(libraryDir),
                            new WildcardFileFilter("*.json"),
                            FileFileFilter.FILE)
                            .stream()
                            .filter(file -> !file.getAbsolutePath().equals(libraryDir))
                            .forEach(file -> {
                                try {
                                    final Game game = gson.fromJson(new FileReader(file), Game.class);
                                    list.add(new GameView(getBaseName(file.getName()),
                                            new Game(game.id(),
                                                    game.title(),
                                                    game.genres(),
                                                    game.score(),
                                                    game.company(),
                                                    game.releaseDate(),
                                                    ScanContentService.class.getResource(game.id() + ".jpeg"))));
                                } catch (FileNotFoundException e) {
                                    LOGGER.error(format("Json file not found %s", file.getAbsolutePath()), e);
                                }
                            });
                }
                list.sort(Comparator.comparing(o -> o.game().title()));
                return list;
            }
        };
    }
}