package eapli.aisafe.dsl.parse;

/**
 * Line/column in the source file (ANTLR: line 1-based, column 0-based).
 */
public record SourceSpan(int line, int column) {

    public String prefix() {
        return "Line " + line + ":" + column + " - ";
    }
}
