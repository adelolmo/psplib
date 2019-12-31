package org.ado.psplib.scancontent;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.GameDatabase;
import org.ado.psplib.GameMetadata;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.view.GameView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class ScanContentService extends Service<List<GameView>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanContentService.class);
    private final GameMetadata gameMetadata;

    public ScanContentService(GameMetadata gameMetadata) {
        this.gameMetadata = gameMetadata;
    }

    @Override
    protected Task<List<GameView>> createTask() {
        return new Task<List<GameView>>() {
            @Override
            protected List<GameView> call() {
                final String libDir = AppConfiguration.getConfiguration("lib.dir");
                final List<GameView> list = new ArrayList<>();

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

                final Map<String, Game> games = GameDatabase.getAll();

                for (final File file : fileList) {
                    updateMessage(format("Processing \"%s\" ...", file.getName()));
                    final String baseName = getBaseName(file.getName());
                    try {
                        final String gameId = GameIdGenerator.toId(baseName);
                        final Game game = games.get(gameId);
                        if (game == null) {
                            LOGGER.warn("Unable to get game details for \"{}\"", baseName);
                            continue;
                        }
                        gameMetadata.writeToDisk(getConfiguration("lib.dir"), baseName, game);
                        list.add(new GameView(baseName, game));
                    } catch (Exception e) {
                        LOGGER.warn("Unable to get game details for \"{}\"", baseName);
                    }
                }
                return list;
            }
        };
    }
}
