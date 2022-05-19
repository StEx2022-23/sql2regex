package sqltoregex.deparser;

import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.xml.sax.SAXException;
import sqltoregex.settings.SettingsManager;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Iterator;

public class StatementDeParserForRegEx extends StatementDeParser {
    ExpressionDeParser expressionDeParserForRegEx;
    SelectDeParserForRegEx selectDeParserForRegEx;

    public StatementDeParserForRegEx(StringBuilder buffer) {
        super(buffer);
    }

    public StatementDeParserForRegEx(ExpressionDeParser expressionDeParser, SelectDeParser selectDeParser, StringBuilder buffer) {
        super(expressionDeParser, selectDeParser, buffer);
    }

    public StatementDeParserForRegEx(ExpressionDeParser expressionDeParser, StringBuilder buffer) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        super(buffer);
        this.expressionDeParserForRegEx = expressionDeParser;
        this.selectDeParserForRegEx = new SelectDeParserForRegEx(new SettingsManager());
    }


    @Override
    public void visit(Select select) {
        selectDeParserForRegEx.setBuffer(buffer);
        expressionDeParserForRegEx.setSelectVisitor(selectDeParserForRegEx);
        expressionDeParserForRegEx.setBuffer(buffer);
        selectDeParserForRegEx.setExpressionVisitor(expressionDeParserForRegEx);
        if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = select.getWithItemsList().iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                withItem.accept(selectDeParserForRegEx);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        select.getSelectBody().accept(selectDeParserForRegEx);
    }

    @Override
    public void visit(Statements stmts) {
        stmts.accept(this);
    }
}
