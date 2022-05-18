package sqltoregex.property.regexgenerator;

import org.springframework.util.Assert;
import sqltoregex.property.SettingsOption;
import sqltoregex.property.RegExGenerator;

import java.util.*;

/**
 * The OrderRotation class allows creating a RegEx expression that takes into all possible order possibilities.
 * Example:
 * SELECT table1, table2
 * SELECT (?:table1\s*,\s*table2|table2\s*,\s*table1)
 */
public class OrderRotation implements RegExGenerator<SettingsOption, List<String>> {
    private final StringBuilder orderRotationOfValueList = new StringBuilder();
    private SpellingMistake spellingMistake;
    private final SettingsOption settingsoption;
    protected boolean isCapturingGroup = false;

    public OrderRotation(SettingsOption settingsOption, SpellingMistake spellingMistake){
        Assert.notNull(spellingMistake, "SpellingMistake must not be null");
        Assert.notNull(settingsOption, "SettingsOption must not be null");
        this.spellingMistake = spellingMistake;
        this.settingsoption = settingsOption;
    }

    public OrderRotation(SettingsOption settingsOption){
        this.settingsoption = settingsOption;
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
                    singleValue.append(spellingMistake.generateRegExFor(iterator.next()));
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
        return settingsoption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderRotation that = (OrderRotation) o;
        return Objects.equals(spellingMistake, that.spellingMistake) && settingsoption == that.settingsoption;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spellingMistake, settingsoption);
    }
}
