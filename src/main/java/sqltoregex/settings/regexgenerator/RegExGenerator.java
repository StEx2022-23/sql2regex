package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class RegExGenerator<T> implements IRegExGenerator<T> {
    protected static final String OPTIONAL_WHITE_SPACE = "\\s*";
    protected boolean isNonCapturingGroup = true;
    private final SettingsOption settingsOption;

    protected RegExGenerator(SettingsOption settingsOption){
        Assert.notNull(settingsOption, "SettingsOption must not be null");
        this.settingsOption = settingsOption;
    }

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
    public SettingsOption getSettingsOption() {
        return this.settingsOption;
    }

    @Override
    public void setNonCapturingGroup(boolean capturingGroup) {
        this.isNonCapturingGroup = capturingGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSettingsOption());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        try{
            RegExGenerator<T> that = (RegExGenerator<T>) o;
            return this.getSettingsOption() == that.getSettingsOption();
        } catch (ClassCastException e){
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.WARNING, "Error while casting RegExGenerator: {0}", e);
        }
        return false;
    }
}
