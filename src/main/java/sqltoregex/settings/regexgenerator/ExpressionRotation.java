package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.xml.sax.SAXException;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.RegExGenerator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpressionRotation implements RegExGenerator<List<Expression>> {
    private final Map<Integer, List<Expression>> groupByOrderOptionsMap = new HashMap<>();
    private Integer groupByOrderOptionsCounter = 0;
    private final SettingsOption settingsOption;
    protected boolean isCapturingGroup = false;

    public ExpressionRotation(SettingsOption settingsOption) {
        this.settingsOption = settingsOption;
    }

    /**
     * helper function for recursive tablename order concatenation
     * @param amount Integer
     * @param valueList List<String>
     */
    private void generateGroupByOrderOptionsRek(Integer amount, List<Expression> valueList){
        if (amount == 1) {
            List<Expression> singleValue = new ArrayList<>(valueList);
            groupByOrderOptionsMap.put(groupByOrderOptionsCounter, singleValue);
            groupByOrderOptionsCounter++;
        } else {
            generateGroupByOrderOptionsRek(amount-1, valueList);
            for(int i = 0; i < amount-1;i++){
                if (amount % 2 == 0){
                    Expression temp;
                    temp = valueList.get(amount-1);
                    valueList.set(amount-1, valueList.get(i));
                    valueList.set(i, temp);
                } else {
                    Expression temp;
                    temp = valueList.get(amount-1);
                    valueList.set(amount-1, valueList.get(0));
                    valueList.set(0, temp);
                }
                generateGroupByOrderOptionsRek(amount-1, valueList);
            }
        }
    }

    @Override
    public String generateRegExFor(List<Expression> input) {
        try{
            generateGroupByOrderOptionsRek(input.size(), input);
            StringBuilder buffer = new StringBuilder();
            ExpressionDeParserForRegEx expressionDeParserForRegEx = new ExpressionDeParserForRegEx(new SelectVisitorAdapter(), buffer, new SettingsManager());
            buffer.append(isCapturingGroup ? "(?:" : "(");
            for(Map.Entry<Integer, List<Expression>> entry : groupByOrderOptionsMap.entrySet()){
                buffer.append("(");
                Iterator<Expression> expressionIterator = groupByOrderOptionsMap.get(entry.getKey()).iterator();
                while(expressionIterator.hasNext()){
                    expressionIterator.next().accept(expressionDeParserForRegEx);
                    if(expressionIterator.hasNext()){
                        buffer.append(",");
                    }
                }
                buffer.append(")");
                buffer.append("|");
            }
            buffer.deleteCharAt(buffer.length()-1);
            buffer.append(")");
            return buffer.toString();
        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "Generate RegEx for Expressionlist go wrong: {0}", e.toString());
        }
        return null;
    }

    /**
     * Sets whether there will be an enclosing non capturing group (?: ... ) around the generated regEx.
     * @param capturingGroup true for capturing group false for non-capturing group
     */
    public void setCapturingGroup(boolean capturingGroup) {
        isCapturingGroup = capturingGroup;
    }

    @Override
    public SettingsOption getSettingsOption() {
        return settingsOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionRotation that = (ExpressionRotation) o;
        return settingsOption == that.settingsOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingsOption);
    }
}
