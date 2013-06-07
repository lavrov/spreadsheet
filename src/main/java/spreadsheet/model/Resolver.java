package spreadsheet.model;

import java.util.Map;

import fj.*;
import static fj.P.*;
import fj.data.*;
import spreadsheet.interpreter.Interpreter;

public class Resolver {
    final Map<CellIndex, Cell> map;

    public Resolver(Map<CellIndex, Cell> map) {
        this.map = map;
    }

    public Option<Map<CellIndex, Cell>> resolve() {
        for(Cell cell : map.values()) {
            if(new CellResolver().resolve(cell.content).isNone()) return Option.none();

        }
        return Option.some(map);
    }

    class CellResolver {

        final Set<CellIndex> fringe;

        public CellResolver() {
            this.fringe = Set.empty(Ord.<CellIndex>hashEqualsOrd());
        }

        public CellResolver(Set<CellIndex> fringe) {
            this.fringe = fringe;
        }


        public Option<Long> resolve(final CellContent cell) {
            if(cell.state instanceof CellContent.Unresolved) {
                Expression expression = ((CellContent.Unresolved)cell.state).expression;
                Option<Long> value = resolveExpression(expression);
                value.foreach(new Effect<Long>() {public void e(Long n) {
                    cell.state = new CellContent.Resolved(n);
                }});
                return value;
            } else if(cell.state instanceof CellContent.Resolved) {
                return Option.some(((CellContent.Resolved)cell.state).value);
            }
            return Option.none();
        }

        Long sumByModule(Long a, Long b) {
            return (a + b) % 18014398241046527L;
        }

        public Option<Long> resolveExpression(Expression exp) {
            if(exp instanceof Expression.Number)
                return Option.some(((Expression.Number)exp).number);
            else if(exp instanceof Expression.Ref)
                return resolveIndex(((Expression.Ref) exp).index);
            else if(exp instanceof Expression.BinaryOp) {
                Expression.BinaryOp binaryOp = (Expression.BinaryOp)exp;
                return resolveExpression(binaryOp.left).bindProduct(
                        resolveExpression(binaryOp.right)
                ).map(new F<P2<Long, Long>, Long>() {
                    public Long f(P2<Long, Long> tuple) {
                        return sumByModule(tuple._1(), tuple._2());
                    }
                });
            } else if(exp instanceof Expression.FunctionApplication) {
                return resolveRange(((Expression.FunctionApplication)exp).range).map(new F<List<Long>, Long>() {
                    public Long f(List<Long> integers) {
                        return integers.foldLeft(new F2<Long, Long, Long>() {
                            public Long f(Long integer, Long integer2) {
                                return sumByModule(integer, integer2);
                            }
                        }, 0L);
                    }
                });
            }

            return Option.none();
        }

        P2<Integer, Integer> minMax(Integer a, Integer b) {
            return p(Math.min(a, b), Math.max(a, b));
        }

        Option<List<Long>> resolveRange(Expression.Range range) {
            final P2<Integer, Integer> rowRange = minMax(range.from.row, range.to.row);
            final P2<Integer, Integer> columnRange = minMax(range.from.column, range.to.column);
            return  Option.sequence(
                    List.range(rowRange._1(), rowRange._2() + 1).bind(new F<Integer, List<Option<Long>>>() {
                        public List<Option<Long>> f(final Integer row) {
                            return List.range(columnRange._1(), columnRange._2() + 1).map(new F<Integer, Option<Long>>() {
                                public Option<Long> f(Integer column) {
                                    return resolveIndex(new CellIndex(row, column));
                                }
                            });
                        }
                    })
            );
        }

        public Option<Long> resolveIndex(final CellIndex index) {
            if(fringe.member(index))
                return Option.none();
            else {
                Option<Cell> cellFromMap = Option.fromNull(map.get(index));
                if(cellFromMap.isNone())
                    return Option.some(0L);
                else
                    return cellFromMap.bind(new F<Cell, Option<Long>>() {
                        public Option<Long> f(Cell cell) {
                            return new CellResolver(fringe.insert(index)).resolve(cell.content);
                        }
                    });
            }
        }
    }

    public static F<Map<CellIndex, Cell>, Option<Map<CellIndex, Cell>>> instance() {
        return
                new F<Map<CellIndex, Cell>, Option<Map<CellIndex, Cell>>>() {
                    public Option<Map<CellIndex, Cell>> f(Map<CellIndex, Cell> map) {
                        return new Resolver(map).resolve();
                    }
                };
    }
}