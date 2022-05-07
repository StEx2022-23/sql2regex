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
    private final StringBuilder tableNameOrderRegEx = new StringBuilder();
    private final SpellingMistake spellingMistake = new SpellingMistake();

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
    private void rekTableNameOrder(Integer amount, List<String> tablenames, Boolean alternativeSpelling){
        StringBuilder tableNameOrderRegExSingleElement = new StringBuilder();
        if (amount == 1) {
            for(String el : tablenames){
                if(Boolean.TRUE.equals(alternativeSpelling)) {
                    tableNameOrderRegExSingleElement.append(spellingMistake.calculateAlternativeWritingStyles(el));
                } else{
                    tableNameOrderRegExSingleElement.append(el);
                }
                tableNameOrderRegExSingleElement.append("\\s*,\\s*");
            }
            tableNameOrderRegExSingleElement.replace(tableNameOrderRegExSingleElement.length()-7, tableNameOrderRegExSingleElement.length(), "");
            tableNameOrderRegExSingleElement.append("|");
            tableNameOrderRegEx.append(tableNameOrderRegExSingleElement);
        } else {
            rekTableNameOrder(amount-1, tablenames, alternativeSpelling);
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
                rekTableNameOrder(amount-1, tablenames, alternativeSpelling);
            }
        }
    }

    /**
     * combine every possible table name order to a non-capturing regex group, optional with alternative writing styles
     * @param tableNameList List<String>
     * @param alternativeSpelling Boolean
     * @return Regex (non-capturing group)
     */
    public String calculateDifferentTableNameOrders(List<String> tableNameList, Boolean alternativeSpelling){
        tableNameOrderRegEx.append("(?:");
        Integer amountOfTables = tableNameList.size();
        rekTableNameOrder(amountOfTables, tableNameList, alternativeSpelling);
        tableNameOrderRegEx.replace(tableNameOrderRegEx.length()-1, tableNameOrderRegEx.length(), "");
        tableNameOrderRegEx.append(")");
        return tableNameOrderRegEx.toString();
    }

}
