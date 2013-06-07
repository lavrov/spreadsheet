package spreadsheet.model;

public interface Expression {
    static class Number implements Expression {
        final long number;
        public Number(long number) {
            this.number = number;
        }
    }

    static class Ref implements Expression {
        public final CellIndex index;
        public Ref(CellIndex index) {
            this.index = index;
        }
    }

    static class BinaryOp implements Expression {
        final String operator;
        final Expression left;
        final Expression right;
        public BinaryOp(String operator, Expression left, Expression right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }
    }

    static class Range {
        final CellIndex from;
        final CellIndex to;
        public Range(CellIndex from, CellIndex to) {
            this.from = from;
            this.to = to;
        }
    }

    static class FunctionApplication implements Expression {
        final Range range;
        public FunctionApplication(Range range) {
            this.range = range;
        }
    }
}
