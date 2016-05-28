package org.ado.psplib;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.ado.psplib.view.AppPresenter;
import org.ado.psplib.view.AppView;

import java.io.IOException;

/**
 * @author Andoni del Olmo
 * @since 14.05.16
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        final AppView appView = new AppView();
        final Scene scene = new Scene(appView.getView());
        final AppPresenter presenter = (AppPresenter) appView.getPresenter();
        presenter.setStage(primaryStage);
        primaryStage.setTitle("PSP Library");
        primaryStage.setScene(scene);
        primaryStage.setMaxHeight(Double.MAX_VALUE);
        primaryStage.setMaxWidth(Double.MAX_VALUE);
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
        primaryStage.show();
    }
}