package org.ado.psplib.scancontent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andoni del Olmo
 * @since 25.08.17
 */
public class GameIdGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameIdGenerator.class);

    public static String toId(final String gameName) {
        final String gameIdWithNoSymbols = gameName.toLowerCase()
                .replaceAll(" ", "-")
                .replaceAll("\\.", "-")
                .replaceAll("'", "")
                .replaceAll(": ", "-")
                .replaceAll(":", "-")
                .replaceAll("; ", "-")
                .replaceAll(";", "-")
                .replaceAll(", ", "-")
                .replaceAll(",", "-")
                .replaceAll("/ ", "-")
                .replaceAll("/", "-")
                .replaceAll("\\? ", "-")
                .replaceAll("\\?", "-")
                .replaceAll("! ", "-")
                .replaceAll("!", "-")
                .replaceAll("&", "and")
                .replaceAll("(\\.|-| )xx[-](!l)?", "-20-")
                .replaceAll("(\\.|-| )xix[-]?", "-19-")
                .replaceAll("(\\.|-| )xviii[-]?", "-18-")
                .replaceAll("(\\.|-| )xvii[-]?", "-17-")
                .replaceAll("(\\.|-| )xvi[-]?", "-16-")
                .replaceAll("(\\.|-| )xv[-]?", "-15-")
                .replaceAll("(\\.|-| )xiv[-]?", "-14-")
                .replaceAll("(\\.|-| )xiii[-]?", "-13-")
                .replaceAll("(\\.|-| )xii[-]?", "-12-")
                .replaceAll("(\\.|-| )xi[-]?", "-11-")
                .replaceAll("(\\.|-| )x[-](!l)?", "-10-")
                .replaceAll("(\\.|-| )ix[-]?", "-9-")
                .replace("-viii-", "-8-")
                .replace("-vii-", "-7-")
                .replace("-vi-", "-6-")
                .replace("-v-", "-5-")
                .replace("-iv-", "-4-")
                .replace("-iii-", "-3-")
                .replace("-ii-", "-2-")
                .replace("-i-", "-1-")
                .replaceAll("(-viii)$", "-8")
                .replaceAll("(-vii)$", "-7")
                .replaceAll("(-vi)$", "-6")
                .replaceAll("(-v)$", "-5")
                .replaceAll("(-iv)$", "-4")
                .replaceAll("(-iii)$", "-3")
                .replaceAll("(-ii)$", "-2")
                .replaceAll("(-i)$", "-1");
        final String gameIdWithNoMultipleDashes =
                gameIdWithNoSymbols
                        .replaceAll("--", "-")
                        .replaceAll("--", "-");
        return gameIdWithNoMultipleDashes
                .endsWith("-") ?
                gameIdWithNoMultipleDashes
                        .substring(0, gameIdWithNoMultipleDashes.length() - 1)
                : gameIdWithNoMultipleDashes;
    }
}