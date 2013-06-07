package spreadsheet;

import fj.*;
import fj.data.*;
import fj.data.List;
import spreadsheet.interpreter.Interpreter;
import spreadsheet.model.Cell;

import java.io.*;
import java.util.*;
import java.util.HashMap;

import spreadsheet.model.*;

public class IO {
    public static Option<Map<CellIndex, Cell>> loadFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            final Map<CellIndex, Cell> map = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.trim().isEmpty()) continue;

                Option<Cell> cell = Interpreter.interpret(line);

                if(cell.isNone()) return Option.none();

                cell.foreach(new Effect<Cell>() {
                    public void e(Cell cell) {
                        map.put(cell.index, cell);
                    }
                });
            }
            return Option.some(map);
        } catch (FileNotFoundException e) {
            return Option.none();
        } catch (IOException e) {
            return Option.none();
        }
    }

    public static Option<Map<CellIndex, Cell>> loadFromFile(String fileName) {
        return loadFromFile(new File(fileName));
    }

    public static F<Map<CellIndex, Cell>, Unit> printResult(){
        return
            new F<Map<CellIndex, Cell>, Unit>(){
                public Unit f(Map<CellIndex, Cell> map) {
                    print(map);
                    return Unit.unit();
                }
            };
    }

    public static P1<Unit> printError() {
        return new P1<Unit>() {
            public Unit _1() {
                System.out.println("error");
                return Unit.unit();
            }
        };
    }

    public static void print(Map<CellIndex, Cell> map) {
        java.util.List<Cell> list = new ArrayList<Cell>(map.values());
        Collections.sort(list, new Comparator<Cell>(){
            public int compare(Cell o, Cell o2) {
                return o.compareTo(o2);
            }
        });

        for(Cell cell : list) {
            System.out.println(cell.index.row + "|" + cell.index.column + " " + ((CellContent.Resolved) cell.content.state).value);
        }
    }
}
