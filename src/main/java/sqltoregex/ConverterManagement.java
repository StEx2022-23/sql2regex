package sqltoregex;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.springframework.stereotype.Service;
import sqltoregex.deparser.StatementDeparser;

import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * realize a spring service for handling the converting process
 */
@Service
public class ConverterManagement {
    public ConverterManagement(){}

    /**
     * Validate inserted SQL-statements for oracle, mysql, sqlserver, mariadb
     * return true or false, with a console output about the error messages
     * @param sqlstatement String
     * @return Boolean
     */
    public Boolean validate(String sqlstatement){
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.ALL);
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        List<DatabaseType> supportedDBMS = new ArrayList<>();
        supportedDBMS.add(DatabaseType.ORACLE);
        supportedDBMS.add(DatabaseType.MYSQL);
        supportedDBMS.add(DatabaseType.SQLSERVER);
        supportedDBMS.add(DatabaseType.MARIADB);

        Validation validation = new Validation(supportedDBMS, sqlstatement);
        List<ValidationError> validationerrors = validation.validate();
        if(validationerrors.size() == 0) { return true;}
        else{
            for(ValidationError va : validationerrors){
                logger.warning(va.toString());
            }
            return false;
        }
    }

    private Statement parse(String sqlstatement) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sqlstatement);
    }

    /**
     * compared list elements about unique elements
     * @param list List<String>
     * @return boolean
     */
    private Boolean checkOfDistinctElements(List<String> list){
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

    /**
     * logical OR-concat, if the exprission-visitor detect an alternative statement
     * @param regexList List<String>
     * @return concatOutputString - String
     */
    private String buildOutputRegex(List<String> regexList){
        StringBuilder outputRegex = new StringBuilder();
        outputRegex.append("^");

        if(checkOfDistinctElements(regexList)){
            outputRegex.append(regexList.get(0));
        } else if(!checkOfDistinctElements(regexList)){
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
     * @param sqlstatement String
     * @return deparsed Statement as RegEx - String
     * @throws JSQLParserException is thrown if parsing goes wrong
     */
    public String deparse(String sqlstatement) throws JSQLParserException {
        Statement statement = this.parse(sqlstatement);
        StringBuilder buffer = new StringBuilder();

        StatementDeParser defaultStatementDeparser = new StatementDeParser(buffer);
        statement.accept(defaultStatementDeparser);
        String regExOne = toMaskedStrings(defaultStatementDeparser);

        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        StatementDeParser joinWhereStatementDeparser = new StatementDeparser(expressionDeParser, buffer);
        String regExTwo = toMaskedStrings(joinWhereStatementDeparser);

        return this.buildOutputRegex(Arrays.asList(regExOne, regExTwo));
    }
}
