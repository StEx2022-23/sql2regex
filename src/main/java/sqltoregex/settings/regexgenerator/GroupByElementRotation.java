package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.xml.sax.SAXException;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupByElementRotation extends RegExGenerator<List<Expression>> {
    private final Map<Integer, List<Expression>> groupByOrderOptionsMap = new HashMap<>();
    private final SettingsOption settingsOption;
    private Integer groupByOrderOptionsCounter = 0;
    Map<String, String> tableNamesWithAlias;

    public GroupByElementRotation(SettingsOption settingsOption) {
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
        generateGroupByOrderOptionsRek(input.size(), input);
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParserForRegEx expressionDeParserForRegEx = null;
        try {
            expressionDeParserForRegEx = new ExpressionDeParserForRegEx(new SelectVisitorAdapter(), buffer, new SettingsManager());
            expressionDeParserForRegEx.setAliasMap(this.tableNamesWithAlias);
        } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException | URISyntaxException e) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.INFO, "Generate RegEx for Expressionlist go wrong: {0}", e.toString());
        }
        buffer.append(isCapturingGroup ? "(?:" : "(");
        for(Map.Entry<Integer, List<Expression>> entry : groupByOrderOptionsMap.entrySet()){
            Iterator<Expression> expressionIterator = groupByOrderOptionsMap.get(entry.getKey()).iterator();
            while(expressionIterator.hasNext()){
                expressionIterator.next().accept(expressionDeParserForRegEx);
                if(expressionIterator.hasNext()){
                    buffer.append(",");
                }
            }
            buffer.append("|");
        }
        buffer.deleteCharAt(buffer.length()-1);
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Sets whether there will be an enclosing non capturing group (?: ... ) around the generated regEx.
     * @param capturingGroup true for capturing group false for non-capturing group
     */
    @Override
    public void setCapturingGroup(boolean capturingGroup) {
        this.isCapturingGroup = capturingGroup;
    }

    @Override
    public SettingsOption getSettingsOption() {
        return settingsOption;
    }

    public void setAliasMap(Map<String, String> getAliasMap) {
        this.tableNamesWithAlias = getAliasMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupByElementRotation that = (GroupByElementRotation) o;
        return settingsOption == that.settingsOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingsOption);
    }
}
