package clustercode.impl.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Optional;
import java.util.jar.Manifest;

@Slf4j
public class PackagingUtil {

    private PackagingUtil() {

    }

    public static Optional<String> getManifestAttribute(String key) {
        try (var stream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF")) {
            return Optional.ofNullable(
                new Manifest(stream)
                    .getMainAttributes()
                    .getValue(key));
        } catch (IOException | NullPointerException e) {
            MDC.put("error", e.getMessage());
            MDC.put("key", key);
            log.warn("Cannot retrieve manifest attribute.");
        } finally {
            MDC.remove("error");
            MDC.remove("key");
        }
        return Optional.empty();
    }

}
