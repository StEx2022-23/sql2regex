package sqltoregex.settings;

/**
 * Enum representing all possible setting options.
 * @author Patrick Binkert
 * @author Maximilian Förster
 */
public enum SettingsOption {
    COLUMNNAMESPELLING,
    KEYWORDSPELLING,
    TABLENAMESPELLING,
    STRINGVALUESPELLING,
    COLUMNNAMEORDER,
    TABLENAMEORDER,
    INDEXCOLUMNNAMEORDER,
    INDEXCOLUMNNAMESPELLING,
    DATESYNONYMS,
    DATETIMESYNONYMS,
    TIMESYNONYMS,
    AGGREGATEFUNCTIONLANG,
    DATATYPESYNONYMS,
    GROUPBYELEMENTORDER,
    INSERTINTOVALUESORDER,
    OTHERSYNONYMS,
    DEFAULT
    // when extending this class keep the naming of the enums and the xml tags the same
    // that they can easily be transformed in each other
    // Spellings and orders need to be added in pairs
}
