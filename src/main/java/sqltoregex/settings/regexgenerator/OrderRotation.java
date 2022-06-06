package sqltoregex.settings.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation extends RegExGenerator<List<String>> {

    public OrderRotation(SettingsOption settingsOption) {
        super(settingsOption);
    }

    @Override
    public List<String> generateAsList(List<String> valueList) {
        Assert.notNull(valueList, "Value list must not be null!");
        List<String> stringList = new ArrayList<>(valueList);
        int amountOfElements = valueList.size();
        return orderRotationRek(amountOfElements, stringList);
    }

    /**
     * helper function for recursive tablename order concatenation
     *
     * @param amount    Integer
     * @param valueList List<String>
     */
    private List<String> orderRotationRek(int amount, List<String> valueList) {
        StringBuilder singleValue = new StringBuilder();
        List<String> stringList = new LinkedList<>();
        if (amount == 1) {
            Iterator<String> iterator = valueList.iterator();
            while (iterator.hasNext()) {
                singleValue.append(iterator.next());
                if (iterator.hasNext()) {
                    singleValue.append("\\s*,\\s*");
                }
            }
            stringList.add(singleValue.toString());
        } else {
            stringList.addAll(orderRotationRek(amount - 1, valueList));
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
                stringList.addAll(orderRotationRek(amount - 1, valueList));
            }
        }
        return stringList;
    }

    public static String useOrDefault(OrderRotation orderRotation, List<String> valueList){
        if (null != orderRotation) return orderRotation.generateRegExFor(valueList);
        return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, valueList);
    }

    public static List<String> generateAsListOrDefault(OrderRotation orderRotation, List<String> valueList){
        if (null != orderRotation) return orderRotation.generateAsList(valueList);
        return valueList;
    }
}
