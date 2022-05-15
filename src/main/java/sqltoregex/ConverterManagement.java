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
import org.springframework.stereotype.Service;
import sqltoregex.deparser.StatementDeParserForRegEx;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * realize a spring service for handling the converting process
 */
@Service
public class ConverterManagement {
    /**
     * Validate inserted SQL-statements for oracle, mysql, sqlserver, mariadb
     * return true or false, with a console output about the error messages
     * @param sqlstatement String
     * @return boolean
     */
    public boolean validate(String sqlstatement){
        List<DatabaseType> supportedDBMS = new ArrayList<>();
        supportedDBMS.add(DatabaseType.ORACLE);
        supportedDBMS.add(DatabaseType.MYSQL);
        supportedDBMS.add(DatabaseType.SQLSERVER);
        supportedDBMS.add(DatabaseType.MARIADB);

        Validation validation = new Validation(supportedDBMS, sqlstatement);
        List<ValidationError> validationerrors = validation.validate();
        if(validationerrors.isEmpty()) { return true;}
        else{
            for(ValidationError va : validationerrors){
                Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
                logger.log(Level.WARNING, "Something went wrong by adding a property to the map: {0}", va);
            }
            return false;
        }
    }

    private Statement parseStatement(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sqlstatement);
    }

    private Expression parseExpression(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parseExpression(sqlstatement);
    }

    /**
     * compared list elements about unique elements
     * @param list List<String>
     * @return boolean
     */
    private boolean isDistinctList(List<String> list){
        if(list.isEmpty()) throw new IllegalArgumentException("List should not be empty. One element is required.");
        Set<String> set = new HashSet<>(list);
        return (set.size() == list.size());
    }

    /**
     * generate string from deparser objekt, masked backslashes
     * @param statementDeParser StatementDeParser
     * @return maskedString - String
     */
    private String toMaskedStrings(StatementDeParser statementDeParser){
        return statementDeParser.getBuffer().toString().replace("\\", "\\\\");
    }

    private String toMaskedStrings(ExpressionDeParser expressionDeParser){
        return expressionDeParser.getBuffer().toString().replace("\\", "\\\\");
    }

    private String buildOutputRegex(String regex){
        StringBuilder outputRegex = new StringBuilder();
        return outputRegex.append("^").append(regex).append("$").toString();
    }

    /**
     * logical OR-concat, if the exprission-visitor detect an alternative statement
     * @param regexList List<String>
     * @return concatOutputString - String
     */
    private String buildOutputRegex(List<String> regexList){
        StringBuilder outputRegex = new StringBuilder();
        outputRegex.append("^");

        if(regexList.size() == 1 || !isDistinctList(regexList)){
            outputRegex.append(regexList.get(0));
        } else if(isDistinctList(regexList)){
            for(String str : regexList){
                outputRegex.append("(");
                outputRegex.append(str);
                outputRegex.append(")|");
            }
            outputRegex.replace(outputRegex.length() - 1, outputRegex.length(), "");
        }
        outputRegex.append("$");
        return outputRegex.toString();
    }

    /**
     * (de-)parsing the given statement
     * @param sqlStatement String
     * @param isOnlyExpression boolean
     * @return deparsed Statement as RegEx - String
     * @throws JSQLParserException is thrown if parsing goes wrong
     */
    public String deparse(String sqlStatement, boolean isOnlyExpression) throws JSQLParserException {
        return deparse(sqlStatement, isOnlyExpression, true);
    }

    /**
     * (de-)parsing the given statement
     * @param sqlStatement String
     * @param isOnlyExpression boolean
     * @param toBeValidated boolean
     * @return deparsed Statement as RegEx - String
     * @throws JSQLParserException is thrown if parsing goes wrong
     */
    public String deparse(String sqlStatement, boolean isOnlyExpression, boolean toBeValidated) throws JSQLParserException {
        Statement statement;
        Expression expression;

        StringBuilder buffer = new StringBuilder();

        if(isOnlyExpression){
            expression = this.parseExpression(sqlStatement);
            ExpressionVisitor expressionVisitor = new ExpressionVisitorAdapter();
            expression.accept(expressionVisitor);
            ExpressionDeParser expressionDeParser = new ExpressionDeParser();
            expression.accept(expressionDeParser);
            return this.buildOutputRegex(toMaskedStrings(expressionDeParser));
        } else {
            if(toBeValidated && !this.validate(sqlStatement)){
                throw new IllegalArgumentException();
            }
            statement = this.parseStatement(sqlStatement);
            StatementDeParser defaultStatementDeparser = new StatementDeParser(buffer);
            statement.accept(defaultStatementDeparser);
            String regExOne = toMaskedStrings(defaultStatementDeparser);
            ExpressionDeParser expressionDeParser = new ExpressionDeParser();
            StatementDeParser joinWhereStatementDeparser = new StatementDeParserForRegEx(expressionDeParser, buffer);
            String regExTwo = toMaskedStrings(joinWhereStatementDeparser);
            return this.buildOutputRegex(Arrays.asList(regExOne, regExTwo));
        }
    }
}
