package sqltoregex.settings;

/**
 * Enum representing all possible setting options.
 */
public enum SettingsOption {
    COLUMNNAMESPELLING,
    KEYWORDSPELLING,
    TABLENAMESPELLING,
    COLUMNNAMEORDER,
    TABLENAMEORDER,
    INDEXCOLUMNNAMEORDER,
    DATESYNONYMS,
    DATETIMESYNONYMS,
    TIMESYNONYMS,
    AGGREGATEFUNCTIONLANG,
    NOT_AS_EXCLAMATION_AND_WORD,
    GROUPBYELEMENTORDER,
    OTHERSYNONYMS,
    DEFAULT
    // when extending this class keep the naming of the enums and the xml tags the same
    // that they can easily be transformed in each other
}
