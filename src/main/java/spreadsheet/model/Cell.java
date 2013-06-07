package spreadsheet.model;

public class Cell implements Comparable<Cell> {
    public final CellIndex index;
    public final CellContent content;

    public Cell(CellIndex index, Expression expression) {
        this.index = index;
        this.content = new CellContent(expression);
    }

    public String toString() {
        return "Cell(" + index.toString() + " " + content.toString() + ")";
    }

    public int compareTo(Cell that) {
        int comparedRow = Integer.compare(this.index.row, that.index.row);
        return comparedRow != 0 ? comparedRow : Integer.compare(this.index.column, that.index.column);
    }
}
