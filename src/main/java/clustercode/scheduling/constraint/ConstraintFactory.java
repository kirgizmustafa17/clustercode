package clustercode.scheduling.constraint;

import clustercode.main.config.Configuration;
import clustercode.scheduling.Constraint;
import io.vertx.core.json.JsonObject;

import java.nio.file.Paths;
import java.time.Clock;

public class ConstraintFactory {

    private final JsonObject config;

    ConstraintFactory(JsonObject config) {
        this.config = config;
    }

    public Constraint createFromEnum(Constraints constraint) {
        AbstractConstraint c;
        switch (constraint) {
            case FILE_NAME:
                c = new FileNameConstraint(
                        config.getString(Configuration.constraint_file_regex.key())
                );
                break;
            case FILE_SIZE:
                c = new FileSizeConstraint(
                        Paths.get(config.getString(Configuration.input_dir.key())),
                        config.getDouble(Configuration.constraint_file_size_min.key()),
                        config.getDouble(Configuration.constraint_file_size_max.key())
                );
                break;
            case TIME:
                c = new TimeConstraint(
                        config.getString(Configuration.constraint_time_begin.key()),
                        config.getString(Configuration.constraint_time_stop.key()),
                        Clock.systemDefaultZone()
                );
                break;
            case NONE:
                c = new NoConstraint();
                break;
            default:
                throw new EnumConstantNotPresentException(constraint.getClass(), constraint.name());
        }
        c.getLogger().info("Enabled constraint.");
        return c;
    }

}
