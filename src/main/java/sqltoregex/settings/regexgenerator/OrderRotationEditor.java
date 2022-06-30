package sqltoregex.settings.regexgenerator;

import org.thymeleaf.util.StringUtils;
import sqltoregex.settings.SettingsOption;

import java.beans.PropertyEditorSupport;

public class OrderRotationEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        SpellingMistake spellingMistake = (SpellingMistake) getValue();

        return spellingMistake.getSettingsOption().toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isEmpty(text)){
            setValue(null);
        }else{
            OrderRotation orderRotation;
            try{
                 orderRotation = new OrderRotation(SettingsOption.valueOf(text));
            }catch (IllegalArgumentException e){
                throw new IllegalArgumentException("SpellingMistake instantiation failed. Nested Exception:" + e.getMessage());
            }
            setValue(orderRotation);
        }
    }
}
