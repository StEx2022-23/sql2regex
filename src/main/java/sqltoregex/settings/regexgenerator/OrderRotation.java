package sqltoregex.settings.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.RegExGenerator;

import java.util.*;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation implements RegExGenerator<List<String>> {
    private final StringBuilder orderRotationOfValueList = new StringBuilder();
    private SpellingMistake spellingMistake;
    private final SettingsOption settingsOption;
    protected boolean isCapturingGroup = false;
    private static final String DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE = "##########";

    public OrderRotation(SettingsOption settingsOption, SpellingMistake spellingMistake){
        Assert.notNull(spellingMistake, "SpellingMistake must not be null");
        Assert.notNull(settingsOption, "SettingsOption must not be null");
        this.spellingMistake = spellingMistake;
        this.settingsOption = settingsOption;
    }

    public OrderRotation(SettingsOption settingsOption){
        this.settingsOption = settingsOption;
    }

    /**
     * helper function for recursive tablename order concatenation
     * @param amount Integer
     * @param valueList List<String>
     */
    private void orderRotationRek(Integer amount, List<String> valueList){
        StringBuilder singleValue = new StringBuilder();
        if (amount == 1) {
            Iterator<String> iterator = valueList.iterator();
            while (iterator.hasNext()) {
                if(spellingMistake != null) {
                    String temp = iterator.next();
                    if(temp.contains(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE)) singleValue.append(temp.replace(DELIMITER_FOR_ORDERROTATION_WITHOUT_SPELLINGMISTAKE, ""));
                    else singleValue.append(spellingMistake.generateRegExFor(temp));
                } else{
                    singleValue.append(iterator.next());
                }
                if(iterator.hasNext()){
                    singleValue.append("\\s*,\\s*");

                }

            }
            singleValue.append("|");
            orderRotationOfValueList.append(singleValue);
        } else {
            orderRotationRek(amount-1, valueList);
            for(int i = 0; i < amount-1;i++){
                if (amount % 2 == 0){
                    String temp;
                    temp = valueList.get(amount-1);
                    valueList.set(amount-1, valueList.get(i));
                    valueList.set(i, temp);
                } else {
                    String temp;
                    temp = valueList.get(amount-1);
                    valueList.set(amount-1, valueList.get(0));
                    valueList.set(0, temp);
                }
                orderRotationRek(amount-1, valueList);
            }
        }
    }

    /**
     * combine every possible table name order to a non-capturing regex group, optional with alternative writing styles
     * @param valueList List<String>
     * @return Regex (non-capturing group)
     */
    public String generateRegExFor(List<String> valueList){
        orderRotationOfValueList.replace(0, orderRotationOfValueList.length(), "");
        Assert.notNull(valueList, "Value list must not be null!");
        orderRotationOfValueList.append(isCapturingGroup ? '(' : "(?:");
        Integer amountOfElements = valueList.size();
        orderRotationRek(amountOfElements, valueList);
        orderRotationOfValueList.replace(orderRotationOfValueList.length()-1, orderRotationOfValueList.length(), "");
        orderRotationOfValueList.append(')');
        return orderRotationOfValueList.toString();
    }

    public void setSpellingMistake(SpellingMistake spellingMistake){
        Assert.notNull(spellingMistake, "Spelling Mistake must not be null");
        this.spellingMistake = spellingMistake;
    }

    public SpellingMistake getSpellingMistake() {
        return spellingMistake;
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
        OrderRotation that = (OrderRotation) o;
        return Objects.equals(spellingMistake, that.spellingMistake) && settingsOption == that.settingsOption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spellingMistake, settingsOption);
    }
}
