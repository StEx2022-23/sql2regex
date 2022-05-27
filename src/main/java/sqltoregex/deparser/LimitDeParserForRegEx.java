package sqltoregex.deparser;

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import sqltoregex.settings.regexgenerator.IRegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

public class LimitDeParserForRegEx extends LimitDeparser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private SpellingMistake keywordSpellingMistake;

    public LimitDeParserForRegEx(StringBuilder buffer) {
        super(buffer);
    }

    public LimitDeParserForRegEx(StringBuilder buffer, SettingsManager settingsManager) {
        super(buffer);
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class).orElse(null);
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deParse(Limit limit) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "LIMIT"));
        buffer.append(REQUIRED_WHITE_SPACE);
        if (limit.getRowCount() instanceof NullValue) {
            buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "NULL"));
        } else {
            buffer.append(limit.getRowCount());
            if (limit.getRowCount() instanceof AllValue) {
                buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "ALL"));
            } else {
                if (null != limit.getOffset() && null != limit.getRowCount()) {
                    buffer.append("(?:");
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "OFFSET"));
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(limit.getOffset());
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append("|");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(limit.getOffset());
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(",");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(limit.getRowCount());
                    buffer.append(")");
                }

                if (null != limit.getOffset() && null == limit.getRowCount()) {
                    buffer.append(RegExGenerator.useSpellingMistake(this.keywordSpellingMistake, "LIMIT"));
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(limit.getOffset());
                }
            }
        }
    }
}
