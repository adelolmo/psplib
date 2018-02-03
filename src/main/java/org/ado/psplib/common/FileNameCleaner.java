package org.ado.psplib.common;

import java.util.Arrays;

/**
 * https://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars
 *
 * @since 03.02.18
 */
public class FileNameCleaner {

    private final static int[] ILLEGAL_CHARS =
            {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                    25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    static {
        Arrays.sort(ILLEGAL_CHARS);
    }

    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int) badFileName.charAt(i);
            if (Arrays.binarySearch(ILLEGAL_CHARS, c) < 0) {
                cleanName.append((char) c);
            }
        }
        return cleanName.toString();
    }
}