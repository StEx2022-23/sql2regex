package sqltoregex.settings;

/**
 * Enum representing all possible setting options.
 * @author Patrick Binkert
 * @author Maximilian Förster
 */
public enum SettingsOption {
    COLUMNNAMESPELLING,
    INDEXCOLUMNNAMESPELLING,
    KEYWORDSPELLING,
    STRINGVALUESPELLING,
    TABLENAMESPELLING,
    COLUMNNAMEORDER,
    GROUPBYELEMENTORDER,
    INDEXCOLUMNNAMEORDER,
    INSERTINTOVALUESORDER,
    TABLENAMEORDER,
    DATATYPESYNONYMS,
    DATESYNONYMS,
    DATETIMESYNONYMS,
    TIMESYNONYMS,
    AGGREGATEFUNCTIONLANG,
    OTHERSYNONYMS,
    DEFAULT
    // when extending this class keep the naming of the enums and the xml tags the same
    // that they can easily be transformed in each other
    // Spellings and orders need to be added in pairs
}
