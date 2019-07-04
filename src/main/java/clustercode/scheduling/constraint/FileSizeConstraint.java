package clustercode.scheduling.constraint;

import clustercode.api.domain.Media;
import clustercode.impl.util.InvalidConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

/**
 * This constraint checks the file size of the given argument. If the file is too big or too small it will be
 * rejected. The limits are configurable. If the minimum or maximum size are 0 (zero), the check is disabled
 * (for its respective limit).
 */
@Slf4j
public class FileSizeConstraint
        extends AbstractConstraint {

    public static long BYTES = 1;
    public static long KIBI_BYTES = BYTES * 1024;
    public static long MEBI_BYTES = KIBI_BYTES * 1024;
    private final Path baseInputDir;
    private final double minSize;
    private final double maxSize;
    private final DecimalFormat formatter = new DecimalFormat("#.####");

    FileSizeConstraint(Path baseInputDir, double minSize, double maxSize) {
        checkConfiguration(minSize, maxSize);
        this.baseInputDir = baseInputDir;
        this.minSize = minSize * MEBI_BYTES;
        this.maxSize = maxSize * MEBI_BYTES;
    }

    private void checkConfiguration(double minSize, double maxSize) {
        if (Math.min(minSize, maxSize) > 0 && minSize > maxSize) {
            throw new InvalidConfigurationException("minSize cannot be greater than maxSize. Min: {}, Max: {}",
                    minSize, maxSize);
        }
        if (Math.min(minSize, maxSize) < 0) {
            throw new InvalidConfigurationException("File sizes cannot contain negative values. Min: {}, Max: {}",
                    minSize, maxSize);
        }
        if (minSize == maxSize) {
            throw new InvalidConfigurationException("File sizes cannot be equal. Min: {}, Max: {}",
                    minSize, maxSize);
        }
    }

    @Override
    public boolean accept(Media candidate) {
        var file = candidate.getFullPath().get();
        MDC.put("min_MB_required", formatNumber(minSize / MEBI_BYTES));
        MDC.put("max_MB_allowed", formatNumber(maxSize / MEBI_BYTES));
        MDC.put("file", file.toString());
        try {
            long size = Files.size(file);
            MDC.put("source_MB", formatNumber(size / MEBI_BYTES));
            if (minSize > 0 && maxSize > 0) {
                // file between max and min
                return logAndReturnResult(size >= minSize && size <= maxSize);
            } else if (minSize <= 0) {
                // size smaller than max, min disabled
                return logAndReturnResult(size <= maxSize);
            } else {
                // size greater than min, max disabled
                return logAndReturnResult(size >= minSize);
            }
        } catch (IOException e) {
            MDC.put("error", e.getMessage());
            log.warn("Could not determine file size. Declined file.");
            return false;
        } finally {
            MDC.remove("min_MB_required");
            MDC.remove("max_MB_allowed");
            MDC.remove("file");
            MDC.remove("source_MB");
            MDC.remove("error");
        }
    }

    private String formatNumber(double number) {
        return formatter.format(number);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}

