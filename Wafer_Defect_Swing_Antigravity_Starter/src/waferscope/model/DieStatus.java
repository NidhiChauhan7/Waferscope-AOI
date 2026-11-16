package waferscope.model;

/**
 * Represents the ternary status of an individual die location on a semiconductor wafer.
 * Conforms to the standard WM-811K ternary encoding:
 * - OUTSIDE (-1 / 'E'): Beyond wafer perimeter (empty substrate / edge exclusion)
 * - GOOD    ( 0 / '0'): Functionally passing die
 * - DEFECTIVE (1 / '1'): Functionally failing die
 */
public enum DieStatus {
    OUTSIDE(-1, 'E', "Outside Wafer"),
    GOOD(0, '0', "Good Die (Pass)"),
    DEFECTIVE(1, '1', "Defective Die (Fail)");

    private final int code;
    private final char symbol;
    private final String description;

    DieStatus(int code, char symbol, String description) {
        this.code = code;
        this.symbol = symbol;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public static DieStatus fromCode(int code) {
        if (code < 0) return OUTSIDE;
        if (code == 1) return DEFECTIVE;
        return GOOD;
    }

    public static DieStatus fromChar(char ch) {
        if (ch == 'E' || ch == 'e' || ch == '2') return OUTSIDE;
        if (ch == '1') return DEFECTIVE;
        return GOOD;
    }
}

