package museu.interpreter.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

final class BasicValues {

    static final double TRUE_VALUE = -1;
    static final double FALSE_VALUE = 0;
    private static final int MAX_STRING_LENGTH = 1_000;
    private static final double MAX_EXACT_INTEGER = 1e15;
    private static final DecimalFormatSymbols NUMBER_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);

    private BasicValues() {
    }

    static boolean isStringVariable(String name) {
        return name.endsWith("$");
    }

    static Object defaultValueFor(String variableName) {
        return isStringVariable(variableName) ? "" : 0.0;
    }

    static void requireCompatible(String variableName, Object value) {
        if (isStringVariable(variableName) != (value instanceof String)) {
            throw new BasicException("TIPOS INCOMPATIVEIS EM " + variableName);
        }
    }

    static double asNumber(Object value) {
        if (value instanceof Double number) {
            return number;
        }
        throw new BasicException("TIPOS INCOMPATIVEIS: ESPERAVA NUMERO");
    }

    static String asString(Object value) {
        if (value instanceof String text) {
            return text;
        }
        throw new BasicException("TIPOS INCOMPATIVEIS: ESPERAVA TEXTO");
    }

    static boolean isTrue(Object value) {
        return asNumber(value) != 0;
    }

    static double fromBoolean(boolean condition) {
        return condition ? TRUE_VALUE : FALSE_VALUE;
    }

    static String requireMaxLength(String text) {
        if (text.length() > MAX_STRING_LENGTH) {
            throw new BasicException("TEXTO LONGO DEMAIS");
        }
        return text;
    }

    static double requireFinite(double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            throw new BasicException("ESTOURO NUMERICO");
        }
        return number;
    }

    static String format(Object value) {
        if (value instanceof String text) {
            return text;
        }
        double number = asNumber(value);
        if (number == Math.rint(number) && Math.abs(number) < MAX_EXACT_INTEGER) {
            return Long.toString((long) number);
        }
        return new DecimalFormat("0.######", NUMBER_SYMBOLS).format(number);
    }
}
