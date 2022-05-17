package sqltoregex.property;

/**
 * Enum representing all possible PropertyOptions.
 */
public enum SettingsOption {
    KEYWORDSPELLING,
    TABLENAMEORDER,
    TABLENAMESPELLING,
    COLUMNNAMEORDER,
    COLUMNNAMESPELLING,
    DATESYNONYMS,
    TIMESYNONYMS,
    DATETIMESYNONYMS,
    AGGREGATEFUNCTIONLANG,
    DEFAULT
    // when extending this class keep the naming of the enums and the xml tags the same
    // that they can easily be transformed in each other
}