package clustercode.scheduling.constraint;

import clustercode.api.domain.Media;
import clustercode.impl.util.InvalidConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Provides an implementation of a time constraint. The media will not be accepted for scheduling when the current
 * time is outside of the configurable 24h time window. The 'begin' and 'stop' strings are expected in the "HH:mm"
 * format (0-23).
 */
@Slf4j
public class TimeConstraint extends AbstractConstraint {

    private final Clock clock;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private LocalTime stop;
    private LocalTime begin;

    protected TimeConstraint(String timeBegin,
                             String timeStop,
                             Clock clock) {
        this.clock = clock;
        try {
            this.begin = LocalTime.parse(timeBegin, formatter);
            this.stop = LocalTime.parse(timeStop, formatter);
        } catch (DateTimeParseException ex) {
            throw new InvalidConfigurationException("The time format is HH:mm. You specified: begin({}), stop({})", ex,
                timeBegin, timeStop);
        }
        checkConfiguration();
    }

    private void checkConfiguration() {
        if (begin.compareTo(stop) == 0) {
            throw new InvalidConfigurationException("Begin and stop time are identical (specify different times in " +
                "HH:mm format).");
        }
    }

    @Override
    public boolean accept(Media candidate) {
        LocalTime now = LocalTime.now(clock);
        if (begin.isBefore(stop)) {
            return logAndReturn(begin.isBefore(now) && now.isBefore(stop), now);
        } else {
            return logAndReturn((
                now.isAfter(begin) && now.isAfter(stop)) || (now.isBefore(stop) && now.isBefore(begin)), now);
        }
    }

    protected boolean logAndReturn(boolean result, LocalTime now) {
        MDC.put("begin", formatter.format(begin));
        MDC.put("stop", formatter.format(stop));
        MDC.put("now", formatter.format(now));
        return logAndReturnResult(result);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
