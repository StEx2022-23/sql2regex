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

    /**
     * Constructor of OrderRotation. Init the super class.
     * @param settingsOption one of enum {@link SettingsOption}
     * @see RegExGenerator
     */
    public OrderRotation(SettingsOption settingsOption) {
        super(settingsOption);
    }

    /**
     * Generate a list of strings with all possible orders.
     * @param valueList with all items
     * @return list of string with all possible orders
     */
    @Override
    public List<String> generateAsList(List<String> valueList) {
        Assert.notNull(valueList, "Value list must not be null!");
        List<String> stringList = new ArrayList<>(valueList);
        int amountOfElements = valueList.size();
        return orderRotationRek(amountOfElements, stringList);
    }

    /**
     * helper function for recursive tablename order concatenation
     * @param amount amount of items to order rotate
     * @param valueList List of String with to order rotate items
     */
    private List<String> orderRotationRek(int amount, List<String> valueList) {
        StringBuilder singleValue = new StringBuilder();
        List<String> stringList = new LinkedList<>();
        if (amount == 1) {
            Iterator<String> iterator = valueList.iterator();
            while (iterator.hasNext()) {
//                singleValue.append("[`´\"']?").append(iterator.next()).append("[`´\"']?");
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

    /**
     * Generate a regex with all possible orders, if the param orderRotation isn't null.
     * Otherwise, only the joined string list returned.
     * @param orderRotation OrderRotation object
     * @param valueList list of strings to order rotate
     * @return generated regex or str
     */
    public static String useOrDefault(OrderRotation orderRotation, List<String> valueList){
        if (null != orderRotation) return orderRotation.generateRegExFor(valueList);
        return String.join(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE, valueList);
    }

    /**
     * Generate a list of strings with all possible orders, if the param orderRotation isn't null.
     * Otherwise, the given list of string is returned.
     * @param orderRotation OrderRotation object
     * @param valueList list of strings to order rotate
     * @return generated list of orders as string or given list
     */
    public static List<String> generateAsListOrDefault(OrderRotation orderRotation, List<String> valueList){
        if (null != orderRotation) return orderRotation.generateAsList(valueList);
        return valueList;
    }
}
