package sqltoregex.deparser;

import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.util.deparser.DropDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;

public class DropDeParserForRegEx extends DropDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private final SpellingMistake keywordSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final OrderRotation tableNameOrderRotation;

    public DropDeParserForRegEx(SettingsContainer settings){
        this(new StringBuilder(), settings);
    }

    public DropDeParserForRegEx(StringBuilder buffer, SettingsContainer settings) {
        super(buffer);
        this.keywordSpellingMistake =  settings.get(SpellingMistake.class).get(SettingsOption.KEYWORDSPELLING);
        this.tableNameSpellingMistake =  settings.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.tableNameOrderRotation = settings.get(OrderRotation.class).get(SettingsOption.TABLENAMEORDER);
    }

    @Override
    public void deParse(Drop drop) {
        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "DROP"));
        buffer.append(REQUIRED_WHITE_SPACE);

        buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, drop.getType()));
        buffer.append(REQUIRED_WHITE_SPACE);

        if (drop.isIfExists()) {
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "IF"));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(SpellingMistake.useOrDefault(this.keywordSpellingMistake, "EXISTS"));
            buffer.append(REQUIRED_WHITE_SPACE);
        }

        buffer.append(SpellingMistake.useOrDefault(this.tableNameSpellingMistake, drop.getName().toString()));

        if (drop.getParameters() != null && !drop.getParameters().isEmpty()) {
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(OrderRotation.useOrDefault(this.tableNameOrderRotation, drop.getParameters()));
        }
    }
}
