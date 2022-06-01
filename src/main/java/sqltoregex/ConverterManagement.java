package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.deparser.StatementDeParserForRegEx;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsManager;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * realize a spring service for handling the converting process
 */
@Service
public class ConverterManagement {

    private final SettingsManager settingsManager;

    /**
     * Validate inserted SQL-statements for oracle, mysql, sqlserver, mariadb
     * return true or false, with a console output about the error messages
     *
     * @return boolean
     */

    @Autowired
    public ConverterManagement(SettingsManager settingsManager) {
        Assert.notNull(settingsManager, "Settings manager must not be null");
        this.settingsManager = settingsManager;
    }

    private String buildOutputRegex(String regex) {
        return "^" + regex + "$";
    }

    /**
     * logical OR-concat, if the exprission-visitor detect an alternative statement
     *
     * @param regexList List<String>
     * @return concatOutputString - String
     */
    private String buildOutputRegex(List<String> regexList) {
        StringBuilder outputRegex = new StringBuilder();
        outputRegex.append("^");

        if (regexList.size() == 1 || !isDistinctList(regexList)) {
            outputRegex.append(regexList.get(0));
        } else if (isDistinctList(regexList)) {
            for (String str : regexList) {
                outputRegex.append("(");
                outputRegex.append(str);
                outputRegex.append(")|");
            }
            outputRegex.replace(outputRegex.length() - 1, outputRegex.length(), "");
        }
        outputRegex.append("$");
        return outputRegex.toString();
    }

    private String deParseExpression(String sqlstatement) throws JSQLParserException {
        Expression expression;
        expression = this.parseExpression(sqlstatement);
        ExpressionVisitor expressionVisitor = new ExpressionVisitorAdapter();
        expression.accept(expressionVisitor);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        expression.accept(expressionDeParser);
        return this.buildOutputRegex(expressionDeParser.getBuffer().toString());
    }

    private String deParseStatement(String sqlStatement, StringBuilder buffer) throws JSQLParserException {
        Statement statement;
        statement = this.parseStatement(sqlStatement);
        StatementDeParserForRegEx defaultStatementDeParser = new StatementDeParserForRegEx(buffer, new SettingsContainer().withSettingsManager(settingsManager));
        statement.accept(defaultStatementDeParser);
        String regExOne = defaultStatementDeParser.getBuffer().toString();
        ExpressionDeParserForRegEx expressionDeParser = new ExpressionDeParserForRegEx(new SettingsContainer().withSettingsManager(settingsManager));
        StatementDeParser joinWhereStatementDeParser = new StatementDeParserForRegEx(expressionDeParser, buffer,
                                                                                     new SettingsContainer().withSettingsManager(settingsManager));
        String regExTwo = joinWhereStatementDeParser.getBuffer().toString();
        return this.buildOutputRegex(Arrays.asList(regExOne, regExTwo));
    }

    /**
     * (de-)parsing the given statement
     *
     * @param sqlStatement String
     * @return deparsed Statement as RegEx - String
     * @throws JSQLParserException is thrown if parsing goes wrong
     */
    public String deparse(String sqlStatement) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        if (!this.validate(sqlStatement)) {
            throw new IllegalArgumentException();
        }
        return this.deParseStatement(sqlStatement, buffer);
    }

    public String deparse(String sqlStatement, boolean isOnlyExpression) throws JSQLParserException {
        return deparse(sqlStatement, isOnlyExpression, true);
    }

    public String deparse(String sqlStatement, boolean isOnlyExpression,
                          boolean toBeValidated) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        if (isOnlyExpression) {
            return this.deParseExpression(sqlStatement);
        } else {
            if (toBeValidated && !this.validate(sqlStatement)) {
                throw new IllegalArgumentException();
            }
            return this.deParseStatement(sqlStatement, buffer);
        }
    }

    /**
     * compared list elements about unique elements
     *
     * @param list List<String>
     * @return boolean
     */
    private boolean isDistinctList(List<String> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("List should not be empty. One element is required.");
        Set<String> set = new HashSet<>(list);
        return (set.size() == list.size());
    }

    private Expression parseExpression(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parseExpression(sqlstatement);
    }

    private Statement parseStatement(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sqlstatement);
    }

    public boolean validate(String sqlstatement) {
        List<DatabaseType> supportedDBMS = new ArrayList<>();
        supportedDBMS.add(DatabaseType.ORACLE);
        supportedDBMS.add(DatabaseType.MYSQL);
        supportedDBMS.add(DatabaseType.SQLSERVER);
        supportedDBMS.add(DatabaseType.MARIADB);

        Validation validation = new Validation(supportedDBMS, sqlstatement);
        List<ValidationError> validationErrors = validation.validate();
        if (validationErrors.isEmpty()) {
            return true;
        } else {
            for (ValidationError va : validationErrors) {
                Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                logger.log(Level.WARNING, "Error while validating the statement: {0}", va);
            }
            return false;
        }
    }
}
