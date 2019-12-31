package org.ado.psplib.psp;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.ado.psplib.common.AppConfiguration.getConfiguration;
import static org.apache.commons.io.FileUtils.listFilesAndDirs;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;

/**
 * @author Andoni del Olmo
 * @since 30/12/2019
 */
public class PspDevice {

    public static List<File> getInstalledGames() {
        if (!getPspGamesDirectory().exists()) {
            return Collections.emptyList();
        }

        return listFilesAndDirs(getPspGamesDirectory(), new WildcardFileFilter(new String[]{"*.cso", "*.iso"}), FILE)
                .stream()
                .sorted(comparing(File::getName))
                .filter(file -> !file.getAbsolutePath().equals(getPspGamesDirectory().getAbsolutePath() + "/.cso"))
                .collect(toList());
    }

    public static File getPspGamesDirectory() {
        return new File(getConfiguration("psp.dir"), "ISO");
    }
}
