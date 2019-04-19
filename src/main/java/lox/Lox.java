package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    public static boolean hadError = false;

    public static void main(String[] args) throws IOException{
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); //using the conventions defined in the UNIX “sysexits.h” header
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }

    // Gets lox source file from a given path and executes it
    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
    }

    // Prompts user to write source code and executes it as it's written
    public static void runPrompt() throws IOException {
        // Sets up the reader to read the user input
        InputStreamReader input  = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // Prompts user for a new line of code and executes it, FOREVER (or until user hits control-C)
        for(;;) {
            System.out.println("> ");
            run(reader.readLine());
            // Avoids killing the user's entire session because they made a typo
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        if (hadError) {
            return;
        }

        System.out.println(new ASTPrinter().print(expression));
    }

    // Non-private method to give access to error reporting method, "report"
    static void error(int line, String message) {
        report(line, "", message);
    }

    // Reports syntax errors and their location to the user, and remembers that there was an error
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error"+ where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
