package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sqltoregex.deparser.CreateDatabaseDeParserForRegEx;
import sqltoregex.deparser.StatementDeParserForRegEx;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;
import sqltoregex.visitor.StatementVisitorJoinToWhere;
import sqltoregex.visitor.StatementVisitorKeyPlacement;

import java.util.*;

/**
 * Realizes a spring service for handling the converting process.
 */
@Service
public class ConverterManagement {
    private final SettingsManager settingsManager;

    /**
     * Constructor of converter management.
     * @param settingsManager which handles {@link sqltoregex.settings.UserSettings} and presets, its autowired, no required action here
     */
    @Autowired
    public ConverterManagement(SettingsManager settingsManager) {
        Assert.notNull(settingsManager, "Settings manager must not be null");
        this.settingsManager = settingsManager;
    }

    /**
     * Builds output regex with ^ in the beginning and $ at the end, including optional semicolon.
     * @param regex generated regex
     * @return builded regex
     */
    private String buildOutputRegex(String regex) {
        return "^" + regex + ";?$";
    }

    /**
     * Logical OR-concat, if the expression-visitor detect an alternative statement, to allow both of them in the regex, including optional semicolon.
     * @param regexList List of String with alternative regex
     * @return concated regex
     */
    private String buildOutputRegex(List<String> regexList) {
        StringBuilder outputRegex = new StringBuilder();
        outputRegex.append("^");

        Set<String> sqlRegExSet = new HashSet<>(regexList);

        if (sqlRegExSet.size() > 1){
            outputRegex.append("(");
        }
        Iterator<String> iterator = sqlRegExSet.iterator();
        while (iterator.hasNext()){
            String regEx = iterator.next();
            outputRegex.append(regEx);
            outputRegex.append(";?");
            if (iterator.hasNext()){
                outputRegex.append("|");
            }
        }
        if (sqlRegExSet.size() > 1){
            outputRegex.append(")");
        }

        outputRegex.append("$");
        return outputRegex.toString();
    }

    /**
     * Deparses expressions.
     * @param sqlstatement current sql statement
     * @return generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    private String deParseExpression(String sqlstatement) throws JSQLParserException {
        Expression expression;
        expression = this.parseExpression(sqlstatement);
        ExpressionVisitor expressionVisitor = new ExpressionVisitorAdapter();
        expression.accept(expressionVisitor);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        expression.accept(expressionDeParser);
        return this.buildOutputRegex(expressionDeParser.getBuffer().toString());
    }

    /**
     * Deparses statements.
     * @param sqlStatement current sql statement
     * @param buffer StringBuilder {@link StringBuilder}
     * @param settingsType {@link sqltoregex.settings.UserSettings} or presets
     * @return  generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    private String deParseStatement(String sqlStatement, StringBuilder buffer, SettingsType settingsType) throws JSQLParserException {
        if(checkIfStatementTypeOfCreateDatabase(sqlStatement)) return new CreateDatabaseDeParserForRegEx(SettingsContainer.builder().with(settingsManager, settingsType)).deParse(sqlStatement);

        Statement statement;
        statement = this.parseStatement(sqlStatement);
        List<String> regExList = new LinkedList<>();

        StatementDeParserForRegEx defaultStatementDeParser = new StatementDeParserForRegEx(buffer, SettingsContainer.builder().with(settingsManager, settingsType));

        statement.accept(defaultStatementDeParser);
        regExList.add(defaultStatementDeParser.getBuffer().toString());

        statement.accept(new StatementVisitorJoinToWhere());

        defaultStatementDeParser.setBuffer(new StringBuilder());
        statement.accept(defaultStatementDeParser);
        regExList.add(defaultStatementDeParser.getBuffer().toString());

        for (StatementVisitorKeyPlacement.KeyPlacementOption option
                : StatementVisitorKeyPlacement.KeyPlacementOption.values()) {
            statement.accept(new StatementVisitorKeyPlacement(option));
            defaultStatementDeParser.setBuffer(new StringBuilder());
            statement.accept(defaultStatementDeParser);
            regExList.add(defaultStatementDeParser.getBuffer().toString());
        }

        return this.buildOutputRegex(regExList);
    }

    /**
     * Performs statement deparsing.
     * @param sqlStatement current sql statement
     * @return generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    public String deparse(String sqlStatement) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        return this.deParseStatement(sqlStatement, buffer, SettingsType.USER);
    }

    /**
     * Performs statement deparsing.
     * @param sqlStatement current sql statement
     * @param isOnlyExpression must be set true, if you only want to deparse an expression
     * @return generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    public String deparse(String sqlStatement, boolean isOnlyExpression) throws JSQLParserException {
        return deparse(sqlStatement, isOnlyExpression, SettingsType.USER);
    }

    /**
     * Extended deparse method, to allow all options by setting the follow parameters.
     * @param sqlStatement current sql statement
     * @param isOnlyExpression must be set true, if you only want to deparse an expression
     * @param settingsType UserSettings or presets
     * @return generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    public String deparse(String sqlStatement, boolean isOnlyExpression, SettingsType settingsType) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        if (isOnlyExpression) {
            return this.deParseExpression(sqlStatement);
        } else {
            return this.deParseStatement(sqlStatement, buffer, settingsType);
        }
    }

    /**
     * Parses an expression.
     * @param sqlstatement current sql-statement
     * @return Expression object
     * @throws JSQLParserException if parsing goes wrong
     */
    private Expression parseExpression(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parseExpression(sqlstatement);
    }

    /**
     * Parses a statement.
     * @param sqlstatement current sql-statement
     * @return Statement object
     * @throws JSQLParserException if parsing goes wrong
     */
    private Statement parseStatement(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sqlstatement);
    }

    private boolean checkIfStatementTypeOfCreateDatabase(String sqlStatement) {
        return sqlStatement.contains("CREATE") && sqlStatement.contains("DATABASE");
    }
}
