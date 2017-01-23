package org.ado.psplib.common;

/**
 * @author Andoni del Olmo
 * @since 23.01.17
 */
public class FileSize {

    private final long bytes;

    public FileSize(long bytes) {
        this.bytes = bytes;
    }

    public long toKiloBytes() {
        return bytes / 1024;
    }

    public long toMegaBytes() {
        return bytes / 1024 / 1024;
    }
}