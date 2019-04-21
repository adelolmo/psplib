package org.ado.psplib.view;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.ado.psplib.common.FileSize;
import org.ado.psplib.gameloader.GameLoaderService;
import org.ado.psplib.install.InstallGameService;
import org.ado.psplib.scancontent.Game;
import org.ado.psplib.scancontent.ScanContentService;
import org.ado.psplib.uninstall.UninstallGameService;
import org.ado.psplib.view.about.AboutPresenter;
import org.ado.psplib.view.about.AboutView;
import org.ado.psplib.view.error.ErrorPresenter;
import org.ado.psplib.view.error.ErrorView;
import org.ado.psplib.view.scanErrors.ScanErrorsPresenter;
import org.ado.psplib.view.scanErrors.ScanErrorsView;
import org.ado.psplib.view.settings.SettingsPresenter;
import org.ado.psplib.view.settings.SettingsView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.ado.psplib.common.FilenameUtils.clean;
import static org.apache.commons.io.FileUtils.listFilesAndDirs;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class AppPresenter implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPresenter.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private final ScanContentService scanContentService;
    private final InstallGameService installGameService;
    private final UninstallGameService uninstallGameService;
    private final GameLoaderService gameLoaderService;

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

    @FXML
    private ComboBox<String> genreComboBox;

    private final ObservableList<GameView> gameViewObservableList = FXCollections.observableArrayList();
    private final List<GameView> fullGameList = new ArrayList<>();
    private final List<GameView> gameViews = new ArrayList<>();

    public AppPresenter() {
        final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        gameLoaderService = new GameLoaderService(gson);
        scanContentService = new ScanContentService(gson);
        installGameService = new InstallGameService();
        uninstallGameService = new UninstallGameService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePane.setVisible(false);
        sortComboBox.getItems().addAll(SortType.TITLE, SortType.SCORE, SortType.SIZE);
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSearch());
        genreComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSearch());
        gamesListView.setItems(gameViewObservableList);

        final CSVParser csv;
        try {
            csv = CSVParser.parse(ScanContentService.class.getResource("games.csv"),
                    Charset.forName(StandardCharsets.UTF_8.name()),
                    CSVFormat.newFormat(';').withFirstRecordAsHeader());


            csv.iterator().forEachRemaining(csvRecord ->
                    fullGameList.add(
                            new GameView("",
                                    Game.of(csvRecord.get("id"),
                                            csvRecord.get("title"),
                                            csvRecord.get("genres"),
                                            csvRecord.get("company"),
                                            csvRecord.get("score"),
                                            csvRecord.get("released_at"),
                                            ScanContentService.class.getResource(csvRecord.get("id") + ".jpeg")))));
        } catch (IOException e) {
            LOGGER.error("Cannot parse games csv file", e);
        }

        gameLoaderService.setOnRunning(event -> statusLabel.setText("Loading game library..."));
        gameLoaderService.setOnSucceeded(event -> {
            gameViews.clear();
            gameViews.addAll(gameLoaderService.getValue());
            gameViewObservableList.setAll(gameViews);
            statusLabel.setText(format("%d games found.", gamesListView.getItems().size()));
            populateGenres();
            refresh();
        });
        gameLoaderService.setOnFailed(event -> {
            statusLabel.setText("Error.");
            refresh();
        });

        scanContentService.setList(gameViewObservableList);
        scanContentService.setOnSucceeded(event -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText(format("Scan new content finished. %d games available.", gameViewObservableList.size()));
            populateGenres();
            refresh();
        });
        scanContentService.setOnFailed(event -> {
            statusLabel.setText("Scan new content failed!");
            refresh();
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
        });

        gamesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        gamesListView.setCellFactory(getHighlightCellFactory());

        installGameService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(format("Installing \"%s\" ...", newValue.game().title()));
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
            LOGGER.error("Game installation failed.", event.getSource().exceptionProperty().getValue());
            error(((Exception) event.getSource().exceptionProperty().getValue()).getMessage());
        });

        uninstallGameService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(format("Removing \"%s\" ...", newValue.game().title()));
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

    private void populateGenres() {
        final List<String> genres = new ArrayList<>();
        gameViewObservableList.forEach(gameView -> genres.addAll(asList(gameView.game().genres())));
        final List<String> uniqueGenres = genres.stream()
                .distinct()
                .sorted(String::compareTo)
                .collect(toList());
        uniqueGenres.add(0, "");
        genreComboBox.setItems(observableList(uniqueGenres));
    }

    public void onSearch() {
        final String libraryDir = getConfiguration("lib.dir");
        final List<GameView> collect = gameViewObservableList.stream()
                .filter(gameView -> gameView.game().title().toLowerCase()
                        .contains(searchTextField.getCharacters().toString()))
                .filter(gameView ->
                        isEmpty(genreComboBox.getValue())
                                || asList(gameView.game().genres()).contains(genreComboBox.getValue()))
                .sorted(((gw1, gw2) -> {
                    final SortType sortType = sortComboBox.getValue();
                    if (sortType != null) {
                        switch (sortType) {
                            case TITLE:
                                return gw1.game().title().compareTo(gw2.game().title());
                            case SCORE:
                                return Integer.compare(gw2.game().score(), gw1.game().score());
                            case SIZE:
                                return Long.compare(
                                        new File(libraryDir, gw2.fileBaseName() + ".cso").length(),
                                        new File(libraryDir, gw1.fileBaseName() + ".cso").length());
                            default:
                                return gw1.game().title().compareTo(gw2.game().title());
                        }
                    } else {
                        return 1;
                    }
                })).collect(toList());

        gamesListView.setItems(observableList(collect));

        refresh();
    }

    private void refresh() {
        gamesListView.setCellFactory(getHighlightCellFactory());
        gamesListView.setOnMouseClicked(onClickMyGames());
        refreshSpaceProgressBar();
    }

    private File getPspGamesDirectory() {
        return new File(getConfiguration("psp.dir"), "ISO");
    }

    private void refreshSpaceProgressBar() {
        final File pspGamesDirectory = getPspGamesDirectory();
        final long freeSpace = pspGamesDirectory.getFreeSpace();
        freeLabel.setText(new FileSize(freeSpace).toMegaBytes() + " MB Free");
        final long usedSpace = pspGamesDirectory.getTotalSpace() - freeSpace;
        spaceProgressBar.setProgress(((usedSpace * 100) / (double) pspGamesDirectory.getTotalSpace() / 100));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void settings() {
        final Stage stage = new Stage();
        final SettingsView settingsView = new SettingsView();
        final SettingsPresenter presenter = (SettingsPresenter) settingsView.getPresenter();
        presenter.setStage(stage, (String libraryDirectory, String pspDirectory, boolean extractIso) -> {
            gameLoaderService.setLibraryDirectory(libraryDirectory);
            gameLoaderService.restart();
        });
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(settingsView.getView()));
        stage.setTitle("Settings");
        stage.show();
    }

    private void error(String message) {
        final Stage stage = new Stage();
        final ErrorView errorView = new ErrorView();
        final ErrorPresenter presenter = (ErrorPresenter) errorView.getPresenter();
        presenter.setStage(stage, message);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(errorView.getView()));
        stage.setTitle("Error");
        stage.show();
    }

    public void scanForNewContent() {
        if (!scanContentService.isRunning()) {
            statusLabel.textProperty().bind(scanContentService.messageProperty());
            scanContentService.reset();
            scanContentService.start();
        }
    }

    public void exit() {
        stage.close();
    }

    public void about() {
        final Stage stage = new Stage();
        final AboutView aboutView = new AboutView();
        final AboutPresenter presenter = (AboutPresenter) aboutView.getPresenter();
        presenter.setStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(aboutView.getView()));
        stage.setTitle("About");
        stage.show();
    }

    public void scanErrors() {
        final Stage stage = new Stage();
        final ScanErrorsView aboutView = new ScanErrorsView();
        final ScanErrorsPresenter presenter = (ScanErrorsPresenter) aboutView.getPresenter();
        presenter.setStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(aboutView.getView()));
        stage.setTitle("Scan Errors");
        stage.show();
    }

    public void showMyLibrary() {
        gamePane.setVisible(false);
        gameViewObservableList.clear();
        gameLoaderService.reset();
        gameLoaderService.start();
        gamesListView.setItems(gameViewObservableList);
        gamesListView.setCellFactory(getHighlightCellFactory());
        gamesListView.setOnMouseClicked(onClickMyGames());
    }

    public void showCompleteLibrary() {
        installButton.setText("Install");
        installButton.setDisable(true);
        uninstallButton.setDisable(true);
        gamePane.setVisible(false);
        statusLabel.setText("");
//        gameViewObservableList.setAll(fullGameList);
        gamesListView.setItems(observableList(fullGameList));

        gamesListView.setOnMouseClicked(event -> {
            final ObservableList<GameView> selectedItems = gamesListView.getSelectionModel().getSelectedItems();
            gamePane.setVisible(false);
            if (!selectedItems.isEmpty()) {
                gamePane.setVisible(true);

                final GameView gameView = selectedItems.get(0);
                companyLabel.setText(gameView.game().company());
                releaseDateLabel.setText(DATE_FORMAT.format(gameView.game().releaseDate()));
                genreLabel.setText(join(", ", (CharSequence[]) gameView.game().genres()));
                scoreLabel.setText(valueOf(gameView.game().score()) + "/100");
                loadGameImage(gameView.game().cover());
            }
        });
//        gamesListView.refresh();
        refresh();
    }

    public void showInstalledGames() {
        gameViewObservableList.clear();

        installButton.setText("Install");
        installButton.setDisable(true);
        uninstallButton.setDisable(false);
        gamePane.setVisible(false);
        statusLabel.setText("");

        gameLoaderService.reset();
        gameLoaderService.start();

        final List<String> installGameFilenames = getInstalledGames().stream()
                .map(file -> getBaseName(file.getName()))
                .collect(toList());

        final List<GameView> installedGames = new ArrayList<>();
        for (GameView gameView : gameViews) {
            final String fileBaseName = gameView.fileBaseName();
            if (installGameFilenames.contains(fileBaseName)) {
                installedGames.add(gameView);
            }
        }

        gamesListView.setCellFactory(param -> new ListCell<GameView>() {
            @Override
            protected void updateItem(GameView gameView, boolean empty) {
                super.updateItem(gameView, empty);
                getStyleClass().remove("gameInstalled");
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
            }
        });

//        gameViewObservableList.setAll(installedGames);
        gamesListView.setItems(observableList(installedGames));


//        gamesListView.setOnMouseClicked(onClickMyGames());
    }

    public void install() {
        if (!installGameService.isRunning()) {
            installGameService.setGames(new ArrayList<>(gamesListView.getSelectionModel().getSelectedItems()));
            installGameService.reset();
            installGameService.start();
        } else {
            LOGGER.warn("Installation process is currently running.");
        }
    }

    public void uninstall() {
        if (!uninstallGameService.isRunning()) {
            uninstallGameService.setGames(new ArrayList<>(gamesListView.getSelectionModel().getSelectedItems()));
            uninstallGameService.reset();
            uninstallGameService.start();
        } else {
            LOGGER.warn("Removal process is currently running.");
        }
    }

    private EventHandler<MouseEvent> onClickMyGames() {
        return event -> {
            final ObservableList<GameView> selectedItems = gamesListView.getSelectionModel().getSelectedItems();
            if (!selectedItems.isEmpty()) {
                gamePane.setVisible(true);

                final GameView gameView = selectedItems.get(0);
                companyLabel.setText(gameView.game().company());
                releaseDateLabel.setText(DATE_FORMAT.format(gameView.game().releaseDate()));
                genreLabel.setText(join(", ", (CharSequence[]) gameView.game().genres()));
                scoreLabel.setText(valueOf(gameView.game().score()) + "/100");
                loadGameImage(gameView.game().cover());

                final File csoGame = new File(getConfiguration("lib.dir"), gameView.fileBaseName() + ".cso");
                sizeLabel.setText(new FileSize(csoGame.length()).toMegaBytes() + " MB");
                final File isoGame = new File(getConfiguration("lib.dir"), gameView.fileBaseName() + ".iso");
                if (isoGame.exists()) {
                    sizeLabel.setText(new FileSize(isoGame.length()).toMegaBytes() + " MB");
                }

                final boolean isInstalled = AppPresenter.this.getInstalledGames().stream()
                        .map(file -> getBaseName(file.getName()))
                        .collect(toList())
                        .contains(gameView.fileBaseName());

                installButton.setText("Install");
                installButton.setDisable(true);
                uninstallButton.setDisable(true);

                if (isInstalled) {
                    uninstallButton.setDisable(false);
                } else {
                    installButton.setText("Install");
                    if (selectedItems.size() > 1) {
                        installButton.setText(format("Install (%d)", selectedItems.size()));
                    }
                    installButton.setDisable(false);
                }
            } else {
                gamePane.setVisible(false);
            }
        };
    }

    private void loadGameImage(URL cover) {
        try {
            gameImageView.setImage(null);
            if (cover != null) {
                gameImageView.setImage(new Image(cover.openStream()));
            }

        } catch (IOException e) {
            LOGGER.error("Cannot show cover image", e);
        }
    }

    private List<File> getInstalledGames() {
        if (getPspGamesDirectory().exists()) {
            return listFilesAndDirs(getPspGamesDirectory(), new WildcardFileFilter(new String[]{"*.cso", "*.iso"}), FILE)
                    .stream()
                    .sorted(comparing(File::getName))
                    .filter(file -> !file.getAbsolutePath().equals(getPspGamesDirectory().getAbsolutePath() + "/.cso"))
                    .collect(toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Callback<ListView<GameView>, ListCell<GameView>> getHighlightCellFactory() {
        final List<String> installedGames = getInstalledGames().stream()
                .map(file -> getBaseName(file.getName()))
                .collect(toList());
        return param -> new ListCell<GameView>() {
            @Override
            protected void updateItem(GameView gameView, boolean empty) {
                super.updateItem(gameView, empty);
                getStyleClass().remove("gameInstalled");
                if (!empty) {
                    Platform.runLater(() -> setText(gameView.game().title()));
                    if (installedGames.contains(clean(gameView.fileBaseName()))) {
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