package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.List;

public abstract class RegExGenerator<T> implements IRegExGenerator<T> {
    protected static final String OPTIONAL_WHITE_SPACE = "\\s*";
    protected boolean isNonCapturingGroup = true;

    public static String useSpellingMistake(SpellingMistake spellingMistake, String str) {
        if (null != spellingMistake) return spellingMistake.generateRegExFor(str);
        else return str;
    }

    public static <T> String useStringSynonymGenerator(SynonymGenerator<?, T> synonymGenerator, T str) {
        if (null != synonymGenerator) return synonymGenerator.generateRegExFor(str);
        else return str.toString();
    }

    public static String useExpressionSynonymGenerator(SynonymGenerator<SimpleDateFormat, Expression> synonymGenerator,
                                                       Expression expression) {
        if (null != synonymGenerator) return synonymGenerator.generateRegExFor(expression);
        else return new DateAndTimeFormatSynonymGenerator(SettingsOption.DEFAULT).searchSynonymToString(expression);
    }

    public static String useOrderRotation(OrderRotation orderRotation, List<String> stringList) {
        if (null != orderRotation) return orderRotation.generateRegExFor(stringList);
        return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, stringList);
    }

    @Override
    public void setNonCapturingGroup(boolean capturingGroup) {
        this.isNonCapturingGroup = capturingGroup;
    }
}
