package bg.sofia.uni.fmi.mjt.dungeons.exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Date;

public final class ExceptionHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "resources" + File.separator + "exception_log.txt";

    public void handleException(Exception exception, String causedBy) {
        handleException(exception, causedBy, FILE_NAME);
    }

    public void handleException(Exception exception, String causedBy, String fileName) {
        String stackTrace = getExceptionInformationAsJson(exception, causedBy);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, true))) {
            writer.println(stackTrace);
        } catch (IOException e) {
            throw new UncheckedIOException("Something went wrong when writing exception to file", e);
        }
    }

    private static String getExceptionInformationAsJson(Exception exception, String causedBy) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        ExceptionInfo exceptionInfo = new ExceptionInfo(
            new Date(),
            exception.getClass().getName(),
            exception.getMessage(),
            causedBy,
            stackTrace
        );

        return GSON.toJson(exceptionInfo);
    }

    private static class ExceptionInfo {
        @SuppressWarnings("unused")
        private Date timestamp;
        @SuppressWarnings("unused")
        private String exceptionType;
        @SuppressWarnings("unused")
        private String message;
        @SuppressWarnings("unused")
        private String causedBy;
        @SuppressWarnings("unused")
        private String stackTrace;

        public ExceptionInfo(
            Date timestamp,
            String exceptionType,
            String message,
            String causedBy,
            String stackTrace
        ) {
            this.timestamp = timestamp;
            this.exceptionType = exceptionType;
            this.message = message;
            this.causedBy = causedBy;
            this.stackTrace = stackTrace;
        }
    }
}
