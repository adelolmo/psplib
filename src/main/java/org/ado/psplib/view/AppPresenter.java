package org.ado.psplib.view;

import javafx.application.Platform;
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
import org.ado.psplib.core.GameView;
import org.ado.psplib.gameloader.GameLoaderService;
import org.ado.psplib.install.InstallGameService;
import org.ado.psplib.scancontent.ScanContentService;
import org.ado.psplib.uninstall.UninstallGameService;
import org.ado.psplib.view.about.AboutPresenter;
import org.ado.psplib.view.about.AboutView;
import org.ado.psplib.view.error.ErrorPresenter;
import org.ado.psplib.view.error.ErrorView;
import org.ado.psplib.view.settings.SettingsPresenter;
import org.ado.psplib.view.settings.SettingsView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.ado.psplib.common.AppConfiguration.getConfigurationProperty;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePane.setVisible(false);
        sortComboBox.getItems().addAll(SortType.TITLE, SortType.SCORE, SortType.SIZE);
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSearch());
        genreComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSearch());
        gameLoaderService.setList(gameViewObservableList);
        gamesListView.setItems(gameViewObservableList);
        gameLoaderService.valueProperty()
                .addListener((observable, oldValue, newValue) -> scoreLabel.setText("Loading games..."));
        gameLoaderService.setOnSucceeded(event -> {
            statusLabel.setText(format("%d games found.", gamesListView.getItems().size()));
            populateGenres();
            refresh();
        });
        gameLoaderService.setOnFailed(event -> {
            statusLabel.setText("Error.");
            refresh();
        });

        scanContentService.setList(gameViewObservableList);
        scanContentService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText(format("Processing \"%s\" ...", newValue.getName()));
            }
        });
        scanContentService.setOnSucceeded(event -> {
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
            LOGGER.error(event.getSource().exceptionProperty().getValue().toString());
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
        gameViewObservableList.forEach(gameView -> genres.addAll(Arrays.asList(gameView.game().genre())));
        final List<String> uniqueGenres = genres.stream()
                .distinct()
                .sorted(String::compareTo)
                .collect(Collectors.toList());
        uniqueGenres.add(0, "");
        genreComboBox.setItems(FXCollections.observableList(uniqueGenres));
    }

    public void onSearch() {
        final String libraryDir = getConfigurationProperty("lib.dir");
        final List<GameView> collect = gameViewObservableList.stream()
                .filter(gw -> gw.game().title().toLowerCase()
                        .contains(searchTextField.getCharacters().toString()))
                .filter(gameView ->
                        StringUtils.isEmpty(genreComboBox.getValue())
                                || Arrays.asList(gameView.game().genre()).contains(genreComboBox.getValue()))
                .sorted(((gw1, gw2) -> {
                    final SortType sortType = sortComboBox.getValue();
                    if (sortType != null) {
                        switch (sortType) {
                            case TITLE:
                                return gw1.game().title().compareTo(gw2.game().title());
                            case SCORE:
                                return Integer.compare(gw2.game().metaScore(), gw1.game().metaScore());
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
                })).collect(Collectors.toList());

        gamesListView.setItems(FXCollections.observableList(collect));

        refresh();
    }

    private void refresh() {
        gamesListView.setCellFactory(getHighlightCellFactory());
        onGameClicked();
        refreshSpaceProgressBar();
    }

    private File getPspDirectory() {
        return new File(getConfigurationProperty("psp.dir"), "ISO");
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
        presenter.setStage(stage, (String libraryDirectory, String pspDirectory, boolean extractIso) -> {
            gameLoaderService.setLibraryDirectory(libraryDirectory);
            gameLoaderService.restart();
        });
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(settingsView.getView()));
        stage.setTitle("Settings");
        stage.show();
    }

    public void error(String message) {
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
                        new File(getConfigurationProperty("lib.dir"), gameView.fileBaseName() + ".jpg");
                if (cover.exists()) {
                    gameImageView.setImage(new Image(new FileInputStream(cover)));
                } else {
                    gameImageView.setImage(null);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
            sizeLabel.setText(
                    (new File(getConfigurationProperty("lib.dir"), gameView.fileBaseName() + ".cso").length() / 1024)
                            / 1024 + " MB");

            final List<String> installedGames = getInstalledGames().stream()
                    .map(file -> FilenameUtils.getBaseName(file.getName()))
                    .collect(Collectors.toList());
            if (new File(getConfigurationProperty("psp.dir")).exists()
                    && !installedGames.contains(gameView.fileBaseName())) {
                installButton.setText(format("Install (%d)", selectedItems.size()));
                installButton.setDisable(false);
                uninstallButton.setDisable(true);
            } else {
                installButton.setText("Install");
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
                    new WildcardFileFilter(new String[]{"*.cso", "*.iso"}),
                    FileFileFilter.FILE).stream()
                    .sorted(Comparator.comparing(File::getName))
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
                    Platform.runLater(() -> setText(gameView.game().title()));
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