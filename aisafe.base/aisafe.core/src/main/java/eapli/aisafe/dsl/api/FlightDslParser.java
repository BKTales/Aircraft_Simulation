package eapli.aisafe.dsl.api;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import eapli.aisafe.dsl.FlightPlanLexer;
import eapli.aisafe.dsl.FlightPlanParser;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.parse.FlightPlanBuilder;
import eapli.aisafe.dsl.parse.FlightPlanTreeShapeListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FlightDslParser {

    /**
     * Reads a UTF-8 text file (any extension) and parses it as a Core Flight DSL document.
     */
    public ParseResult parse(final Path path) {
        try {
            if (!Files.exists(path)) {
                return ParseResult.failure(List.of("File not found: " + path.toAbsolutePath()));
            }
            if (!Files.isRegularFile(path)) {
                return ParseResult.failure(List.of("Not a regular file: " + path.toAbsolutePath()));
            }
            final String content = Files.readString(path, StandardCharsets.UTF_8);
            return parse(content);
        } catch (final NoSuchFileException e) {
            return ParseResult.failure(List.of("File not found: " + path.toAbsolutePath()));
        } catch (final IOException e) {
            return ParseResult.failure(List.of("Could not read file: " + path.toAbsolutePath()
                    + " — " + e.getMessage()));
        }
    }

    public ParseResult parse(final String input) {
        final CharStream stream = CharStreams.fromString(input);
        final FlightPlanErrorListener errorListener = new FlightPlanErrorListener();

        final FlightPlanLexer lexer = new FlightPlanLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final FlightPlanParser parser = new FlightPlanParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        final ParseTree tree = parser.flightPlan();

        if (errorListener.hasErrors()) {
            return ParseResult.failure(errorListener.getErrors());
        }

        final FlightPlanBuilder builder = new FlightPlanBuilder();
        final FlightPlanDescriptor descriptor = (FlightPlanDescriptor) builder.visit(tree);

        final FlightPlanTreeShapeListener shape = FlightPlanTreeShapeListener.inspect(tree);
        if (!shape.matchesDescriptor(descriptor)) {
            return ParseResult.failure(List.of("Internal error: parse tree and descriptor mismatch."));
        }

        final List<String> semanticErrors = descriptor.validateSemantics(builder.sourceIndex());
        if (!semanticErrors.isEmpty()) {
            return ParseResult.failure(semanticErrors);
        }

        return ParseResult.success(descriptor);
    }

    /**
     * Colocated with {@link FlightDslParser} so IDE incremental builds cannot leave a broken
     * {@code .class} when the listener type is missing from the compile classpath.
     */
    public static final class FlightPlanErrorListener extends BaseErrorListener {

        private final List<String> errors = new ArrayList<>();

        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer,
                                final Object offendingSymbol,
                                final int line,
                                final int charPositionInLine,
                                final String msg,
                                final RecognitionException e) {
            final String token = offendingSymbol instanceof Token
                    ? ((Token) offendingSymbol).getText()
                    : "<unknown>";

            final String friendly;
            if (e instanceof NoViableAltException) {
                friendly = String.format(
                        "Line %d:%d - Token inesperado '%s'. Verifique a estrutura do flight plan.",
                        line, charPositionInLine, token);
            } else if (e instanceof InputMismatchException) {
                friendly = String.format(
                        "Line %d:%d - Input inesperado '%s'. Verifique a sintaxe perto deste ponto.",
                        line, charPositionInLine, token);
            } else {
                friendly = String.format(
                        "Line %d:%d - Erro lexico/sintatico perto de '%s': %s",
                        line, charPositionInLine, token, msg);
            }
            errors.add(friendly);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public List<String> getErrors() {
            return List.copyOf(errors);
        }
    }
}
