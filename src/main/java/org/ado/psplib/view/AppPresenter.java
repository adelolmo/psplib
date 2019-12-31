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
import org.ado.psplib.GameDatabase;
import org.ado.psplib.GameMetadata;
import org.ado.psplib.common.FileSize;
import org.ado.psplib.gameloader.GameLoaderService;
import org.ado.psplib.install.InstallGameService;
import org.ado.psplib.psp.PspDevice;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.ado.psplib.common.FilenameUtils.clean;
import static org.apache.commons.io.FilenameUtils.getBaseName;
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

    private final List<GameView> myLibraryGameList = new ArrayList<>();
    private final List<GameView> completeGameList = new ArrayList<>();
    private final List<GameView> installedGameList = new ArrayList<>();
    private final ObservableList<GameView> myLibraryViewObservableList = FXCollections.observableArrayList();
    private final ObservableList<GameView> completeLibraryObservableList = FXCollections.observableArrayList();
    private final ObservableList<GameView> installedGamesObservableList = FXCollections.observableArrayList();

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

    @FXML
    private ListView<GameView> myLibraryListView;
    @FXML
    private ListView<GameView> installedGamesListView;
    @FXML
    private ListView<GameView> completeLibraryListView;

    public AppPresenter() {
        final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        gameLoaderService = new GameLoaderService(gson);
        scanContentService = new ScanContentService(new GameMetadata(gson));
        installGameService = new InstallGameService();
        uninstallGameService = new UninstallGameService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePane.setVisible(false);
        sortComboBox.getItems().addAll(SortType.TITLE, SortType.SCORE, SortType.SIZE);
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSearch());
        genreComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSearch());

        myLibraryListView.toFront();
        myLibraryListView.setItems(myLibraryViewObservableList);
        myLibraryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        myLibraryListView.setCellFactory(getHighlightCellFactory());
        myLibraryListView.setOnMouseClicked(showGameDetailsEvent(myLibraryListView));

        installedGamesListView.setItems(installedGamesObservableList);
        installedGamesListView.setOnMouseClicked(showGameDetailsEvent(installedGamesListView));
        installedGamesListView.setCellFactory(param -> new ListCell<GameView>() {
            @Override
            protected void updateItem(GameView gameView, boolean empty) {
                super.updateItem(gameView, empty);
                getStyleClass().remove("gameInstalled");
                if (empty) {
                    Platform.runLater(() -> setText(""));
                    return;
                }
                Platform.runLater(() -> setText(gameView.game().title()));
            }
        });

        GameDatabase.getAll()
                .forEach((s, game) -> completeGameList.add(new GameView("", game)));
        completeGameList.sort(Comparator.comparing(gameView -> gameView.game().title()));
        completeLibraryObservableList.setAll(completeGameList);
        completeLibraryListView.setItems(completeLibraryObservableList);
        completeLibraryListView.setOnMouseClicked(event -> {
            final ObservableList<GameView> selectedItems = completeLibraryListView.getSelectionModel().getSelectedItems();
            gamePane.setVisible(false);
            if (!selectedItems.isEmpty()) {
                gamePane.setVisible(true);

                final GameView gameView = selectedItems.get(0);
                companyLabel.setText(gameView.game().company());
                releaseDateLabel.setText(DATE_FORMAT.format(gameView.game().releaseDate()));
                genreLabel.setText(join(", ", gameView.game().genres()));
                scoreLabel.setText(gameView.game().score() + "/100");
                loadGameImage(gameView.game().cover());
            }
        });

        gameLoaderService.setOnRunning(event -> statusLabel.setText("Loading game library..."));
        gameLoaderService.setOnSucceeded(event -> {
            myLibraryGameList.clear();
            myLibraryGameList.addAll(gameLoaderService.getValue());
            myLibraryViewObservableList.setAll(gameLoaderService.getValue());

            statusLabel.setText(format("%d games found.", myLibraryListView.getItems().size()));
            populateGenres();
            refreshInstalledGames();
            refreshSpaceProgressBar();
        });
        gameLoaderService.setOnFailed(event -> {
            statusLabel.setText("Error. > " + event.getSource().getMessage());
            refreshSpaceProgressBar();
        });

        scanContentService.setOnSucceeded(event -> {
            myLibraryGameList.addAll(scanContentService.getValue());
            myLibraryViewObservableList.setAll(myLibraryGameList);

            statusLabel.textProperty().unbind();
            statusLabel.setText(format("Scan new content finished. %d games available.", myLibraryGameList.size()));
            populateGenres();
            refreshInstalledGames();
            refreshSpaceProgressBar();
        });
        scanContentService.setOnFailed(event -> {
            statusLabel.setText("Scan new content failed!");
            refreshSpaceProgressBar();
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
        });

        installGameService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(format("Installing \"%s\" ...", newValue.game().title()));
                refreshSpaceProgressBar();
            }
        });
        installGameService.setOnSucceeded(event -> {
            statusLabel.setText("All games installed successfully");
            refreshInstalledGames();
            refreshSpaceProgressBar();
        });
        installGameService.setOnFailed(event -> {
            statusLabel.setText("Game(s) installation failed!");
            refreshSpaceProgressBar();
            LOGGER.error("Game installation failed. {}", event.getSource().exceptionProperty().getValue());
            error(((Exception) event.getSource().exceptionProperty().getValue()).getMessage());
        });

        uninstallGameService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(format("Removing \"%s\" ...", newValue.game().title()));
                refreshSpaceProgressBar();
            }
        });
        uninstallGameService.setOnSucceeded(event -> {
            statusLabel.setText("All games removed successfully");
            refreshInstalledGames();
            refreshSpaceProgressBar();
        });
        uninstallGameService.setOnFailed(event -> {
            statusLabel.setText("Game(s) removal failed!");
            refreshSpaceProgressBar();
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
        });

        refreshSpaceProgressBar();
        gameLoaderService.start();
    }

    private void refreshInstalledGames() {
        final List<String> installGameFilenames = PspDevice.getInstalledGames().stream()
                .map(file -> getBaseName(file.getName()))
                .collect(toList());
        installedGameList.clear();
        installedGameList.addAll(
                myLibraryGameList.stream()
                        .filter(gameView -> installGameFilenames.contains(gameView.fileBaseName()))
                        .collect(toList()));
        installedGamesObservableList.setAll(installedGameList);
    }

    private void populateGenres() {
        final List<String> genres = new ArrayList<>();
        myLibraryViewObservableList.forEach(gameView -> genres.addAll(asList(gameView.game().genres())));
        final List<String> uniqueGenres = genres.stream()
                .distinct()
                .sorted(String::compareTo)
                .collect(toList());
        uniqueGenres.add(0, "");
        genreComboBox.setItems(observableList(uniqueGenres));
    }

    public void onSearch() {
        myLibraryViewObservableList.setAll(applySearchFilters(myLibraryGameList));
        completeLibraryObservableList.setAll(applySearchFilters(completeGameList));
        installedGamesObservableList.setAll(applySearchFilters(installedGameList));
        refreshSpaceProgressBar();
    }

    private List<GameView> applySearchFilters(List<GameView> gameViewList) {
        return gameViewList.stream()
                .filter(gameView -> gameView.game().title().toLowerCase()
                        .contains(searchTextField.getCharacters().toString()))
                .filter(gameView ->
                        isEmpty(genreComboBox.getValue())
                                || asList(gameView.game().genres()).contains(genreComboBox.getValue()))
                .sorted(new GameViewComparator(sortComboBox.getValue(), getConfiguration("lib.dir")))
                .collect(toList());
    }

    private void refreshSpaceProgressBar() {
        final File pspGamesDirectory = PspDevice.getPspGamesDirectory();
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
        myLibraryListView.toFront();
        gamePane.setVisible(false);
    }

    public void showCompleteLibrary() {
        completeLibraryListView.toFront();
        installButton.setText("Install");
        installButton.setDisable(true);
        uninstallButton.setDisable(true);
        gamePane.setVisible(false);
        statusLabel.setText("");
        refreshSpaceProgressBar();
    }

    public void showInstalledGames() {
        installedGamesListView.toFront();
        installButton.setText("Install");
        installButton.setDisable(true);
        uninstallButton.setDisable(false);
        gamePane.setVisible(false);
        statusLabel.setText("");
    }

    public void install() {
        if (!installGameService.isRunning()) {
            installGameService.setGames(new ArrayList<>(myLibraryListView.getSelectionModel().getSelectedItems()));
            installGameService.reset();
            installGameService.start();
        } else {
            LOGGER.warn("Installation process is currently running.");
        }
    }

    public void uninstall() {
        if (!uninstallGameService.isRunning()) {
            uninstallGameService.setGames(new ArrayList<>(myLibraryListView.getSelectionModel().getSelectedItems()));
            uninstallGameService.reset();
            uninstallGameService.start();
            refreshSpaceProgressBar();
        } else {
            LOGGER.warn("Removal process is currently running.");
        }
    }

    private EventHandler<MouseEvent> showGameDetailsEvent(ListView<GameView> listView) {
        return event -> {
            final ObservableList<GameView> selectedItems = listView.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                gamePane.setVisible(false);
                return;
            }
            gamePane.setVisible(true);

            final GameView gameView = selectedItems.get(0);
            companyLabel.setText(gameView.game().company());
            releaseDateLabel.setText(DATE_FORMAT.format(gameView.game().releaseDate()));
            genreLabel.setText(join(", ", gameView.game().genres()));
            scoreLabel.setText(gameView.game().score() + "/100");
            loadGameImage(gameView.game().cover());

            final File csoGame = new File(getConfiguration("lib.dir"), gameView.csoFilename());
            sizeLabel.setText(new FileSize(csoGame.length()).toMegaBytes() + " MB");
            final File isoGame = new File(getConfiguration("lib.dir"), gameView.isoFilename());
            if (isoGame.exists()) {
                sizeLabel.setText(new FileSize(isoGame.length()).toMegaBytes() + " MB");
            }

            final boolean isInstalled = PspDevice.getInstalledGames().stream()
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

    private Callback<ListView<GameView>, ListCell<GameView>> getHighlightCellFactory() {
        final List<String> installedGames = PspDevice.getInstalledGames().stream()
                .map(file -> getBaseName(file.getName()))
                .collect(toList());
        return param -> new ListCell<GameView>() {
            @Override
            protected void updateItem(GameView gameView, boolean empty) {
                super.updateItem(gameView, empty);
                getStyleClass().remove("gameInstalled");
                if (empty) {
                    Platform.runLater(() -> setText(""));
                    return;
                }
                Platform.runLater(() -> setText(gameView.game().title()));
                if (installedGames.contains(clean(gameView.fileBaseName()))) {
                    getStyleClass().add("gameInstalled");
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
            }
        };
    }
}
