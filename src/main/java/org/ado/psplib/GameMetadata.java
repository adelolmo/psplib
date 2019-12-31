package org.ado.psplib;

import com.google.gson.Gson;
import org.ado.psplib.scancontent.Game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Andoni del Olmo
 * @since 22/04/19
 */
public class GameMetadata {

    private final Gson gson;

    public GameMetadata(Gson gson) {
        this.gson = gson;
    }

    public void writeToDisk(String libDir, String baseName, final Game game) throws IOException {
        final FileWriter fileWriter = new FileWriter(new File(libDir, baseName + ".json"));
        gson.toJson(game, fileWriter);
        fileWriter.flush();

        final URL url = game.cover();
        if (url == null) {
            return;
        }
        try (InputStream in = url.openStream()) {
            final Path target = Paths.get(libDir, baseName + ".jpeg");
            Files.copy(in, target, REPLACE_EXISTING);
        }

    }
}
