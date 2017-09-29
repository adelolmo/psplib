package org.ado.psplib.view.scanErrors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.ado.psplib.common.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author Andoni del Olmo
 * @since 14.01.17
 */
public class ScanErrorsPresenter implements Initializable {

    private static final String ISO_WILDCARD = "*.iso";
    private static final String CSO_WILDCARD = "*.cso";
    @FXML
    public TextArea list;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final String libraryDirectory = AppConfiguration.getConfiguration("lib.dir");

        list.setEditable(false);
        FileUtils.listFilesAndDirs(new File(libraryDirectory),
                new WildcardFileFilter(new String[]{CSO_WILDCARD, ISO_WILDCARD}),
                FileFileFilter.FILE)
                .stream()
                .filter(file ->
                        file.getAbsolutePath().length()
                                > libraryDirectory.length() + 5)
                .filter(file -> !new File(libraryDirectory,
                        FilenameUtils.getBaseName(file.getName()) + ".json").exists())
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList())
                .forEach(file -> list.appendText(file.getName() + "\n"));
        list.setScrollTop(Double.MIN_VALUE);
    }

    public void close() {
        stage.close();
    }

}