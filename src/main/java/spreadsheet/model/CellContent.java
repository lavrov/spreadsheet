package spreadsheet.model;

public class CellContent {
    public volatile State state;

    public CellContent(Expression expression) {
        this.state = new Unresolved(expression);
    }

    @Override
    public String toString() {
        return state.toString();
    }

    interface State {}

    static class Unresolved implements State {
        public final Expression expression;

        public Unresolved(Expression expression) {
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "Unresolved(" + expression + ")";
        }
    }

    public static class Resolved implements State {
        public final long value;

        public Resolved(long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Resolved(" + value + ")";
        }
    }
}
