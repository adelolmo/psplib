package org.ado.psplib.service;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.Game;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.core.GameView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 25.05.16
 */
public class GameLoaderService extends Service<GameView> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoaderService.class);

    @Inject
    private Gson gson;

    private ObservableList<GameView> list;
    private String libraryDir;

    public GameLoaderService() {
        libraryDir = AppConfiguration.getConfigurationProperty("lib.dir");
    }

    public void setList(ObservableList<GameView> list) {
        this.list = list;
    }

    public void setLibraryDirectory(String libraryDirectory) {
        this.libraryDir = libraryDirectory;
    }

    @Override
    protected Task<GameView> createTask() {
        return new Task<GameView>() {
            @Override
            protected GameView call() throws Exception {
                System.out.println("loading games... " + libraryDir);
                if (libraryDir != null && new File(libraryDir).exists()) {

                    final Collection<File> gameFiles =
                            FileUtils.listFilesAndDirs(new File(libraryDir),
                                    new WildcardFileFilter("*.json"),
                                    FileFileFilter.FILE)
                                    .stream()
                                    .filter(file -> !file.getAbsolutePath().equals(libraryDir))
                                    .collect(Collectors.toList());
                    //                    final Map<File, Game> gameMap = new HashMap<>();
//                    System.out.println(gameFiles);
                    for (File file : gameFiles) {
//                        System.out.println(file);
                        list.add(new GameView(FilenameUtils.getBaseName(file.getName()),
                                gson.fromJson(new FileReader(file), Game.class)));
//                        updateValue(
//                                new GameView(FilenameUtils.getBaseName(file.getName()),
//                                        gson.fromJson(new FileReader(file), Game.class)));
                    }

/*
                    final List<Game> collect = gameFiles.stream()
                            .filter(file -> !file.getAbsolutePath().equals(libraryDir))
                            .map(file -> {
                                try {
                                    return gameMap.put(file, gson.fromJson(new FileReader(file), Game.class));
                                } catch (FileNotFoundException e) {
                                    throw new IllegalStateException(e);
                                }
                            }).collect(Collectors.toList());

                    for (File file : gameFiles) {
                        updateValue(new GameView(FilenameUtils.getBaseName(file.getName()),
                                gameMap.get(file)));
                    }
*/

/*                    final List<GameView> gameViewList = gameFiles.stream()
                            .filter(file -> !file.getAbsolutePath().equals(libraryDir))
*//*                            .filter(file -> {
                                return searchSequence != null ?
                                        gameMap.get(file).title().toLowerCase().contains(searchSequence)
                                        : true;
                            })*//*
                            .sorted((f1, f2) -> compareGames(f1, f2, sortType, gameMap))
                            .map(file -> new GameView(FilenameUtils.getBaseName(file.getName()),
                                    gameMap.get(file)))
                            .collect(Collectors.toList());*/

                } else {
                    list.clear();
                }
                return null;
            }
        };
    }
}