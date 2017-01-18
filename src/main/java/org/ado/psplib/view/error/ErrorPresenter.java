package org.ado.psplib.view.error;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Andoni del Olmo
 * @since 14.01.17
 */
public class ErrorPresenter implements Initializable {

    @FXML
    private Label labelError;

    private Stage stage;

    public void setStage(Stage stage, String message) {
        this.stage = stage;
        labelError.setText(message);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void close() {
        stage.close();
    }

}