package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    // Scans the entire source code for tokens, one character at a time, and returns those tokens
    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    // For every character(s) scanned, determines it's corresponding token
    private void scanToken() {
        char c = advance();
        switch (c) {
            // 1-character-long tokens
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            // 2-character-long tokens
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '/':
                // Skips over comments if the slash is part of a double slash
                if(match('/')) {
                    while(peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }
                // Otherwise, treats the slash as a division symbol
                else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Ignore whitespace, tabs, etc.
            case ' ': case '\r': case '\t': break;

            case '\n':
                line++;
                break;

            case '"' : string(); break;

            default:
                if(isDigit(c)) {
                    number();
                }
                else if(isAlpha(c)) {
                    identifier();
                }
                // Returns an error if the scanned character does not have an associated token
                else {
                    Lox.error(line, "Unexpected character");
                    break;
                }
        }
    }

    // Checks to see if the scanner has reached the end of the source code
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Returns the character at the current index, and then advances the index
    private char advance() {
        char c = source.charAt(current);
        current++;
        return c;
    }

    // Avoids needing to write null in every call to addToken with a non-literal token
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Adds the recognized token to the list of tokens
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // Checks to see if the character after the one that was just scanned is what one would expect
    // for one of the 2-character-long tokens
    private boolean match(char expected) {
        if(isAtEnd() || source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    // Looks ahead at the next character to be scanned without advancing
    private char peek() {
        if(isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    // Look ahead at the character after the next to be scanned without advancing
    private char peekNext() {
        if(current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    // Scans an entire string as a token
    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                line++;
            }
            advance();
        }

        // Throws an error if the string wasn't terminated
        if(isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // Reads the last '"' if the string WAS terminated properly
        advance();

        // Gets the literal value of the string by trimming the quotation marks
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    // Returns whether or not a given character is a digit
    private boolean isDigit(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }

    // Scans an entire number as a token
    private void number() {
        while(isDigit(peek())) {
            advance();
        }

        // Handles a decimal point
        if(peek() == '.' && isDigit(peekNext())) {
            advance();

            while(isDigit(peek())) {
                advance();
            }
        }

        Double value = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, value);

    }

    // Scans an entire identifier as a token and checks if it's a keyword
    private void identifier() {
        while(isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

}
