package sqltoregex.property.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.property.Property;
import sqltoregex.property.PropertyOption;

import java.util.*;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation implements Property<PropertyOption>, RegExGenerator<List<String>> {
    private final StringBuilder tableNameOrderRegEx = new StringBuilder();
    private SpellingMistake spellingMistake;
    private final PropertyOption propertyoption;
    protected boolean isCapturingGroup = false;

    public OrderRotation(PropertyOption propertyOption, SpellingMistake spellingMistake){
        Assert.notNull(spellingMistake, "SpellingMistake must not be null");
        Assert.notNull(propertyOption, "PropertyOption must not be null");
        this.spellingMistake = spellingMistake;
        this.propertyoption = propertyOption;
    }

    public OrderRotation(PropertyOption propertyOption){
        this.propertyoption = propertyOption;
    }

    @Override
    public Set<PropertyOption> getSettings() {
        return new HashSet<>(Arrays.asList(propertyoption));
    }

    /**
     * helper function for recursive tablename order concatenation
     * @param amount Integer
     * @param tableNames List<String>
     */
    private void rekTableNameOrder(Integer amount, List<String> tableNames){
        StringBuilder tableNameOrderRegExSingleElement = new StringBuilder();
        if (amount == 1) {
            for(String el : tableNames){
                if(spellingMistake != null) {
                    tableNameOrderRegExSingleElement.append(spellingMistake.generateRegExFor(el));
                } else{
                    tableNameOrderRegExSingleElement.append(el);
                }
                tableNameOrderRegExSingleElement.append("\\s*,\\s*");
            }
            tableNameOrderRegExSingleElement.replace(tableNameOrderRegExSingleElement.length()-7, tableNameOrderRegExSingleElement.length(), "");
            tableNameOrderRegExSingleElement.append("|");
            tableNameOrderRegEx.append(tableNameOrderRegExSingleElement);
        } else {
            rekTableNameOrder(amount-1, tableNames);
            for(int i = 0; i < amount-1;i++){
                if (amount % 2 == 0){
                    String temp;
                    temp = tableNames.get(amount-1);
                    tableNames.set(amount-1, tableNames.get(i));
                    tableNames.set(i, temp);
                } else {
                    String temp;
                    temp = tableNames.get(amount-1);
                    tableNames.set(amount-1, tableNames.get(0));
                    tableNames.set(0, temp);
                }
                rekTableNameOrder(amount-1, tableNames);
            }
        }
    }

    /**
     * combine every possible table name order to a non-capturing regex group, optional with alternative writing styles
     * @param tableNameList List<String>
     * @return Regex (non-capturing group)
     */
    public String generateRegExFor(List<String> tableNameList){
        Assert.notNull(tableNameList, "tableNameList must not be null");
        if(Boolean.TRUE.equals(this.isCapturingGroup)) tableNameOrderRegEx.append("(?:");
        else tableNameOrderRegEx.append("(");
        Integer amountOfTables = tableNameList.size();
        rekTableNameOrder(amountOfTables, tableNameList);
        tableNameOrderRegEx.replace(tableNameOrderRegEx.length()-1, tableNameOrderRegEx.length(), "");
        tableNameOrderRegEx.append(")");
        return tableNameOrderRegEx.toString();
    }

    public void setSpellingMistake(SpellingMistake spellingMistake){
        Assert.notNull(spellingMistake, "Spelling Mistake must not be null");
        this.spellingMistake = spellingMistake;
    }

    public SpellingMistake getSpellingMistake() {
        return spellingMistake;
    }

    public void setCapturingGroup(boolean capturingGroup) {
        isCapturingGroup = capturingGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderRotation that = (OrderRotation) o;
        return Objects.equals(spellingMistake, that.spellingMistake) && propertyoption == that.propertyoption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spellingMistake, propertyoption);
    }
}
