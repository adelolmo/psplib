package org.ado.psplib.install;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.ado.psplib.view.GameView;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.lang.String.format;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.ado.psplib.common.AppConfiguration.getConfigurationBoolean;
import static org.ado.psplib.common.FilenameUtils.clean;
import static org.apache.commons.io.FileUtils.copyFile;

/**
 * @author Andoni del Olmo
 * @since 16.05.16
 */
public class InstallGameService extends Service<GameView> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallGameService.class);

    private List<GameView> games;

    public void setGames(final List<GameView> games) {
        this.games = games;
    }

    @Override
    protected Task<GameView> createTask() {
        return new Task<GameView>() {
            @Override
            protected GameView call() throws Exception {
                final String libraryDirectoryName = getConfiguration("lib.dir");
                final String pspDirectoryName = getConfiguration("psp.dir");
                final boolean extractIso = getConfigurationBoolean("iso.extract");
                final File pspIsoDirectory = new File(pspDirectoryName, "ISO");
                LOGGER.info("Extract ISO: {}", extractIso);

                for (GameView gameView : games) {
                    updateValue(gameView);

                    final String csoFilename = gameView.fileBaseName() + ".cso";
                    final File csoFile = new File(libraryDirectoryName, csoFilename);
                    LOGGER.info("CSO file: {}", csoFile.getAbsoluteFile());

                    final String isoFilename = gameView.fileBaseName() + ".iso";
                    final File libIsoFile = new File(libraryDirectoryName, isoFilename);
                    final File pspIsoFile = new File(pspIsoDirectory, clean(isoFilename));

                    if (extractIso) {
                        if (libIsoFile.exists()) {
                            try {
                                copyFile(libIsoFile,
                                        pspIsoFile);
                            } catch (IOException e) {
                                throw new IOException(format("Cannot copy file %s to %s.",
                                        libIsoFile.getAbsolutePath(), pspIsoFile.getAbsolutePath()), e);
                            }
                            continue;
                        }

                        if (!csoFile.exists()) {
                            throw new IOException(format("File not found %s.", csoFile.getAbsolutePath()));
                        }
                        final Process extractProcess = Runtime.getRuntime()
                                .exec(new String[]{"/usr/bin/ciso", "0",
                                        csoFile.getAbsolutePath(),
                                        pspIsoFile.getAbsolutePath()}
                                );
                        try {
                            final int statusCode = extractProcess.waitFor();
                            final String input = IOUtils.toString(extractProcess.getInputStream(), StandardCharsets.UTF_8);
                            if (statusCode > 0 || input.startsWith("Usage")) {
                                LOGGER.error("Unable to extract CSO file into ISO. Cause: " + input);
                                throw new IOException(format("Unable to extract CSO file %s into ISO.", csoFile.getAbsolutePath()));
                            }
                        } finally {
                            extractProcess.destroy();
                        }

                    } else {
                        if (!csoFile.exists()) {
                            throw new IOException(format("File not found %s.", csoFile.getAbsolutePath()));
                        }
                        copyFile(csoFile,
                                new File(pspIsoDirectory, csoFilename));
                    }
                }
                return null;
            }
        };
    }
}
