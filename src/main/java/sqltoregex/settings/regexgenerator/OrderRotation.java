package sqltoregex.settings.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation extends RegExGenerator<List<String>> {
    private final StringBuilder buffer = new StringBuilder();

    public OrderRotation(SettingsOption settingsOption) {
        super(settingsOption);
    }

    /**
     * combine every possible table name order to a non-capturing regex group, optional with alternative writing styles
     *
     * @param valueList List<String>
     * @return Regex (non-capturing group)
     */
    public String generateRegExFor(List<String> valueList) {
        List<String> rekList = new ArrayList<>(valueList);
        Assert.notNull(valueList, "Value list must not be null!");
        buffer.replace(0, buffer.length(),"");
        buffer.append(isNonCapturingGroup ? "(?:" : "(");
        Integer amountOfElements = valueList.size();
        orderRotationRek(amountOfElements, rekList);
        buffer.replace(buffer.length() - 1, buffer.length(), "");
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * helper function for recursive tablename order concatenation
     *
     * @param amount    Integer
     * @param valueList List<String>
     */
    private void orderRotationRek(Integer amount, List<String> valueList) {
        StringBuilder singleValue = new StringBuilder();
        if (amount == 1) {
            Iterator<String> iterator = valueList.iterator();
            while (iterator.hasNext()) {
                singleValue.append(iterator.next());
                if (iterator.hasNext()) {
                    singleValue.append("\\s*,\\s*");
                }
            }
            singleValue.append("|");
            buffer.append(singleValue);
        } else {
            orderRotationRek(amount - 1, valueList);
            for (int i = 0; i < amount - 1; i++) {
                if (amount % 2 == 0) {
                    String temp;
                    temp = valueList.get(amount - 1);
                    valueList.set(amount - 1, valueList.get(i));
                    valueList.set(i, temp);
                } else {
                    String temp;
                    temp = valueList.get(amount - 1);
                    valueList.set(amount - 1, valueList.get(0));
                    valueList.set(0, temp);
                }
                orderRotationRek(amount - 1, valueList);
            }
        }
    }
}
