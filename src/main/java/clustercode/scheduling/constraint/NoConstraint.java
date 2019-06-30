package clustercode.scheduling.constraint;

import clustercode.api.domain.Media;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class NoConstraint extends AbstractConstraint {

    @Override
    public boolean accept(Media candidate) {
        return logAndReturnResult(true);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
