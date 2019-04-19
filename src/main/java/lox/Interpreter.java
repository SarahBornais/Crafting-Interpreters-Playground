package lox;

class Interpreter extends Expression.Visitor<Object> {

    // Returns a runtime value from a literal
    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    // Recursively evaluates the expression inside a grouping
    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = evaluate(expression.right);

        switch(expression.operator.type) {
            case MINUS: return -(double)right;
            case BANG: return !isTruthy(right);
        }

        // Unreachable, but Java doesn't trust us and so it gets mad if we don't put this here
        return null;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch(expression.operator.type) {
            case MINUS: return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
            case SLASH: return (double)left / (double)right;
            case STAR: return (double)left * (double)right;
            case GREATER: return (double)left > (double)right;
            case GREATER_EQUAL: return (double)left >= (double)right;
            case LESS: return (double)left < (double)right;
            case LESS_EQUAL: return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);

        }
    }

    // Sends a given expression back to interpreter's visitor implementation
    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    // Evaluates "truthiness" of an object
    // In Lox, nil and false are false, and EVERYTHING ELSE is true
    private Boolean isTruthy(Object object) {
        if (object ==  null) {
            return false;
        }

        if (object instanceof Boolean) {
            return (Boolean)object;
        }

        return true;
    }

    private Boolean isEqual(Object a, Object b) {
        if(a == null  && b == null) {
            return true;
        }

        if(a == null) {
            return false;
        }

        return a.equals(b);
    }
}
