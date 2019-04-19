package lox;

import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expression parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    // Recursive tree descent to create the abstract syntax tree...
    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expression = comparison();

        // Checks if there is an equality to work with repeatedly until there are no more equalities
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            // Stores the type of equality
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = addition();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = addition();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression addition() {
        Expression expression = multiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = multiplication();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression multiplication() {
        Expression expression = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if(match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return primary();
    }

    private Expression primary() {
        if(match(TokenType.FALSE)) {
            return new Expression.Literal(false);
        }
        else if(match(TokenType.TRUE)) {
            return new Expression.Literal(true);
        }
        else if(match(TokenType.NIL)) {
            return new Expression.Literal(null);
        }
        else if(match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal);
        }
        else if(match(TokenType.LEFT_PAREN)) {
            Expression expression = expression();
            // Makes sure there are both left and right parenthesizes, otherwise gives an error
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expected expression.");
    }

    // Checks if the next token is of one of the types passed
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // Given a token type, returns whether or not the next token is of that type
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    // While not at the end of the token list, increments the index that is looked at
    private Token advance() {
        if(!isAtEnd()) {
            current++;
        }

        return previous();
    }

    // Checks whether or not the end of the token list has been reached
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    // Returns the current token
    private Token peek() {
        return tokens.get(current);
    }

    // Returns the token before the current token
    private Token previous() {
        return tokens.get(current - 1);
    }


    private Token consume(TokenType type, String message) {
        if (check(type)) {
           return advance();
        }

        throw error(peek(), message);
    }

    // Gives an error message by calling Lox.error, and returns a ParseError to be caught in parse()
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // When the parser encounters an error, discards tokens until the parser reaches the next statement to get back on track
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
