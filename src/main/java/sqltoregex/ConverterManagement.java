package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sqltoregex.deparser.CreateDatabaseDeParserForRegEx;
import sqltoregex.deparser.ExpressionDeParserForRegEx;
import sqltoregex.deparser.SelectDeParserForRegEx;
import sqltoregex.deparser.StatementDeParserForRegEx;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsManager;
import sqltoregex.settings.SettingsType;
import sqltoregex.visitor.StatementVisitorJoinToWhere;
import sqltoregex.visitor.StatementVisitorKeyPlacement;
import static sqltoregex.deparser.StatementDeParserForRegEx.QUOTATION_MARK_REGEX;
import static sqltoregex.deparser.StatementDeParserForRegEx.OPTIONAL_WHITE_SPACE;

import java.util.*;

/**
 * Realizes a spring service for handling the converting process.
 */
@Service
public class ConverterManagement {
    private final SettingsManager settingsManager;
    private static final String NOT_QUOTATION_MARK_REGEX = "[^" + QUOTATION_MARK_REGEX.substring(1);

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
        return "^" + OPTIONAL_WHITE_SPACE + regex + OPTIONAL_WHITE_SPACE + ";?" + OPTIONAL_WHITE_SPACE + "$";
    }

    /**
     * Logical OR-concat, if the expression-visitor detect an alternative statement, to allow both of them in the regex, including optional semicolon.
     * @param regexList List of String with alternative regex
     * @return concated regex
     */
    private String buildOutputRegex(List<String> regexList) {
        StringBuilder outputRegex = new StringBuilder();
        outputRegex.append("^").append(OPTIONAL_WHITE_SPACE);

        Set<String> sqlRegExSet = new HashSet<>(regexList);

        if (sqlRegExSet.size() > 1){
            outputRegex.append("(?:");
        }
        Iterator<String> iterator = sqlRegExSet.iterator();
        while (iterator.hasNext()){
            String regEx = iterator.next();
            outputRegex.append(regEx);
            outputRegex.append(OPTIONAL_WHITE_SPACE).append(";?");
            if (iterator.hasNext()){
                outputRegex.append("|");
            }
        }
        if (sqlRegExSet.size() > 1){
            outputRegex.append(")");
        }

        outputRegex.append(OPTIONAL_WHITE_SPACE).append("$");
        return outputRegex.toString();
    }

    /**
     * Deparses expressions.
     * @param sqlStatement current sql statement
     * @return generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    private String deParseExpression(String sqlStatement, SettingsType settingsType) throws JSQLParserException {
        Expression expression;
        expression = CCJSqlParserUtil.parseExpression(sqlStatement);
        SettingsContainer settings = SettingsContainer.builder().with(settingsManager, settingsType);
        ExpressionDeParser expressionDeParser = new ExpressionDeParserForRegEx(new SelectDeParserForRegEx(
                settings), new StringBuilder(), settings);
        expression.accept(expressionDeParser);
        return this.buildOutputRegex(expressionDeParser.getBuffer().toString());
    }

    /**
     * Deparses statements.
     * @param sqlStatement current sql statement
     * @param settingsType {@link sqltoregex.settings.UserSettings} or presets
     * @return  generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    private String deParseStatement(String sqlStatement, SettingsType settingsType) throws JSQLParserException {
        if(isCreateDatabase(sqlStatement)) return new CreateDatabaseDeParserForRegEx(SettingsContainer.builder().with(settingsManager, settingsType)).deParse(sqlStatement);

        Statement statement;
        statement = CCJSqlParserUtil.parse(sqlStatement);
        List<String> regExList = new LinkedList<>();

        StatementDeParserForRegEx defaultStatementDeParser = new StatementDeParserForRegEx(new StringBuilder(), SettingsContainer.builder().with(settingsManager, settingsType));
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
        return this.deparse(sqlStatement, false, SettingsType.USER);
    }

    /**
     * Performs statement deparsing.
     * @param sqlStatement current sql statement
     * @param isOnlyExpression must be set true, if you only want to deparse an expression
     * @return generated regex
     * @throws JSQLParserException if parsing goes wrong
     */
    public String deparse(String sqlStatement, boolean isOnlyExpression) throws JSQLParserException {
        return this.deparse(sqlStatement, isOnlyExpression, SettingsType.USER);
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
        Set<String> statementSet = new HashSet<>(extractStatements(sqlStatement));
        StringBuilder buffer = new StringBuilder();
        Iterator<String> iterator = statementSet.iterator();
        while (iterator.hasNext()){
            if (isOnlyExpression) {
                buffer.append(this.deParseExpression(iterator.next(), settingsType));
            } else {
                buffer.append(this.deParseStatement(iterator.next(), settingsType));
            }
            if (iterator.hasNext()){
                buffer.append('|');
            }
        }
        return buffer.toString();
    }

    /**
     * Searches for ";" in the provided String and splits the Statements on ";" which are not enclosed in QuotationMarks
     * @param multStmtString string with multiple sql statements, delimited by ;
     * @see StatementDeParserForRegEx#QUOTATION_MARK_REGEX
     * @return collection with single statements
     */
    public static Collection<String> extractStatements(String multStmtString){
        Collection<String> stmtList = new LinkedList<>();
        int firstPos = 0;
        int secondPos = 0;
        String subBetweenSemis = "";

        while ((secondPos = multStmtString.indexOf(';', secondPos)) != -1) {
            subBetweenSemis = multStmtString.substring(firstPos, secondPos);
            //RegEx searches for even (incl. 0) number of QuotationMarks "(([^']*'[^']*'[^']*)|[^'])*"
            if (!subBetweenSemis.matches("((" + NOT_QUOTATION_MARK_REGEX +"*"
                                                 + QUOTATION_MARK_REGEX + NOT_QUOTATION_MARK_REGEX +"*"
                                                 + QUOTATION_MARK_REGEX + NOT_QUOTATION_MARK_REGEX +"*)|"
                                                 + NOT_QUOTATION_MARK_REGEX +")*")){
                secondPos++;
                continue;
            }

            firstPos = ++secondPos;
            subBetweenSemis = subBetweenSemis.replace("\r", "").replace("\n", "").trim();
            if(!subBetweenSemis.isEmpty()) stmtList.add(subBetweenSemis);
        }

        if (!multStmtString.endsWith(";") && !multStmtString.substring(firstPos).replace("\r", "").replace("\n", "").trim().isEmpty()){
            stmtList.add(multStmtString.substring(firstPos));
        }
        return stmtList;
    }

    /**
     * Check if the given statement is a special case like create database.
     * @param sqlStatement given sql statement
     * @return is create database (true) or not (false)
     */
    private boolean isCreateDatabase(String sqlStatement) {
        return sqlStatement.contains("CREATE") && sqlStatement.contains("DATABASE");
    }
}
