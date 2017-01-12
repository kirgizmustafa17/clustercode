package net.chrigel.clustercode.scan;

import java.nio.file.Path;
import java.util.Optional;

public interface ProfileParser {

    /**
     * Reads and parses the given text file. Logs a warning if the file could not be read and parsed. Empty lines and
     * lines beginning with "#" are treated as comments and ignored. Expects UTF-8 formatted files.
     *
     * @param path the path to the file.
     * @return an optional containing the profile. Empty if the file could not be read.
     */
    Optional<Profile> parseFile(Path path);

}