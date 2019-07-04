package clustercode.scan;

import clustercode.api.domain.Media;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class MediaScanServiceImpl implements MediaScanService {

    private final MediaScanConfig scanConfig;
    private final Supplier<FileScanner> fileScannerSupplier;

    public MediaScanServiceImpl(MediaScanConfig scanConfig,
                                Supplier<FileScanner> fileScannerSupplier) {
        this.scanConfig = scanConfig;
        this.fileScannerSupplier = fileScannerSupplier;
    }

    @Override
    public Map<Path, List<Media>> retrieveFiles() {
        try {
            MDC.put("root", scanConfig.base_input_dir().toString());
            log.debug("Scanning for media files.");
            return fileScannerSupplier.get()
                .searchIn(scanConfig.base_input_dir())
                .withRecursion(false)
                .withDirectories(true)
                .stream()
                .filter(this::isPriorityDirectory)
                .peek(path -> MDC.put("dir", path.toString()))
                .peek(path -> log.debug("Found input directory."))
                .collect(Collectors.toMap(
                    Function.identity(), this::getListOfMediaFiles));
        } finally {
            MDC.remove("root");
            MDC.remove("dir");
        }
    }

    @Override
    public List<Media> retrieveFilesAsList() {
        return retrieveFiles()
            .values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Collects a list of possible media candidates that are recursively found under the given path.
     *
     * @param path the path to the root directory.
     * @return a list of candidates which may empty on error or none found.
     */
    List<Media> getListOfMediaFiles(Path path) {
        try {
            MDC.put("path", path.toString());
            log.debug("Scanning for media files.");
            return fileScannerSupplier.get()
                .searchIn(path)
                .withRecursion(true)
                .withFileExtensions(scanConfig.allowed_extensions())
                //.whileSkippingExtraFilesWith(scanConfig.skip_extension_name())
                //.whileSkippingExtraFilesIn(scanConfig.mark_source_dir())
                .streamAndIgnoreErrors()
                .map(file -> buildMedia(path, file))
                .peek(m -> MDC.put("media", m.toString()))
                .peek(m -> log.debug("Found file."))
                .collect(Collectors.toList());
        } finally {
            MDC.remove("path");
            MDC.remove("media");
        }
    }

    /**
     * Creates a media object with the given priority dir and file location.
     *
     * @param priorityDir the root path, which must start with a number.
     * @param file        the file name, which will be relativized against the base input dir.
     * @return new media object.
     */
    Media buildMedia(Path priorityDir, Path file) {
        var prio = getNumberFromDir(priorityDir);
        return Media.fromPath(
            scanConfig.base_input_dir(),
            scanConfig.base_input_dir().resolve(String.valueOf(prio)).relativize(file),
            prio);
    }

    /**
     * Indicates whether the given path starts with a number {@literal >= 0}.
     *
     * @param path the path.
     * @return true if the filename is {@literal >= 0}.
     */
    boolean isPriorityDirectory(Path path) {
        try {
            return getNumberFromDir(path) >= 0;
        } catch (NumberFormatException ex) {
            log.debug(ex.getMessage());
            return false;
        }
    }

    /**
     * Get the number of the file path.
     *
     * @param path a relative path which starts with a number.
     * @return the number of the file name.
     * @throws NumberFormatException if the path could not be parsed and is not a number.
     */
    int getNumberFromDir(Path path) {
        return Integer.parseInt(path.getFileName().toString());
    }

}
