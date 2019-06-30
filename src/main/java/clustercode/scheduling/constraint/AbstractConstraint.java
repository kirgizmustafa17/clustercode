package clustercode.scheduling.constraint;

import clustercode.api.domain.Media;
import clustercode.scheduling.Constraint;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Provides a template class for constraints.
 */
public abstract class AbstractConstraint implements Constraint {

    /**
     * Logs a debug message and returns the result unmodified. This method can be used before returning from
     * {@link #accept(Media)}. "Accepted media." or "Declined media. " will be logged with Debug level. Before returning,
     * the MDC is being cleared.
     *
     * @param accepted the result of {@link #accept(Media)}.
     * @return {@code accepted}
     */
    protected final boolean logAndReturnResult(boolean accepted) {
        if (accepted) {
            getLogger().debug("Accepted media.");
        } else {
            getLogger().debug("Declined media.");
        }
        return accepted;
    }

    protected abstract Logger getLogger();

}
