package sqltoregex.property;

/**
 * Enum representing all possible PropertyOptions.
 */
public enum PropertyOption {
    KEYWORDSPELLING,
    TABLENAMEORDER,
    TABLENAMESPELLING,
    COLUMNNAMEORDER,
    COLUMNNAMESPELLING,
    DATESYNONYMS,
    TIMESYNONYMS,
    DATETIMESYNONYMS,
    AGGREGATEFUNCTIONLANG,
    DEFAULT;
    //When extending this class keep the naming of the enums and the xml tags the same. That they can easily be
    // transformed in each other.
}
