package sqltoregex.property;

import java.util.Collections;
import java.util.List;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation implements Property{
    public StringBuilder TableNameOrderRegEx = new StringBuilder();
    public SpellingMistake spellingMistake = new SpellingMistake();

    @Override
    public List<String> getSettings() {
        return Collections.singletonList("OrderRotation");
    }

    /**
     * helper function for recursive tablename order concatenation
     * @param amount Integer
     * @param tablenames List<String>
     * @param alternativeSpelling Boolean
     */
    private void _rek_TableNameOrder(Integer amount, List<String> tablenames, Boolean alternativeSpelling){
        StringBuilder TableNameOrderRegExSingleElement = new StringBuilder();
        if (amount == 1) {
            for(String el : tablenames){
                if(alternativeSpelling) {
                    TableNameOrderRegExSingleElement.append(spellingMistake.calculateAlternativeWritingStyles(el));
                } else{
                    TableNameOrderRegExSingleElement.append(el);
                }
                TableNameOrderRegExSingleElement.append("\\s*,\\s*");
            }
            TableNameOrderRegExSingleElement.replace(TableNameOrderRegExSingleElement.length()-7, TableNameOrderRegExSingleElement.length(), "");
            TableNameOrderRegExSingleElement.append("|");
            TableNameOrderRegEx.append(TableNameOrderRegExSingleElement);
        } else {
            _rek_TableNameOrder(amount-1, tablenames, alternativeSpelling);
            for(int i = 0; i < amount-1;i++){
                if (amount % 2 == 0){
                    String temp;
                    temp = tablenames.get(amount-1);
                    tablenames.set(amount-1, tablenames.get(i));
                    tablenames.set(i, temp);
                } else {
                    String temp;
                    temp = tablenames.get(amount-1);
                    tablenames.set(amount-1, tablenames.get(0));
                    tablenames.set(0, temp);
                }
                _rek_TableNameOrder(amount-1, tablenames, alternativeSpelling);
            }
        }
    }

    /**
     * combine every possible table name order to a non-capturing regex group, optional with alternative writing styles
     * @param TableNameList List<String>
     * @param alternativeSpelling Boolean
     * @return Regex (non-capturing group)
     */
    public String CalculateDifferentTableNameOrders(List<String> TableNameList, Boolean alternativeSpelling){
        TableNameOrderRegEx.append("(?:");
        Integer amountOfTables = TableNameList.size();
        _rek_TableNameOrder(amountOfTables, TableNameList, alternativeSpelling);
        TableNameOrderRegEx.replace(TableNameOrderRegEx.length()-1, TableNameOrderRegEx.length(), "");
        TableNameOrderRegEx.append(")");
        return TableNameOrderRegEx.toString();
    }

}
