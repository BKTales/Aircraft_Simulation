package eapli.aisafe.dsl.api;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Console report for DSL parse failures: source line + caret at column.
 */
public final class FlightPlanParseErrorReporter {

    private static final Pattern LOCATION =
            Pattern.compile("^Line (\\d+):(\\d+)\\s*-\\s*(.+)$");
    private static final int MAX_LINE_CHARS = 200;

    private FlightPlanParseErrorReporter() {}

    public static void print(final PrintStream out, final Path file, final List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            out.println("\nFlight plan is invalid (no details).");
            return;
        }
        try {
            final String content = Files.readString(file, StandardCharsets.UTF_8);
            out.println();
            out.println("File: " + file.toAbsolutePath());
            print(out, content, errors);
        } catch (final IOException e) {
            out.println();
            out.println("File: " + file.toAbsolutePath());
            out.println("(could not read file content to show context)");
            printWithoutSource(out, errors);
        }
    }

    public static void print(final PrintStream out, final String sourceContent, final List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            out.println("\nFlight plan is invalid (no details).");
            return;
        }

        final List<String> sourceLines = splitLines(sourceContent);
        final List<String> located = new ArrayList<>();
        final List<String> other = new ArrayList<>();

        for (final String error : errors) {
            if (LOCATION.matcher(error.trim()).matches()) {
                located.add(error.trim());
            } else {
                other.add(error);
            }
        }

        out.println();
        out.println("=".repeat(72));
        out.println("  INVALID FLIGHT PLAN — " + errors.size() + " issue(s)");
        out.println("=".repeat(72));

        int index = 0;
        for (final String error : located) {
            index++;
            printLocatedError(out, sourceLines, error, index, located.size());
        }

        if (!other.isEmpty()) {
            out.println();
            out.println("-".repeat(72));
            out.println("  Other issues (no line/column in file)");
            out.println("-".repeat(72));
            for (final String error : other) {
                out.println("  * " + error);
            }
        }

        out.println();
        out.println("=".repeat(72));
    }

    private static void printLocatedError(
            final PrintStream out,
            final List<String> sourceLines,
            final String error,
            final int index,
            final int total) {
        final Matcher matcher = LOCATION.matcher(error);
        if (!matcher.matches()) {
            return;
        }

        final int line = Integer.parseInt(matcher.group(1));
        final int column = Integer.parseInt(matcher.group(2));
        final String message = matcher.group(3);

        out.println();
        out.println("--- Error " + index + " of " + total + " ---");
        out.println("  Where:  line " + line + ", column " + column + " (0-based column index)");
        out.println("  What:   " + message);
        out.println();

        if (line < 1 || line > sourceLines.size()) {
            out.println("  (line " + line + " is outside the file — " + sourceLines.size() + " lines)");
            return;
        }

        final String sourceLine = sourceLines.get(line - 1);
        final String displayLine = truncate(sourceLine);
        final int caretColumn = Math.min(column, displayLine.length());
        final String lineNo = String.format("%4d", line);

        out.println("  " + lineNo + " | " + displayLine);
        out.print("       | ");
        out.print(" ".repeat(caretColumn));
        out.println("^ here");
    }

    private static void printWithoutSource(final PrintStream out, final List<String> errors) {
        out.println();
        for (int i = 0; i < errors.size(); i++) {
            out.println("  " + (i + 1) + ") " + errors.get(i));
        }
    }

    private static List<String> splitLines(final String content) {
        if (content == null || content.isEmpty()) {
            return List.of("");
        }
        final String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        return List.of(normalized.split("\n", -1));
    }

    private static String truncate(final String line) {
        if (line == null) {
            return "";
        }
        if (line.length() <= MAX_LINE_CHARS) {
            return line;
        }
        return line.substring(0, MAX_LINE_CHARS) + "…";
    }
}
