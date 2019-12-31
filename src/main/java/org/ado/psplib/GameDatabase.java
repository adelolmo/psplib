package org.ado.psplib;

import org.ado.psplib.scancontent.Game;
import org.ado.psplib.scancontent.ScanContentService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andoni del Olmo
 * @since 22/04/19
 */
public class GameDatabase {

    public static Map<String, Game> getAll() {
        try {
            final CSVParser csv =
                    CSVParser.parse(ScanContentService.class.getResource("games.csv"),
                            StandardCharsets.UTF_8,
                            CSVFormat.newFormat(';').withFirstRecordAsHeader());
            final Map<String, Game> games = new HashMap<>();
            csv.iterator().forEachRemaining(strings ->
                    games.put(strings.get("id"),
                            Game.of(strings.get("id"),
                                    strings.get("title"),
                                    strings.get("genres"),
                                    strings.get("company"),
                                    strings.get("score"),
                                    strings.get("released_at"),
                                    ScanContentService.class.getResource(strings.get("id") + ".jpeg"))));
            return games;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load CSV");
        }
    }
}
