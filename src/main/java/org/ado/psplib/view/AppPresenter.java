package org.ado.psplib.view;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.ado.psplib.Game;
import org.ado.psplib.common.AppConfiguration;
import org.ado.psplib.core.GameView;
import org.ado.psplib.service.GameLoaderService;
import org.ado.psplib.service.InstallGameService;
import org.ado.psplib.service.ScanContentService;
import org.ado.psplib.service.UninstallGameService;
import org.ado.psplib.view.settings.SettingsPresenter;
import org.ado.psplib.view.settings.SettingsView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class AppPresenter implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPresenter.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Inject
    private ScanContentService scanContentService;
    @Inject
    private InstallGameService installGameService;
    @Inject
    private UninstallGameService uninstallGameService;
    @Inject
    private GameLoaderService gameLoaderService;
    @Inject
    private Gson gson;

    private Stage stage;

    @FXML
    private ListView<GameView> gamesListView;

    @FXML
    private Label statusLabel;
    @FXML
    private Label freeLabel;
    @FXML
    private ProgressBar spaceProgressBar;

    @FXML
    private Pane gamePane;

    @FXML
    private ImageView gameImageView;

    @FXML
    private Label companyLabel;
    @FXML
    private Label releaseDateLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label sizeLabel;

    @FXML
    private Button installButton;

    @FXML
    private Button uninstallButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<SortType> sortComboBox;

    private final ObservableList<GameView> gameViewObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePane.setVisible(false);
        sortComboBox.getItems().addAll(SortType.TITLE, SortType.SCORE, SortType.SIZE);
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            onSearch();
        });
        gameLoaderService.setList(gameViewObservableList);
        gamesListView.setItems(gameViewObservableList);
        gameLoaderService.valueProperty().addListener((observable, oldValue, newValue) -> {
            scoreLabel.setText("Loading games...");
//            refresh();
        });
        gameLoaderService.setOnSucceeded(event -> {
            statusLabel.setText(String.format("%d games found.", gamesListView.getItems().size()));
            refresh();
        });
        gameLoaderService.setOnFailed(event -> {
            statusLabel.setText("Error.");
            refresh();
        });

        scanContentService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(String.format("Processing \"%s\" ...", newValue.getName()));
            }
        });
        scanContentService.setOnSucceeded(event -> {
//            final List<GameView> availableGames = getAvailableGames();
            statusLabel.setText(String.format("Scan new content finished. %d games available.", gameViewObservableList.size()));
//            gamesListView.setItems(FXCollections.observableList(availableGames));
            refresh();
        });
        scanContentService.setOnFailed(event -> {
            statusLabel.setText("Scan new content failed!");
//            gamesListView.setItems(FXCollections.observableList(getAvailableGames()));
            refresh();
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
        });

        gamesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        gamesListView.setCellFactory(getHighlightCellFactory());

        installGameService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(String.format("Installing \"%s\" ...", newValue.game().title()));
                refresh();
            }
        });
        installGameService.setOnSucceeded(event -> {
            statusLabel.setText("All games installed successfully");
            refresh();
        });
        installGameService.setOnFailed(event -> {
            statusLabel.setText("Game(s) installation failed!");
            refresh();
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
        });

        uninstallGameService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(String.format("Removing \"%s\" ...", newValue.game().title()));
                refresh();
            }
        });
        uninstallGameService.setOnSucceeded(event -> {
            statusLabel.setText("All games removed successfully");
            refresh();
        });
        uninstallGameService.setOnFailed(event -> {
            statusLabel.setText("Game(s) removal failed!");
            refresh();
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
        });

        refreshSpaceProgressBar();
        gameLoaderService.start();
    }

    public void onSearch() {
        final String libraryDir = AppConfiguration.getConfigurationProperty("lib.dir");
        final List<GameView> collect = gameViewObservableList.stream()
                .filter(gw -> {
                    return searchTextField.getCharacters().toString() != null ?
                            gw.game().title().toLowerCase()
                                    .contains(searchTextField.getCharacters().toString())
                            : true;
                })
                .sorted(((gw1, gw2) -> {
                    switch (sortComboBox.getValue()) {
                        case TITLE:
                            return gw1.game().title().compareTo(gw2.game().title());
                        case SCORE:
                            return Integer.compare(gw2.game().metaScore(), gw1.game().metaScore());
                        case SIZE:
                            return Long.compare(
                                    new File(libraryDir,gw2.fileBaseName() + ".cso").length(),
                                    new File(libraryDir,gw1.fileBaseName() + ".cso").length());
                        default:
                            return gw1.game().title().compareTo(gw2.game().title());
                    }
                })).collect(Collectors.toList());

        gamesListView.setItems(FXCollections.observableList(collect));

        refresh();
    }

    private List<GameView> getAvailableGames() {
        return getAvailableGames(null, SortType.TITLE);
    }

    private List<GameView> getAvailableGames(String searchSequence, SortType sortType) {
        final String libraryDir = AppConfiguration.getConfigurationProperty("lib.dir");
        final Collection<File> gameFiles = FileUtils.listFilesAndDirs(new File(libraryDir),
                new WildcardFileFilter("*.json"),
                FileFileFilter.FILE);
        final Map<File, Game> gameMap = new HashMap<>();
        final List<Game> collect = gameFiles.stream()
                .filter(file -> !file.getAbsolutePath().equals(libraryDir))
                .map(file -> {
                    try {
                        return gameMap.put(file, gson.fromJson(new FileReader(file), Game.class));
                    } catch (FileNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                }).collect(Collectors.toList());
        return gameFiles.stream()
                .filter(file -> !file.getAbsolutePath().equals(libraryDir))
                .filter(file -> {
                    return searchSequence != null ?
                            gameMap.get(file).title().toLowerCase().contains(searchSequence)
                            : true;
                })
                .sorted((f1, f2) -> compareGames(f1, f2, sortType, gameMap))
                .map(file -> new GameView(FilenameUtils.getBaseName(file.getName()),
                        gameMap.get(file)))
                .collect(Collectors.toList());
    }

    private int compareGames(File f1, File f2, SortType sortType, Map<File, Game> gameMap) {
        final Game gameOne = gameMap.get(f1);
        final Game gameTwo = gameMap.get(f2);
        switch (sortType) {
            case TITLE:
                return gameOne.title().compareTo(gameTwo.title());
            case SCORE:
                return Integer.compare(gameTwo.metaScore(), gameOne.metaScore());
            case SIZE:
                return Long.compare(getCsoFile(f2).length(), getCsoFile(f1).length());
            default:
                return gameOne.title().compareTo(gameTwo.title());
        }
    }

    private File getCsoFile(File file) {
        return new File(file.getAbsolutePath().replaceAll("\\.json", "\\.cso"));
    }

    private void refresh() {
        gamesListView.setCellFactory(getHighlightCellFactory());
        onGameClicked();
        refreshSpaceProgressBar();
    }

    private File getPspDirectory() {
        return new File(AppConfiguration.getConfigurationProperty("psp.dir"), "ISO");
    }

    private void refreshSpaceProgressBar() {
        final File pspDirectory = getPspDirectory();
        freeLabel.setText((pspDirectory.getFreeSpace() / 1024) / 1024 + " MB Free");
        final long usedSpace = pspDirectory.getTotalSpace() - pspDirectory.getFreeSpace();
        spaceProgressBar.setProgress(((usedSpace * 100) / (double) pspDirectory.getTotalSpace() / 100));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void settings() {
        final Stage stage = new Stage();
        final SettingsView settingsView = new SettingsView();
        final SettingsPresenter presenter = (SettingsPresenter) settingsView.getPresenter();
        presenter.setStage(stage, (String libraryDirectory, String pspDirectory) -> {
            gameLoaderService.setLibraryDirectory(libraryDirectory);
            gameLoaderService.restart();
//            refresh();
        });
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(settingsView.getView()));
        stage.setTitle("Settings");
        stage.show();
    }

    public void scanForNewContent() {
        if (!scanContentService.isRunning()) {
            scanContentService.reset();
            scanContentService.start();
        }
    }

    public void onGameClicked() {
        final ObservableList<GameView> selectedItems = gamesListView.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            gamePane.setVisible(true);

            final GameView gameView = selectedItems.get(0);
            companyLabel.setText(gameView.game().company());
            releaseDateLabel.setText(DATE_FORMAT.format(gameView.game().releaseDate()));
            genreLabel.setText(String.join(", ", (CharSequence[]) gameView.game().genre()));
            scoreLabel.setText(String.valueOf(gameView.game().metaScore()) + "/100");
            try {
                final File cover =
                        new File(AppConfiguration.getConfigurationProperty("lib.dir"), gameView.fileBaseName() + ".jpg");
                if (cover.exists()) {
                    gameImageView.setImage(new Image(new FileInputStream(cover)));
                } else {
                    gameImageView.setImage(null);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
            sizeLabel.setText(
                    (new File(AppConfiguration.getConfigurationProperty("lib.dir"), gameView.fileBaseName() + ".cso").length() / 1024)
                            / 1024 + " MB");

            final List<String> installedGames = getInstalledGames().stream()
                    .map(file -> FilenameUtils.getBaseName(file.getName()))
                    .collect(Collectors.toList());
            if (new File(AppConfiguration.getConfigurationProperty("psp.dir")).exists()) {
                if (installedGames.contains(gameView.fileBaseName())) {
                    installButton.setDisable(true);
                    uninstallButton.setDisable(false);
                } else {
                    installButton.setDisable(false);
                    uninstallButton.setDisable(true);
                }
            } else {
                installButton.setDisable(true);
                uninstallButton.setDisable(true);
            }
        } else {
            gamePane.setVisible(false);
        }
    }

    public void install() {
        if (!installGameService.isRunning()) {
            installGameService.setGames(gamesListView.getSelectionModel().getSelectedItems().stream()
                    .collect(Collectors.toList()));
            installGameService.reset();
            installGameService.start();
        } else {
            LOGGER.warn("Installation process is currently running.");
        }
    }

    public void uninstall() {
        if (!uninstallGameService.isRunning()) {
            uninstallGameService.setGames(gamesListView.getSelectionModel().getSelectedItems().stream()
                    .collect(Collectors.toList()));
            uninstallGameService.reset();
            uninstallGameService.start();
        } else {
            LOGGER.warn("Removal process is currently running.");
        }
    }

    private List<File> getInstalledGames() {
        if (getPspDirectory().exists()) {
            return FileUtils.listFilesAndDirs(getPspDirectory(),
                    new WildcardFileFilter("*.cso"),
                    FileFileFilter.FILE).stream()
                    .sorted((i1, i2) -> i1.getName().compareTo(i2.getName()))
                    .filter(file -> !file.getAbsolutePath().equals(getPspDirectory().getAbsolutePath() + "/.cso"))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Callback<ListView<GameView>, ListCell<GameView>> getHighlightCellFactory() {
        final List<String> installedGames = getInstalledGames().stream()
                .map(file -> FilenameUtils.getBaseName(file.getName()))
                .collect(Collectors.toList());
        return param -> new ListCell<GameView>() {
            @Override
            protected void updateItem(GameView gameView, boolean empty) {
                super.updateItem(gameView, empty);
                getStyleClass().remove("gameInstalled");
                if (!empty) {
                    setText(gameView.game().title());
                    if (installedGames.contains(gameView.fileBaseName())) {
                        getStyleClass().add("gameInstalled");
                    }
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
            }
        };
    }
}