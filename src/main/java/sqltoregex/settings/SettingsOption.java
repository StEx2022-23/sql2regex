package sqltoregex.settings;

/**
 * Enum representing all possible setting options.
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
    NOT_AS_EXCLAMATION_AND_WORD,
    EXPRESSIONORDER,
    OTHERSYNONYMS,
    DEFAULT
    // when extending this class keep the naming of the enums and the xml tags the same
    // that they can easily be transformed in each other
}
