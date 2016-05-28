package org.ado.psplib.view.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.ado.psplib.common.AppConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsPresenter implements Initializable {

    private final Logger LOGGER = LoggerFactory.getLogger(SettingsPresenter.class);

    @FXML
    private TextField textFieldLibraryDirectory;

    @FXML
    private TextField textFieldPspDirectory;

    private Stage stage;
    private SettingsEventListener listener;
    private final DirectoryChooser fileChooser = new DirectoryChooser();

    public void setStage(Stage stage, SettingsEventListener listener) {
        this.stage = stage;
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFieldLibraryDirectory.setText(AppConfiguration.getConfigurationProperty("lib.dir"));
        textFieldPspDirectory.setText(AppConfiguration.getConfigurationProperty("psp.dir"));
    }

    public void save() {
        final String libraryDirectory =
                StringUtils.defaultIfBlank(textFieldLibraryDirectory.getText(), "");
        AppConfiguration
                .setConfigurationProperty("lib.dir",
                        libraryDirectory);
        final String pspDirectory =
                StringUtils.defaultIfBlank(textFieldPspDirectory.getText(), "");
        AppConfiguration
                .setConfigurationProperty("psp.dir",
                        pspDirectory);
        listener.configurationChange(libraryDirectory, pspDirectory);
    }

    public void close() {
        stage.close();
    }

    public void browseLibraryDirectory() {
        configureDirectoryChooser("Library Directory", fileChooser);
        final File file = fileChooser.showDialog(stage);
        if (file != null) {
            LOGGER.debug("Library Directory {}", file.getAbsolutePath());
            textFieldLibraryDirectory.setText(file.getAbsolutePath());
        }
    }

    public void browsePspDirectory() {
        configureDirectoryChooser("PSP Directory", fileChooser);
        final File file = fileChooser.showDialog(stage);
        if (file != null) {
            LOGGER.debug("PSP Directory {}", file.getAbsolutePath());
            textFieldPspDirectory.setText(file.getAbsolutePath());
        }
    }

    private void configureDirectoryChooser(String title, DirectoryChooser fileChooser) {
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    public interface SettingsEventListener {
        void configurationChange(String libraryDirectory, String pspDirectory);
    }
}
