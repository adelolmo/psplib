package org.ado.psplib.view.scanErrors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.ado.psplib.GameDatabase;
import org.ado.psplib.GameMetadata;
import org.ado.psplib.scancontent.Game;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static javafx.collections.FXCollections.observableList;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * @author Andoni del Olmo
 * @since 14.01.17
 */
public class ScanErrorsPresenter implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanErrorsPresenter.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final String ISO_WILDCARD = "*.iso";
    private static final String CSO_WILDCARD = "*.cso";
    final Map<String, List<Game>> suggestionsMap = new HashMap<>();
    final LevenshteinDistance distance = new LevenshteinDistance();
    private final GameMetadata gameMetadata;
    private final List<Game> fullGameList = new ArrayList<>();
    @FXML
    public ListView<String> unknownGameListView;
    @FXML
    public TabPane tabPaneSuggestions;
    @FXML
    public ListView<Game> suggestionsListView;
    @FXML
    public Button selectButton;
    @FXML
    private ListView<Game> gamesListView;
    @FXML
    private Pane gamePane;
    @FXML
    private Label companyLabel;

    private Stage stage;
    @FXML
    private Label releaseDateLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label scoreLabel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private ImageView gameImageView;

    public ScanErrorsPresenter() {
        final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        gameMetadata = new GameMetadata(gson);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        gamePane.setVisible(false);

        unknownGameListView.setOnMouseClicked(event -> {
            final String selectedFilename =
                    unknownGameListView.getSelectionModel().getSelectedItems().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No unknown game selected"));
            final ObservableList<Game> observableList = FXCollections.observableArrayList();
            observableList.setAll(suggestionsMap.get(selectedFilename));
            suggestionsListView.setItems(observableList);
        });

        suggestionsListView.setOnMouseClicked(onClickMyGames());

        GameDatabase.getAll()
                .forEach((s, game) -> fullGameList.add(game));
        fullGameList.sort(Comparator.comparing(Game::title));
        gamesListView.setItems(observableList(fullGameList));
        gamesListView.setOnMouseClicked(onClickMyGames());

        tabPaneSuggestions.setOnMouseClicked(event -> gamePane.setVisible(false));

        selectButton.setOnMouseClicked(event -> {
            final String basename = unknownGameListView.getSelectionModel().getSelectedItem();
            final Game game = selectedGame(tabPaneSuggestions.getSelectionModel());
            try {
                gameMetadata.writeToDisk(getConfiguration("lib.dir"), basename, game);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showUnknownGames();
            gamePane.setVisible(false);
            suggestionsListView.getItems().clear();
            tabPaneSuggestions.getSelectionModel().select(0);
        });

        showUnknownGames();
    }

    private void showUnknownGames() {
        unknownGameListView.getItems().clear();
        final Map<String, Game> games = GameDatabase.getAll();
        FileUtils.listFilesAndDirs(new File(getConfiguration("lib.dir")),
                new WildcardFileFilter(new String[]{CSO_WILDCARD, ISO_WILDCARD}),
                FileFileFilter.FILE)
                .stream()
                .filter(file ->
                        file.getAbsolutePath().length()
                                > getConfiguration("lib.dir").length() + 5)
                .filter(file -> !new File(getConfiguration("lib.dir"),
                        getBaseName(file.getName()) + ".json").exists())
                .map(file -> getBaseName(file.getName()))
                .distinct()
                .sorted(Comparator.comparing(s -> s))
                .collect(Collectors.toList())
                .forEach(filename -> {
                    unknownGameListView.getItems().add(filename);

                    final List<Game> sortedCsvList = games.values().stream()
                            .filter(strings -> getDistance(distance, strings.title(), filename) > -1)
                            .sorted(Comparator.comparing(o -> getDistance(distance, o.title(), filename)))
                            .collect(Collectors.toList());

                    final List<Game> titles = sortedCsvList.stream().limit(10)
                            .collect(Collectors.toList());
                    suggestionsMap.put(filename, titles);
                });
    }

    private Game selectedGame(SingleSelectionModel<Tab> selectionModel) {
        final Game suggestion = suggestionsListView.getSelectionModel().getSelectedItem();
        switch (selectionModel.getSelectedIndex()) {
            case 0: // suggestions
                return suggestion;
            case 1: // all
                return gamesListView.getSelectionModel().getSelectedItem();
            default:
                return suggestion;
        }
    }

    public void close() {
        stage.close();
    }

    private Integer getDistance(LevenshteinDistance distance, String title, String fileName) {
        return distance.apply(fileName, title);
    }

    private EventHandler<MouseEvent> onClickMyGames() {
        return event -> {
            final ListView<Game> gamesList = (ListView<Game>) event.getSource();
            final ObservableList<Game> selectedItems = gamesList.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                gamePane.setVisible(false);
                return;
            }
            gamePane.setVisible(true);

            final Game game = selectedItems.get(0);
            companyLabel.setText(game.company());
            releaseDateLabel.setText(DATE_FORMAT.format(game.releaseDate()));
            genreLabel.setText(join(", ", game.genres()));
            scoreLabel.setText(game.score() + "/100");
            loadGameImage(game.cover());
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

}
