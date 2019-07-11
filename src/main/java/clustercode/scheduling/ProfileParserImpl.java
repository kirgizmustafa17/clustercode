package clustercode.scheduling;

import clustercode.api.domain.Profile;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ProfileParserImpl implements ProfileParser {

    public static final Pattern FORMAT_PATTERN = Pattern.compile("%\\{([a-zA-Z]+)=(.*)\\}");
    private final ProfileScanConfig config;

    public ProfileParserImpl(ProfileScanConfig profileScanConfig) {

        this.config = profileScanConfig;
    }

    @Override
    public Optional<Profile> parseFile(Path path) {
        try {
            log.debug("Parsing profile.");
            List<String> lines = Files
                .lines(path)
                .map(String::trim)
                .filter(this::isNotCommentLine)
                .collect(Collectors.toList());
            return Optional.of(
                Profile.builder()
                    .arguments(lines.stream()
                        .filter(this::isNotFieldLine)
                        .collect(Collectors.toList()))
                    .fields(lines.stream()
                        .filter(this::isFieldLine)
                        .collect(Collectors.toMap(this::extractKey, this::extractValue)))
                    .path(Profile.constructProfileURI(config.profile_base_dir().relativize(path)))
                    .build());
        } catch (IOException e) {
            MDC.put("error", e.getMessage());
            log.warn("Could not parse file.");
            return Optional.empty();
        } finally {
            MDC.remove("error");
        }
    }

    /**
     * Returns the key (first group) extracted from {@link #FORMAT_PATTERN}.
     *
     * @param t
     * @return the string with the key in upper case.
     */
    String extractKey(String t) {
        Matcher m = FORMAT_PATTERN.matcher(t);
        m.find();
        return m.group(1).toUpperCase(Locale.ENGLISH);
    }

    /**
     * Returns the value (second group) extracted from {@link #FORMAT_PATTERN}.
     *
     * @param t
     * @return the string with the value.
     */
    String extractValue(String t) {
        Matcher m = FORMAT_PATTERN.matcher(t);
        m.find();
        return m.group(2);
    }

    /**
     * Negates {@link #isFieldLine(String)}.
     */
    boolean isNotFieldLine(String s) {
        return !isFieldLine(s);
    }

    /**
     * Returns true if the given string begins with "%{", ends with "}" and has a "=" in between.
     *
     * @param s
     * @return
     */
    boolean isFieldLine(String s) {
        return FORMAT_PATTERN.matcher(s).find();
    }

    /**
     * Negates {@link #isCommentLine(String)}.
     */
    boolean isNotCommentLine(String s) {
        return !isCommentLine(s);
    }

    /**
     * Tests whether the given string is a comment. Comments are empty lines or lines starting with "#" at the
     * beginning.
     *
     * @param subject the line to test, not null.
     * @return true if the line is comment.
     * @throws NullPointerException if subject is null.
     */
    boolean isCommentLine(String subject) {
        return subject.startsWith("#") || "".equals(subject);
    }

}
