package sqltoregex.deparser;

import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.RegExGenerator;
import sqltoregex.settings.regexgenerator.SpellingMistake;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class StatementDeParserForRegEx extends StatementDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    public static final String WITH = "WITH";
    ExpressionDeParserForRegEx expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;
    RegExGenerator<String> keywordSpellingMistake;

    public StatementDeParserForRegEx(StringBuilder buffer, SettingsManager settingsManager) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        this(new ExpressionDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, StringBuilder buffer, SettingsManager settingsManager) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        this(expressionDeParser, new SelectDeParserForRegEx(settingsManager), buffer, settingsManager);
    }

    public StatementDeParserForRegEx(ExpressionDeParserForRegEx expressionDeParser, SelectDeParserForRegEx selectDeParser, StringBuilder buffer, SettingsManager settingsManager) {
        super(expressionDeParser, selectDeParser, buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.selectDeParserForRegEx = selectDeParser;
        this.setKeywordSpellingMistake(settingsManager);
    }

    public StatementDeParserForRegEx(ExpressionDeParser expressionDeParser, StringBuilder buffer) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        super(buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.selectDeParserForRegEx = new SelectDeParserForRegEx(new SettingsManager());
        this.setKeywordSpellingMistake(new SettingsManager());
    }

    private void setKeywordSpellingMistake(SettingsManager settingsManager){
        this.keywordSpellingMistake = settingsManager.getSettingBySettingsOption(SettingsOption.KEYWORDSPELLING, SpellingMistake.class).orElse(null);
    }

    private String useKeywordSpellingMistake(String str){
        if(null != this.keywordSpellingMistake) return this.keywordSpellingMistake.generateRegExFor(str);
        else return str;
    }

    @Override
    public void visit(Select select) {
        this.selectDeParserForRegEx.setBuffer(buffer);
        this.expressionDeParserForRegEx.setSelectVisitor(this.selectDeParserForRegEx);
        this.expressionDeParserForRegEx.setBuffer(buffer);
        this.selectDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegEx);
        if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
            buffer.append(useKeywordSpellingMistake(WITH));
            buffer.append(REQUIRED_WHITE_SPACE);
            buffer.append(selectDeParserForRegEx.handleWithItemValueList(select));
        }
        select.getSelectBody().accept(selectDeParserForRegEx);
    }

    @Override
    public void visit(Statements stmts) {
        stmts.accept(this);
    }
}
