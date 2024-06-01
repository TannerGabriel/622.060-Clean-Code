package at.aau.utils;

import java.util.ArrayList;

public class Logger {
    private static Logger logInstance;
    private final ArrayList<String> logs = new ArrayList<>();


    private Logger() {
    }

    public static Logger getInstance() {
        if (logInstance == null) {
            synchronized (Logger.class) {
                logInstance = new Logger();
            }
        }
        return logInstance;
    }

    public String getLogsString() {
        String errorHeadline = "# ERRORS <br>\n";

        synchronized (logs) {
            StringBuilder logOutput = new StringBuilder();
            logOutput.append(errorHeadline);

            for (String logEntry : logs) {
                logOutput.append("- ").append(logEntry).append("<br>\n");
            }

            return logOutput.toString();
        }
    }

    public void logError(String errorMessage) {
        logs.add(errorMessage);
    }

    public ArrayList<String> getLogs() {
        return logs;
    }
}
