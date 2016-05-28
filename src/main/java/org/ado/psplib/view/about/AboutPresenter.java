package org.ado.psplib.view.about;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Andoni del Olmo
 * @since 28.05.16
 */
public class AboutPresenter implements Initializable {

    @FXML
    private Label labelApplicationVersion;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelApplicationVersion.setText(getImplementationVersion());
    }

    private String getImplementationVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(AboutPresenter.class.getResource("about.properties").openStream());
            return properties.getProperty("project.version");
        } catch (IOException e) {
            return "";
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void close() {
        stage.close();
    }

}