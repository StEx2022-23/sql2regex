package sqltoregex.settings.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation extends RegExGenerator<List<String>> {
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";
    private final StringBuilder buffer = new StringBuilder();
    private final SettingsOption settingsOption;
    private SpellingMistake spellingMistake;

    public OrderRotation(SettingsOption settingsOption, SpellingMistake spellingMistake) {
        Assert.notNull(spellingMistake, "SpellingMistake must not be null");
        Assert.notNull(settingsOption, "SettingsOption must not be null");
        this.spellingMistake = spellingMistake;
        this.settingsOption = settingsOption;
    }

    public OrderRotation(SettingsOption settingsOption) {
        this.settingsOption = settingsOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderRotation that = (OrderRotation) o;
        return Objects.equals(spellingMistake, that.spellingMistake) && settingsOption == that.settingsOption;
    }

    /**
     * combine every possible table name order to a non-capturing regex group, optional with alternative writing styles
     *
     * @param valueList List<String>
     * @return Regex (non-capturing group)
     */
    public String generateRegExFor(List<String> valueList) {
        Assert.notNull(valueList, "Value list must not be null!");
        buffer.replace(0, buffer.length(),"");
        buffer.append(isCapturingGroup ? "(" : "(?:");
        Integer amountOfElements = valueList.size();
        orderRotationRek(amountOfElements, valueList);
        buffer.replace(buffer.length() - 1, buffer.length(), "");
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public SettingsOption getSettingsOption() {
        return settingsOption;
    }

    public SpellingMistake getSpellingMistake() {
        return spellingMistake;
    }

    public void setSpellingMistake(SpellingMistake spellingMistake) {
        Assert.notNull(spellingMistake, "Spelling Mistake must not be null");
        this.spellingMistake = spellingMistake;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spellingMistake, settingsOption);
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
                if (spellingMistake != null) {
                    String temp = iterator.next();
                    if (temp.contains(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE))
                        singleValue.append(temp.replace(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE, ""));
                    else singleValue.append(spellingMistake.generateRegExFor(temp));
                } else {
                    singleValue.append(iterator.next());
                }
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
