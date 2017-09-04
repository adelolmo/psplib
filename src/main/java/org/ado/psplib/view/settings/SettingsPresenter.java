package org.ado.psplib.view.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
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

    @FXML
    private CheckBox checkBoxExtractIso;

    private Stage stage;
    private SettingsEventListener listener;
    private final DirectoryChooser fileChooser = new DirectoryChooser();

    public void setStage(Stage stage, SettingsEventListener listener) {
        this.stage = stage;
        this.listener = listener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFieldLibraryDirectory.setText(AppConfiguration.getConfiguration("lib.dir"));
        textFieldPspDirectory.setText(AppConfiguration.getConfiguration("psp.dir"));
        checkBoxExtractIso.setSelected(AppConfiguration.getConfigurationBoolean("iso.extract"));
    }

    public void save() {
        final String libraryDirectory =
                StringUtils.defaultIfBlank(textFieldLibraryDirectory.getText(), "");
        AppConfiguration
                .setConfiguration("lib.dir",
                        libraryDirectory);

        final String pspDirectory =
                StringUtils.defaultIfBlank(textFieldPspDirectory.getText(), "");
        AppConfiguration
                .setConfiguration("psp.dir",
                        pspDirectory);

        AppConfiguration
                .setConfigurationBoolean("iso.extract",
                        checkBoxExtractIso.isSelected());

        listener.configurationChange(libraryDirectory, pspDirectory, checkBoxExtractIso.isSelected());
        close();
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
        void configurationChange(String libraryDirectory, String pspDirectory, boolean extractIso);
    }
}
