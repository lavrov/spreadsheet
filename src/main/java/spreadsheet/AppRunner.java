package spreadsheet;

import fj.data.*;
import spreadsheet.model.Resolver;

public class AppRunner {
    public static void main(String[] args) {
        String fileName = Array.array(args).toOption().orSome("input.txt");
        IO.loadFromFile(fileName).bind(Resolver.instance()).map(IO.printResult()).orSome(IO.printError());
    }
}

