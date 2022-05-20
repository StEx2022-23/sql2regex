package sqltoregex.deparser;

import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.util.deparser.LimitDeparser;
import sqltoregex.settings.RegExGenerator;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;

public class LimitDeParserForRegEx extends LimitDeparser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private RegExGenerator<String> keywordSpellingMistake;
    boolean isKeywordSpellingMistake;

    public LimitDeParserForRegEx(StringBuilder buffer) {
        super(buffer);
    }

    public LimitDeParserForRegEx(StringBuilder buffer, SettingsManager settingsManager) {
        super(buffer);
        this.isKeywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING);
        if(this.isKeywordSpellingMistake){
            keywordSpellingMistake = settingsManager.getSettingBySettingOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class);
        }
    }

    @Override
    public void deParse(Limit limit) {
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("LIMIT") : "LIMIT");
        buffer.append(REQUIRED_WHITE_SPACE);
        if (limit.isLimitNull()) {
            buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("NULL") : "NULL");
        } else {
            if (limit.isLimitAll()) {
                buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("ALL") : "ALL");
            } else {
                if (null != limit.getOffset() && null != limit.getRowCount()) {
                    buffer.append("(?:");
                    buffer.append(OPTIONAL_WHITE_SPACE);
                    buffer.append(limit.getRowCount());
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("OFFSET") : "OFFSET");
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
                    buffer.append(isKeywordSpellingMistake ? keywordSpellingMistake.generateRegExFor("LIMIT") : "LIMIT");
                    buffer.append(REQUIRED_WHITE_SPACE);
                    buffer.append(limit.getOffset());
                }
            }
        }
    }
}
