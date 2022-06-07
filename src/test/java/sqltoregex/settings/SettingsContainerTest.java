package sqltoregex.settings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.OrderRotation;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

class SettingsContainerTest {

    @Test
    void testCast(){
        OrderRotation orderRotation1 = new OrderRotation(SettingsOption.TABLENAMEORDER);
        SpellingMistake spellingMistake1 = new SpellingMistake(SettingsOption.TABLENAMESPELLING);
        StringSynonymGenerator stringSynonymGenerator1 = new StringSynonymGenerator(SettingsOption.AGGREGATEFUNCTIONLANG);
        SettingsContainer settingsContainer = SettingsContainer.builder()
                .with(orderRotation1)
                .with(spellingMistake1)
                .with(stringSynonymGenerator1).build();

        Assertions.assertEquals(settingsContainer.get(OrderRotation.class).get(SettingsOption.TABLENAMEORDER), orderRotation1);
        Assertions.assertEquals(settingsContainer.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING), spellingMistake1);
    }
}
