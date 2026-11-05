package museu.interpreter.basic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class BasicProgram {

    private final List<BasicLine> lines;
    private final Map<Integer, Integer> indexByLineNumber = new HashMap<>();

    BasicProgram(List<BasicLine> lines) {
        this.lines = List.copyOf(lines);
        for (int index = 0; index < lines.size(); index++) {
            indexByLineNumber.put(lines.get(index).number(), index);
        }
    }

    int size() {
        return lines.size();
    }

    BasicLine lineAt(int index) {
        return lines.get(index);
    }

    int indexOf(int lineNumber) {
        Integer index = indexByLineNumber.get(lineNumber);
        if (index == null) {
            throw new BasicException("LINHA " + lineNumber + " NAO EXISTE");
        }
        return index;
    }
}
