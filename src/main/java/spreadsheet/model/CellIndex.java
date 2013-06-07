package spreadsheet.model;

public final class CellIndex {
    public final int row;
    public final int column;
    public CellIndex(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public int hashCode() {
        return row + column;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CellIndex) {
            CellIndex that = (CellIndex)obj;
            return this.row == that.row && this.column == that.column;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CellIndex("+row+","+column+")";
    }
}
