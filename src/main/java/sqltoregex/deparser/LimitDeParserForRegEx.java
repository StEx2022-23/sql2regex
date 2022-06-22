package sqltoregex.deparser;

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import static sqltoregex.deparser.StatementDeParserForRegEx.QUOTATION_MARK_REGEX;
/**
 * Implements own {@link LimitDeparser} to generate regex.
 */
public class LimitDeParserForRegEx extends LimitDeparser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private SpellingMistake keywordSpellingMistake;

    /**
     * Short constructor for LimitDeParserForRegEx. Inits the expanded constructor.
     * @param buffer {@link StringBuilder}
     */
    public LimitDeParserForRegEx(StringBuilder buffer) {
        super(buffer);
    }

    /**
     * Extended constructor for LimitDeParserForRegEx.
     * @param buffer {@link StringBuilder}
     * @param settingsContainer {@link SettingsContainer}
     */
    public LimitDeParserForRegEx(StringBuilder buffer, SettingsContainer settingsContainer) {
        super(buffer);
        this.keywordSpellingMistake = settingsContainer.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
    }

    /**
     * Deparses the whole {@link Limit} object.
     * {@link SuppressWarnings}: PMD.CyclomaticComplexity
     * @param limit {@link Limit}
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deParse(Limit limit) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "LIMIT"));
        buffer.append(REQUIRED_WHITE_SPACE);
        if (limit.getRowCount() instanceof NullValue) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "NULL"));
        } else {
            buffer.append(StatementDeParserForRegEx.addQuotationMarks(limit.getRowCount().toString().replaceAll(QUOTATION_MARK_REGEX, "")));
            if (limit.getRowCount() instanceof AllValue) {
                buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "ALL"));
            } else {
                if (null != limit.getOffset() && null != limit.getRowCount()) {
                    buffer.append("(?:");
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "OFFSET"));
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(StatementDeParserForRegEx.addQuotationMarks(limit.getOffset().toString().replaceAll(QUOTATION_MARK_REGEX, "")));
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append("|");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(StatementDeParserForRegEx.addQuotationMarks(limit.getOffset().toString().replaceAll(QUOTATION_MARK_REGEX, "")));
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(",");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(StatementDeParserForRegEx.addQuotationMarks(limit.getRowCount().toString().replaceAll(QUOTATION_MARK_REGEX, "")));
                    buffer.append(")");
                }

                if (null != limit.getOffset() && null == limit.getRowCount()) {
                    buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "LIMIT"));
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(StatementDeParserForRegEx.addQuotationMarks(limit.getOffset().toString().replaceAll(QUOTATION_MARK_REGEX, "")));
                }
            }
        }
    }
}
