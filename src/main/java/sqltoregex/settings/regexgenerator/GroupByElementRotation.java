package sqltoregex.settings.regexgenerator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import org.springframework.util.Assert;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;

import java.util.*;

public class GroupByElementRotation extends RegExGenerator<List<Expression>> {
    private final Map<Integer, List<Expression>> groupByOrderOptionsMap = new HashMap<>();
    private final SettingsOption settingsOption;
    private Integer groupByOrderOptionsCounter = 0;
    Map<String, String> tableNamesWithAlias;

    public GroupByElementRotation(SettingsOption settingsOption) {
        Assert.notNull(settingsOption, "SettingsOption must not be null");
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
        ExpressionDeParserForRegEx expressionDeParserForRegEx;
        //TODO: SettingsContainer initalisierung eventuell auflösen, oder das ganze gleich rausnehmen indem, wie besprochen List<String> übergeben wird und außerhalb der OrderRotation geparsed wird (Wäre auch eher Konform mit SRP)
        expressionDeParserForRegEx = new ExpressionDeParserForRegEx(new SelectVisitorAdapter(), buffer, new SettingsContainer());
        expressionDeParserForRegEx.setAliasMap(this.tableNamesWithAlias);
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
