package museu.interpreter.basic;

public final class BasicException extends RuntimeException {

    private final Integer lineNumber;

    public BasicException(String message) {
        this(message, null);
    }

    private BasicException(String message, Integer lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public BasicException atLine(int number) {
        return lineNumber == null ? new BasicException(getMessage(), number) : this;
    }

    public String formattedMessage() {
        return "?" + getMessage() + (lineNumber == null ? "" : " NA LINHA " + lineNumber);
    }
}
