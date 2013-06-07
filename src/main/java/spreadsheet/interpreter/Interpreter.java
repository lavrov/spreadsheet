package spreadsheet.interpreter;

import fj.data.Option;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.*;
import spreadsheet.model.Cell;
import spreadsheet.model.CellIndex;
import spreadsheet.model.Expression;

public class Interpreter {

    public static final Terminals COLON = Terminals.operators("|");

    public static final Parser<String> INTEGER = Terminals.IntegerLiteral.PARSER;

    public static final Parser<Expression> NUMBER = Terminals.IntegerLiteral.PARSER.map(new Map<String, Expression>() {
        public Expression map(String i) {
            return new Expression.Number(Long.valueOf(i));
        }
    });

    static final Terminals OPERATORS = Terminals.operators("+", "(", ")", ":");

    static final Parser<Void> IGNORED = Scanners.WHITESPACES.skipMany();

    static final Parser<?> TOKENIZER =
            Parsers.or(Terminals.IntegerLiteral.TOKENIZER, OPERATORS.tokenizer(), COLON.tokenizer(), Terminals.Identifier.TOKENIZER);

    static Parser<?> term(String... names) {
        return OPERATORS.token(names);
    }

    static <T> Parser<T> op(String name, T value) {
        return term(name).retn(value);
    }


    static enum BinaryOperator implements Binary<Expression> {
        PLUS {
            public Expression map(Expression a, Expression b) {
                return new Expression.BinaryOp("+", a, b);
            }
        }
    }

    static final Parser<Expression.Ref> CellIndex = Parsers.sequence(INTEGER, COLON.token("|"), INTEGER, new Map3<String, Object, String, Expression.Ref>() {
        public Expression.Ref map(String row, Object o, String column) {
            return new Expression.Ref(new CellIndex(Integer.valueOf(row), Integer.valueOf(column)));
        }
    });

    static final Parser<Expression> FunctionApplication =
            Parsers.sequence(
                Terminals.Identifier.PARSER,
                Parsers.sequence(CellIndex, term(":"), CellIndex, new Map3<Expression.Ref, Object, Expression.Ref, Pair<CellIndex, CellIndex>>() {
                    public Pair<CellIndex, CellIndex> map(Expression.Ref from, Object o, Expression.Ref to) {
                        return new Pair<>(from.index, to.index);
                    }
                }).between(term("("), term(")")),
                    new Map2<String, Pair<CellIndex, CellIndex>, Expression>() {
                        public Expression map(String s, Pair<CellIndex, CellIndex> pair) {
                            return new Expression.FunctionApplication(new Expression.Range(pair.a, pair.b));
                        }
                    }
            );

    static final Parser<Expression> atomParser = Parsers.or(CellIndex, FunctionApplication, NUMBER);

    static Parser<Expression> calculator(Parser<Expression> atom) {
        Parser.Reference<Expression> ref = Parser.newReference();
        Parser<Expression> unit = ref.lazy().between(term("("), term(")")).or(atom);
        Parser<Expression> parser = new OperatorTable<Expression>()
                .infixl(op("+", BinaryOperator.PLUS), 10)
                .build(unit);
        ref.set(parser);
        return parser;
    }

    static final Parser<Cell> parser = Parsers.sequence(CellIndex, calculator(atomParser), new Map2<Expression.Ref, Expression, Cell>() {
        public Cell map(Expression.Ref ref, Expression expression) { return new Cell(ref.index, expression); }
    }).from(TOKENIZER, IGNORED);

    public static Option<Cell> interpret(String in) {
        try {
            return Option.some(parser.parse(in));
        } catch(ParserException e) {
            return Option.none();
        }
    }
}