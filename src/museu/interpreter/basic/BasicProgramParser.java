package museu.interpreter.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BasicProgramParser {

    private static final Pattern NUMBERED_LINE = Pattern.compile("^\\s*(\\d{1,5})\\s*(.*)$");

    private final BasicTokenizer tokenizer;

    BasicProgramParser(BasicTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    BasicProgram parse(String source) {
        Map<Integer, List<Token>> tokensByLine = new TreeMap<>();
        for (String rawLine : source.split("\\R")) {
            if (rawLine.isBlank()) {
                continue;
            }
            Matcher matcher = NUMBERED_LINE.matcher(rawLine);
            if (!matcher.matches()) {
                throw new BasicException("LINHA SEM NUMERO: " + rawLine.strip());
            }
            int lineNumber = Integer.parseInt(matcher.group(1));
            tokensByLine.put(lineNumber, tokenizeLine(lineNumber, matcher.group(2)));
        }
        List<BasicLine> lines = new ArrayList<>();
        tokensByLine.forEach((number, tokens) -> lines.add(new BasicLine(number, tokens)));
        return new BasicProgram(lines);
    }

    private List<Token> tokenizeLine(int lineNumber, String content) {
        try {
            return tokenizer.tokenize(content);
        } catch (BasicException exception) {
            throw exception.atLine(lineNumber);
        }
    }
}
