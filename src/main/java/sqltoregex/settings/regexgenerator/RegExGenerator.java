package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

public abstract class RegExGenerator<T> implements IRegExGenerator<T> {
    protected static final String OPTIONAL_WHITE_SPACE = "\\s*";
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    protected boolean isCapturingGroup = false;

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
        for (int i = 0; i < stringList.size(); i++) {
            if (stringList.get(i).contains(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE)) {
                stringList.set(i, stringList.get(i).replace(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE, ""));
            }
        }
        return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, stringList);
    }

    public static String useExpressionRotation(ExpressionRotation expressionRotation,
                                               ExpressionDeParserForRegEx expressionDeParserForRegEx,
                                               List<Expression> expressionList) {
        if (null != expressionRotation) return expressionRotation.generateRegExFor(expressionList);
        else {
            Iterator<Expression> expressionIterator = expressionList.iterator();
            while (expressionIterator.hasNext()) {
                expressionIterator.next().accept(expressionDeParserForRegEx);
                if (expressionIterator.hasNext()) expressionDeParserForRegEx.getBuffer().append(",");
            }
            return expressionDeParserForRegEx.getBuffer().toString();
        }
    }

    @Override
    public void setCapturingGroup(boolean capturingGroup) {
        this.isCapturingGroup = capturingGroup;
    }
}
