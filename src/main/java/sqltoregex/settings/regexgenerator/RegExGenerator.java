package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import org.springframework.util.Assert;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.settings.regexgenerator.synonymgenerator.SynonymGenerator;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

public abstract class RegExGenerator<T> implements IRegExGenerator<T>{
    protected boolean isCapturingGroup = false;
    protected static final String OPTIONAL_WHITE_SPACE = "\\s*";

    @Override
    public void setCapturingGroup(boolean capturingGroup) {
        this.isCapturingGroup = capturingGroup;
    }

    public static String useSpellingMistake(SpellingMistake spellingMistake, String str){
        if(null != spellingMistake) return spellingMistake.generateRegExFor(str);
        else return str;
    }

    public static <T> String useStringSynonymGenerator(SynonymGenerator<?, T> synonymGenerator, T str){
        if(null != synonymGenerator) return synonymGenerator.generateRegExFor(str);
        else return str.toString();
    }

    public static String useExpressionSynonymGenerator(SynonymGenerator<?, Expression> synonymGenerator, Expression expression){
        if(null != synonymGenerator) return synonymGenerator.generateRegExFor(expression);
        else return expression.toString();
    }

    public static String useOrderRotation(OrderRotation orderRotation, List<String> stringList){
        if(null != orderRotation) return orderRotation.generateRegExFor(stringList);
        else return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, stringList);
    }

    public static String useExpressionRotation(ExpressionRotation expressionRotation, ExpressionDeParserForRegEx expressionDeParserForRegEx, List<Expression> expressionList, StringBuilder _buffer){
        if(null != expressionRotation) return expressionRotation.generateRegExFor(expressionList);
        else {
            Iterator<Expression> expressionIterator = expressionList.iterator();
            while (expressionIterator.hasNext()){
                expressionIterator.next().accept(expressionDeParserForRegEx);
                if(expressionIterator.hasNext()) _buffer.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
            return "";
        }
    }
}
