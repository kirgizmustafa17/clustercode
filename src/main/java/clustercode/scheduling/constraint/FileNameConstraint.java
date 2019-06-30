package clustercode.scheduling.constraint;

import clustercode.api.domain.Media;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Provides a constraint which enables file name checking by regex. The input path of the candidate is being
 * checked (relative, without base input directory). Specify a Java-valid regex pattern, otherwise a runtime
 * exception is being thrown.
 */
@Slf4j
public class FileNameConstraint extends AbstractConstraint {

    private final Pattern pattern;

    FileNameConstraint(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean accept(Media candidate) {
        try {
            var toTest = candidate.getRelativePath().orElse(Paths.get("")).toString();
            MDC.put("path", toTest);
            MDC.put("regex", pattern.pattern());
            return logAndReturnResult(pattern.matcher(toTest).matches());
        } finally {
            MDC.remove("path");
            MDC.remove("regex");
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
