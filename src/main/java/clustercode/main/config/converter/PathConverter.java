package clustercode.main.config.converter;

import clustercode.impl.util.FilesystemProvider;
import io.vertx.core.cli.converters.Converter;

import java.nio.file.Path;

public class PathConverter implements Converter<Path> {

    @Override
    public Path fromString(String s) {
        return FilesystemProvider.getInstance().getPath(s);
    }

}
