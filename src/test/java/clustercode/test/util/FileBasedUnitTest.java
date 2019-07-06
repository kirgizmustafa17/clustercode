package clustercode.test.util;

import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Represents a test utility where unit testing using files is needed. Any path created using {@link #getPath(String,
 * String...)} is placed in-memory. The default backend of the file system is Google's Jimfs.
 */
public class FileBasedUnitTest implements TestInstancePostProcessor {

    /**
     * The variable used by {@link FileBasedUnitTest} to access the (existing) file system.
     */
    private FileSystem fs = Jimfs.newFileSystem();

    public FileSystem getFileSystem() {
        return fs;
    }

    /**
     * Gets the path according to {@link FileSystem#getPath(String, String...)}.
     *
     * @param first
     * @param more
     * @return the path.
     * @throws RuntimeException if {@link #fs} is not initialized.
     */
    public Path getPath(String first, String... more) {
        return fs.getPath(first, more);
    }

    /**
     * Creates the file and returns the path. By default any parent directories will be created first.
     *
     * @param path the desired path of the file.
     * @return path
     * @throws RuntimeException with the original IOException as cause if it failed.
     */
    public Path createFile(Path path) {
        try {
            Files.createFile(createParentDirOf(path));
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recursively creates the directories of the given path.
     *
     * @param path
     * @return path
     * @throws RuntimeException with the original IOException as cause if it failed.
     */
    public Path createDirectory(Path path) {
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the parent directory of the given path.
     *
     * @param path
     * @return path
     * @throws RuntimeException with the original IOException as cause if it failed.
     */
    public Path createParentDirOf(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        Arrays.stream(testInstance.getClass().getDeclaredFields())
              .filter(field -> field.getType() == getClass())
              .forEach(field -> injectInstance(testInstance, field));
    }

    private void injectInstance(Object testInstance, Field field) {
        field.setAccessible(true);
        try {
            field.set(testInstance, this);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
