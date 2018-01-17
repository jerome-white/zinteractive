package util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

public class LogAgent {
    public static final Logger LOGGER =
        Logger.getLogger(LogAgent.class.getName());

    public static final void setLevel(Level level) {
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }

        Handler handler = new ConsoleHandler();
        handler.setLevel(level);

	LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
	LOGGER.setLevel(level);
    }
}
