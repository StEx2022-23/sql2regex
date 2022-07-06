package sqltoregex.deparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import sqltoregex.settings.SettingsContainer;
import sqltoregex.settings.SettingsOption;
import sqltoregex.settings.regexgenerator.SpellingMistake;
import sqltoregex.settings.regexgenerator.synonymgenerator.DateAndTimeFormatSynonymGenerator;
import sqltoregex.settings.regexgenerator.synonymgenerator.StringSynonymGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static sqltoregex.deparser.StatementDeParserForRegEx.QUOTATION_MARK_REGEX;
import static sqltoregex.deparser.StatementDeParserForRegEx.REQUIRED_WHITE_SPACE;
import static sqltoregex.deparser.StatementDeParserForRegEx.OPTIONAL_WHITE_SPACE;

/**
 * Implements an own {@link ExpressionDeParser} for generating RegEx.
 * Therefore, overwriting every visit method with RegEx String generation
 */
public class ExpressionDeParserForRegEx extends ExpressionDeParser {
    public static final String NOT = "NOT";
    public static final String OLD_ORACLE_JOIN = "\\(\\+\\)";
    private final SpellingMistake columnNameSpellingMistake;
    private final SpellingMistake tableNameSpellingMistake;
    private final SpellingMistake stringValueSpellingMistake;
    private final DateAndTimeFormatSynonymGenerator dateSynonyms;
    private final DateAndTimeFormatSynonymGenerator timeStampSynonyms;
    private final DateAndTimeFormatSynonymGenerator timeSynonyms;
    private final StringSynonymGenerator otherSynonyms;
    private final StringSynonymGenerator aggregateFunctionLang;
    private final OrderByDeParserForRegEx orderByDeParser;
    private Map<String, String> tableNameAliasMap;

    /**
     * Short constructor.
     * @param settingsContainer containing all deparse Settings
     */
    public ExpressionDeParserForRegEx(SettingsContainer settingsContainer) {
        this(new SelectDeParserForRegEx(settingsContainer), new StringBuilder(), settingsContainer);
    }

    /**
     * Extended constructor.
     * @param selectVisitor visitor used for deparsing sub selects
     * @param buffer used for string building
     * @param settingsContainer containing all deparse Settings
     */
    public ExpressionDeParserForRegEx(SelectVisitor selectVisitor, StringBuilder buffer,
                                      SettingsContainer settingsContainer) {
        this(selectVisitor, buffer, new OrderByDeParserForRegEx(settingsContainer), settingsContainer);
    }

    /**
     * Detailed constructor.
     * @param selectVisitor visitor used for deparsing sub selects
     * @param buffer used for string building
     * @param orderByDeParser visitor for deparsing order by statements
     * @param settings containing all deparse Settings
     */
    ExpressionDeParserForRegEx(SelectVisitor selectVisitor, StringBuilder buffer,
                               OrderByDeParserForRegEx orderByDeParser, SettingsContainer settings) {
        super(selectVisitor, buffer);
        this.orderByDeParser = orderByDeParser;
        this.columnNameSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.COLUMNNAMESPELLING);
        this.tableNameSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.TABLENAMESPELLING);
        this.stringValueSpellingMistake = settings.get(SpellingMistake.class).get(SettingsOption.STRINGVALUESPELLING);
        this.dateSynonyms = settings.get(DateAndTimeFormatSynonymGenerator.class).get(SettingsOption.DATESYNONYMS);
        this.timeSynonyms = settings.get(DateAndTimeFormatSynonymGenerator.class).get(SettingsOption.TIMESYNONYMS);
        this.timeStampSynonyms = settings.get(DateAndTimeFormatSynonymGenerator.class).get(SettingsOption.DATETIMESYNONYMS);
        this.aggregateFunctionLang = settings.get(StringSynonymGenerator.class).get(SettingsOption.AGGREGATEFUNCTIONLANG);
        this.otherSynonyms = settings.get(StringSynonymGenerator.class).get(SettingsOption.OTHERSYNONYMS);
        this.tableNameAliasMap = new HashMap<>();
    }

    @Override
    public void visit(Addition addition) {
        visitCommutativeBinaryExpression(addition, OPTIONAL_WHITE_SPACE + "\\+" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitCommutativeBinaryExpression(andExpression,
                                         OPTIONAL_WHITE_SPACE
                                                 + StringSynonymGenerator.useOrDefault(this.otherSynonyms,
                                                               andExpression.isUseOperator() ? "&&" : "AND")
                                                 + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Between between) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        between.getLeftExpression().accept(this);
        if (between.isNot()) {
            buffer.append(OPTIONAL_WHITE_SPACE + "NOT");
        }

        buffer.append(OPTIONAL_WHITE_SPACE + "BETWEEN" + OPTIONAL_WHITE_SPACE);
        between.getBetweenExpressionStart().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE + "AND" + OPTIONAL_WHITE_SPACE);
        between.getBetweenExpressionEnd().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        buffer.append("(?:");
        visitOldOracleJoinBinaryExpression(equalsTo, OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        visitOldOracleJoinBinaryExpression(
                new EqualsTo()
                        .withLeftExpression(equalsTo.getRightExpression())
                        .withRightExpression(equalsTo.getLeftExpression())
                        .withOldOracleJoinSyntax(
                                (equalsTo.getOldOracleJoinSyntax() + equalsTo.getOldOracleJoinSyntax()) % 3)
                        .withOraclePriorPosition(equalsTo.getOraclePriorPosition())
                , OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);
        if (equalsTo.getRightExpression().toString().equalsIgnoreCase("true")){
            buffer.append('|');
            equalsTo.getLeftExpression().accept(this);
        }
        buffer.append(")");
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division, OPTIONAL_WHITE_SPACE + "/" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(IntegerDivision division) {
        visitBinaryExpression(division, OPTIONAL_WHITE_SPACE + "DIV" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(
                doubleValue.toString().replace(
                        ".",
                        StringSynonymGenerator.useOrDefault(this.otherSynonyms, ".")
                )
        );
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(HexValue hexValue) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(hexValue.toString());
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(NotExpression notExpr) {
        buffer.append("(?:");
        buffer.append(OPTIONAL_WHITE_SPACE)
                .append(StringSynonymGenerator.useOrDefault(this.otherSynonyms, "NOT"))
                .append(OPTIONAL_WHITE_SPACE).append(")");
        notExpr.getExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(BitwiseRightShift expr) {
        visitBinaryExpression(expr, OPTIONAL_WHITE_SPACE + ">>" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(BitwiseLeftShift expr) {
        visitBinaryExpression(expr, OPTIONAL_WHITE_SPACE + "<<" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        buffer.append("(?:");
        visitOldOracleJoinBinaryExpression(greaterThan, OPTIONAL_WHITE_SPACE + ">" + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        visitOldOracleJoinBinaryExpression(new MinorThan()
                                                   .withLeftExpression(greaterThan.getRightExpression())
                                                   .withRightExpression(greaterThan.getLeftExpression())
                                                   .withOldOracleJoinSyntax(greaterThan.getOldOracleJoinSyntax())
                                                   .withOraclePriorPosition(greaterThan.getOraclePriorPosition())
                , OPTIONAL_WHITE_SPACE + "<" + OPTIONAL_WHITE_SPACE);
        buffer.append(")");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        buffer.append("(?:");
        visitOldOracleJoinBinaryExpression(greaterThanEquals, OPTIONAL_WHITE_SPACE + ">=" + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        visitOldOracleJoinBinaryExpression(new GreaterThanEquals()
                                                   .withLeftExpression(greaterThanEquals.getRightExpression())
                                                   .withRightExpression(greaterThanEquals.getLeftExpression())
                                                   .withOldOracleJoinSyntax(greaterThanEquals.getOldOracleJoinSyntax())
                                                   .withOraclePriorPosition(greaterThanEquals.getOraclePriorPosition())
                , OPTIONAL_WHITE_SPACE + "<=" + OPTIONAL_WHITE_SPACE);
        buffer.append(")");
    }

    @Override
    public void visit(InExpression inExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        inExpression.getLeftExpression().accept(this);
        if (inExpression.getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT) {
            buffer.append(OLD_ORACLE_JOIN + OPTIONAL_WHITE_SPACE);
        }
        if (inExpression.isNot()) {
            buffer.append(OPTIONAL_WHITE_SPACE + "NOT");
        }
        buffer.append(OPTIONAL_WHITE_SPACE + "IN" + OPTIONAL_WHITE_SPACE);
        if (inExpression.getRightExpression() != null) {
            inExpression.getRightExpression().accept(this);
        } else {
            inExpression.getRightItemsList().accept(this);
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        // Build a list of matched columns
        StringBuilder columnsListCommaSeperated = new StringBuilder();
        Iterator<Column> iterator = fullTextSearch.getMatchColumns().iterator();
        while (iterator.hasNext()) {
            Column col = iterator.next();
            columnsListCommaSeperated.append(col.getFullyQualifiedName());
            if (iterator.hasNext()) {
                columnsListCommaSeperated.append(OPTIONAL_WHITE_SPACE + "," + OPTIONAL_WHITE_SPACE);
            }
        }
        buffer.append("MATCH" + OPTIONAL_WHITE_SPACE + "\\(" + OPTIONAL_WHITE_SPACE)
                .append(columnsListCommaSeperated).append(OPTIONAL_WHITE_SPACE).append("\\)")
                .append(REQUIRED_WHITE_SPACE)
                .append("AGAINST").append(OPTIONAL_WHITE_SPACE).append("\\(").append(OPTIONAL_WHITE_SPACE)
                .append("['|\"]")
                .append(fullTextSearch.getAgainstValue().toString(), 1,
                        fullTextSearch.getAgainstValue().toString().length() - 1)
                .append("['|\"]")
                .append(fullTextSearch.getSearchModifier() != null ?
                                OPTIONAL_WHITE_SPACE + fullTextSearch.getSearchModifier() : "")
                .append(OPTIONAL_WHITE_SPACE).append("\\)")
                .append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        buffer.append(signedExpression.getSign());
        buffer.append(OPTIONAL_WHITE_SPACE);
        signedExpression.getExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        isNullExpression.getLeftExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("(?:");
        if (isNullExpression.isNot()) {
            buffer.append("NOT" + REQUIRED_WHITE_SPACE + "IS" + OPTIONAL_WHITE_SPACE + "NULL");
        } else {
            buffer.append("IS" + OPTIONAL_WHITE_SPACE + "NULL");
        }
        buffer.append("|");
        buffer.append("IS" + REQUIRED_WHITE_SPACE);
        if (isNullExpression.isNot()) {
            buffer.append("NOT");
        }
        buffer.append(REQUIRED_WHITE_SPACE);
        buffer.append("NULL");
        buffer.append(")");
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        isBooleanExpression.getLeftExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (isBooleanExpression.isTrue()) {
            buffer.append("IS" + REQUIRED_WHITE_SPACE);
            if (isBooleanExpression.isNot()) {
                buffer.append("NOT" + REQUIRED_WHITE_SPACE);
            }
            buffer.append("TRUE");
        } else {
            buffer.append("IS" + REQUIRED_WHITE_SPACE);
            if (isBooleanExpression.isNot()) {
                buffer.append("NOT" + REQUIRED_WHITE_SPACE);
            }
            buffer.append("FALSE");
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("?");
        if (jdbcParameter.isUseFixedIndex()) {
            buffer.append(jdbcParameter.getIndex());
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        visitBinaryExpression(likeExpression,
                              (likeExpression.isNot() ? "NOT" + REQUIRED_WHITE_SPACE : "") + (likeExpression.isCaseInsensitive() ? "ILIKE" : "LIKE") + OPTIONAL_WHITE_SPACE);
        Expression escape = likeExpression.getEscape();
        if (escape != null) {
            buffer.append(OPTIONAL_WHITE_SPACE);
            buffer.append("ESCAPE");
            buffer.append(OPTIONAL_WHITE_SPACE);
            likeExpression.getEscape().accept(this);
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (existsExpression.isNot()) {
            buffer.append("NOT").append(REQUIRED_WHITE_SPACE);
        }
        buffer.append("EXISTS");
        buffer.append(OPTIONAL_WHITE_SPACE);
        existsExpression.getRightExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(LongValue longValue) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(longValue.getStringValue());
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(MinorThan minorThan) {
        buffer.append("(?:");
        visitOldOracleJoinBinaryExpression(minorThan, OPTIONAL_WHITE_SPACE + "<" + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        visitOldOracleJoinBinaryExpression(new GreaterThan()
                                                   .withLeftExpression(minorThan.getRightExpression())
                                                   .withRightExpression(minorThan.getLeftExpression())
                                                   .withOldOracleJoinSyntax(minorThan.getOldOracleJoinSyntax())
                                                   .withOraclePriorPosition(minorThan.getOraclePriorPosition())
                , OPTIONAL_WHITE_SPACE + ">" + OPTIONAL_WHITE_SPACE);
        buffer.append(")");
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        buffer.append("(?:");
        visitOldOracleJoinBinaryExpression(minorThanEquals, OPTIONAL_WHITE_SPACE + "<=" + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        visitOldOracleJoinBinaryExpression(new GreaterThanEquals()
                                                   .withLeftExpression(minorThanEquals.getRightExpression())
                                                   .withRightExpression(minorThanEquals.getLeftExpression())
                                                   .withOldOracleJoinSyntax(minorThanEquals.getOldOracleJoinSyntax())
                                                   .withOraclePriorPosition(minorThanEquals.getOraclePriorPosition())
                , OPTIONAL_WHITE_SPACE + ">=" + OPTIONAL_WHITE_SPACE);
        buffer.append(")");
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitCommutativeBinaryExpression(multiplication, OPTIONAL_WHITE_SPACE + "\\*" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        buffer.append("(?:");
        visitOldOracleJoinBinaryExpression(notEqualsTo,
                                           OPTIONAL_WHITE_SPACE + notEqualsTo.getStringExpression() + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        visitOldOracleJoinBinaryExpression(new NotEqualsTo()
                                                   .withLeftExpression(notEqualsTo.getRightExpression())
                                                   .withRightExpression(notEqualsTo.getLeftExpression())
                                                   .withOldOracleJoinSyntax(
                                                           (notEqualsTo.getOldOracleJoinSyntax() + notEqualsTo.getOldOracleJoinSyntax()) % 3)
                                                   .withOraclePriorPosition(notEqualsTo.getOraclePriorPosition())
                , OPTIONAL_WHITE_SPACE + notEqualsTo.getStringExpression() + OPTIONAL_WHITE_SPACE);
        buffer.append(")");
    }

    @Override
    public void visit(NullValue nullValue) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(nullValue.toString());
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitCommutativeBinaryExpression(orExpression, OPTIONAL_WHITE_SPACE + "OR" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(XorExpression xorExpression) {
        visitCommutativeBinaryExpression(xorExpression, OPTIONAL_WHITE_SPACE + "XOR" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("\\(");
        parenthesis.getExpression().accept(this);
        buffer.append("\\)");
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(StringValue stringValue) {
        if (stringValue.getPrefix() != null) {
            buffer.append(stringValue.getPrefix());
        }

        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(
                StatementDeParserForRegEx.addQuotationMarks(
                        SpellingMistake.useOrDefault(
                                this.stringValueSpellingMistake,
                                stringValue.getValue()
                        )
                )
        );
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction, OPTIONAL_WHITE_SPACE + "-" + OPTIONAL_WHITE_SPACE);
        buffer.append('|');
        buffer.append('-');
        visitBinaryExpression(new Addition()
                                      .withLeftExpression(subtraction.getLeftExpression())
                                      .withRightExpression(subtraction.getRightExpression())
                , "+");
    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.isUseBrackets()) {
            buffer.append("\\(");
        }
        if (this.getSelectVisitor() != null) {
            if (subSelect.getWithItemsList() != null) {
                buffer.append("WITH" + REQUIRED_WHITE_SPACE);
                for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext(); ) {
                    iter.next().accept(this.getSelectVisitor());
                    if (iter.hasNext()) {
                        buffer.append("," + OPTIONAL_WHITE_SPACE);
                    }
                    buffer.append(REQUIRED_WHITE_SPACE);
                }
                buffer.append(REQUIRED_WHITE_SPACE);
            }
            subSelect.getSelectBody().accept(this.getSelectVisitor());

        }
        if (subSelect.isUseBrackets()) {
            buffer.append("\\)");
        }
    }

    @Override
    public void visit(Column tableColumn) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        final Table table = tableColumn.getTable();
        String tableName = null;
        if (table != null) {
            if (table.getAlias() != null) {
                tableName = table.getAlias().toString().replaceAll(QUOTATION_MARK_REGEX, "");
            }else{
                tableName = table.getFullyQualifiedName().replaceAll(QUOTATION_MARK_REGEX, "");
            }

            StringBuilder tableNameWithAlias = new StringBuilder();
            deparseTableName(tableName, tableNameWithAlias);
            buffer.append(tableNameWithAlias);
        }

        buffer.append(
                StatementDeParserForRegEx.addQuotationMarks(
                    SpellingMistake.useOrDefault(
                            this.columnNameSpellingMistake,
                            tableColumn.getColumnName().replaceAll(QUOTATION_MARK_REGEX, "")
                     )
                )
        );
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    private void deparseTableName(String tableName, StringBuilder tableNameWithAlias) {
        if(this.getRelatedTableNameOrAlias(tableName) == null){
            tableNameWithAlias.append("(");
            tableNameWithAlias.append(
                    StatementDeParserForRegEx.addQuotationMarks(
                            SpellingMistake.useOrDefault(
                                    this.tableNameSpellingMistake,
                                    tableName.replaceAll(QUOTATION_MARK_REGEX, "")
                            )
                    )
            );
            tableNameWithAlias.append(")?");
        } else {
            tableNameWithAlias.append("(?:");
            tableNameWithAlias.append(
                    StatementDeParserForRegEx.addQuotationMarks(
                            SpellingMistake.useOrDefault(
                                    this.tableNameSpellingMistake,
                                    tableName.replaceAll(QUOTATION_MARK_REGEX, "")
                            )
                    )
            );
            tableNameWithAlias.append("|");
            tableNameWithAlias.append(
                    StatementDeParserForRegEx.addQuotationMarks(
                            SpellingMistake.useOrDefault(
                                    this.tableNameSpellingMistake,
                                    this.getRelatedTableNameOrAlias(tableName)
                            )
                    )
            );
            tableNameWithAlias.append(")?");
        }
        if (!tableName.isEmpty()) {
            tableNameWithAlias.append("\\.?");
        }
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void visit(Function function) {
        if (function.isEscaped()) {
            buffer.append("\\{fn ");
        }

        buffer.append(StringSynonymGenerator.useOrDefault(this.aggregateFunctionLang, function.getName()));
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (function.getParameters() == null && function.getNamedParameters() == null) {
            buffer.append("\\(\\)");
        } else {
            buffer.append("\\(").append(OPTIONAL_WHITE_SPACE);
            if (function.isDistinct()) {
                buffer.append("DISTINCT").append(REQUIRED_WHITE_SPACE);
            } else if (function.isAllColumns()) {
                buffer.append("ALL").append(REQUIRED_WHITE_SPACE);
            } else if (function.isUnique()) {
                buffer.append("UNIQUE").append(REQUIRED_WHITE_SPACE);
            }
            if (function.getNamedParameters() != null) {
                visit(function.getNamedParameters());
            }
            if (function.getParameters() != null) {
                Iterator<Expression> expressionIterator = function.getParameters().getExpressions().iterator();
                while(expressionIterator.hasNext()){
                    String singleExpression = expressionIterator.next().toString();
                    StringBuilder tableNameWithAlias = new StringBuilder();
                    if(singleExpression.contains(".")){
                        String tableName = singleExpression.split("\\.")[0];
                        deparseTableName(tableName, tableNameWithAlias);
                        buffer.append(tableNameWithAlias);

                    }
                    String[] columnName = singleExpression.split("\\.");
                    buffer.append(tableNameWithAlias);
                    buffer.append(
                            StatementDeParserForRegEx.addQuotationMarks(
                                    SpellingMistake.useOrDefault(
                                            columnNameSpellingMistake,
                                            columnName[columnName.length - 1].replaceAll(QUOTATION_MARK_REGEX, "")
                                    ).replace("*", "\\*")
                            )
                    );

                    if(expressionIterator.hasNext()){
                        buffer.append(OPTIONAL_WHITE_SPACE).append(",").append(OPTIONAL_WHITE_SPACE);
                    }
                }
            }
            if (function.getOrderByElements() != null) {
                buffer.append(REQUIRED_WHITE_SPACE).append("ORDER").append(REQUIRED_WHITE_SPACE).append("BY").append(REQUIRED_WHITE_SPACE);
                boolean comma = false;
                orderByDeParser.setExpressionDeParserForRegEx(this);
                orderByDeParser.setBuffer(buffer);
                for (OrderByElement orderByElement : function.getOrderByElements()) {
                    if (comma) {
                        buffer.append(OPTIONAL_WHITE_SPACE).append(",").append(OPTIONAL_WHITE_SPACE);
                    } else {
                        comma = true;
                    }
                    orderByDeParser.deParseElement(orderByElement);
                }
            }
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\)");
        }

        if (function.getAttribute() != null) {
            buffer.append(".").append(function.getAttribute());
        } else if (function.getAttributeName() != null) {
            buffer.append(".").append(function.getAttributeName());
        }
        if (function.getKeep() != null) {
            buffer.append(REQUIRED_WHITE_SPACE).append(function.getKeep());
        }

        if (function.isEscaped()) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("\\}").append(OPTIONAL_WHITE_SPACE);
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        if (expressionList.isUsingBrackets()) {
            buffer.append("(").append(OPTIONAL_WHITE_SPACE);
        }
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
            Expression expression = iter.next();
            expression.accept(this);
            if (iter.hasNext()) {
                buffer.append(",").append(OPTIONAL_WHITE_SPACE);
            }
        }
        if (expressionList.isUsingBrackets()) {
            buffer.append(OPTIONAL_WHITE_SPACE).append(")");
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        List<String> names = namedExpressionList.getNames();
        List<Expression> expressions = namedExpressionList.getExpressions();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                buffer.append(REQUIRED_WHITE_SPACE);
            }
            String name = names.get(i);
            if (!name.equals("")) {
                buffer.append(name);
                buffer.append(REQUIRED_WHITE_SPACE);
            }
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(DateValue dateValue) {
        buffer.append("(?:")
                .append("\\{d").append(OPTIONAL_WHITE_SPACE).append("'").append(dateValue.getValue().toString())
                .append("'").append(OPTIONAL_WHITE_SPACE).append("\\}")
                .append('|')
                .append("#?")
                .append(DateAndTimeFormatSynonymGenerator.useOrDefault(this.dateSynonyms, dateValue))
                .append("#?")
                .append(")");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        buffer.append("(?:")
                .append("\\{ts").append(OPTIONAL_WHITE_SPACE).append("'").append(timestampValue.getValue().toString())
                .append(OPTIONAL_WHITE_SPACE).append("\\}")
                .append('|')
                .append("#?")
                .append(DateAndTimeFormatSynonymGenerator.useOrDefault(this.timeStampSynonyms, timestampValue))
                .append("#?")
                .append(")");
    }

    @Override
    public void visit(TimeValue timeValue) {
        buffer.append("(?:")
                .append("\\{t").append(OPTIONAL_WHITE_SPACE).append("'").append(timeValue.getValue().toString())
                .append(OPTIONAL_WHITE_SPACE).append("\\}")
                .append('|')
                .append("#?")
                .append(DateAndTimeFormatSynonymGenerator.useOrDefault(this.timeSynonyms, timeValue))
                .append("#?")
                .append(")");
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        buffer.append(caseExpression.isUsingBrackets() ? "(" : "").append(OPTIONAL_WHITE_SPACE).append("CASE")
                .append(OPTIONAL_WHITE_SPACE);
        Expression switchExp = caseExpression.getSwitchExpression();
        if (switchExp != null) {
            switchExp.accept(this);
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        for (Expression exp : caseExpression.getWhenClauses()) {
            exp.accept(this);
        }

        Expression elseExp = caseExpression.getElseExpression();
        if (elseExp != null) {
            buffer.append("ELSE").append(OPTIONAL_WHITE_SPACE);
            elseExp.accept(this);
            buffer.append(OPTIONAL_WHITE_SPACE);
        }

        buffer.append("END").append(caseExpression.isUsingBrackets() ? OPTIONAL_WHITE_SPACE + ")" : "");
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(WhenClause whenClause) {
        buffer.append("WHEN").append(OPTIONAL_WHITE_SPACE);
        whenClause.getWhenExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE).append("THEN").append(OPTIONAL_WHITE_SPACE);
        whenClause.getThenExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        buffer.append(anyComparisonExpression.getAnyType().name()).append(OPTIONAL_WHITE_SPACE).append("(")
                .append(OPTIONAL_WHITE_SPACE);
        SubSelect subSelect = anyComparisonExpression.getSubSelect();
        if (subSelect != null) {
            subSelect.accept((ExpressionVisitor) this);
        } else {
            ExpressionList expressionList = (ExpressionList) anyComparisonExpression.getItemsList();
            buffer.append("VALUES").append(OPTIONAL_WHITE_SPACE);
            buffer.append(
                    PlainSelect.getStringList(expressionList.getExpressions(), true,
                                              anyComparisonExpression.isUsingBracketsForValues()));
        }
        buffer.append(OPTIONAL_WHITE_SPACE).append(")").append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat, OPTIONAL_WHITE_SPACE + "||" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Matches matches) {
        visitOldOracleJoinBinaryExpression(matches, OPTIONAL_WHITE_SPACE + "@@" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd, OPTIONAL_WHITE_SPACE + "&" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr, OPTIONAL_WHITE_SPACE + "|" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor, OPTIONAL_WHITE_SPACE + "^" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(CastExpression cast) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (cast.isUseCastKeyword()) {
            buffer.append("CAST(");
            cast.getLeftExpression().accept(this);
            buffer.append(OPTIONAL_WHITE_SPACE).append("AS").append(OPTIONAL_WHITE_SPACE);
            buffer.append(cast.getRowConstructor() != null ? cast.getRowConstructor() : cast.getType());
            buffer.append(")");
        } else {
            cast.getLeftExpression().accept(this);
            buffer.append("::");
            buffer.append(cast.getType());
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(TryCastExpression cast) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        if (cast.isUseCastKeyword()) {
            buffer.append("TRY_CAST(");
            cast.getLeftExpression().accept(this);
            buffer.append(OPTIONAL_WHITE_SPACE).append("AS").append(OPTIONAL_WHITE_SPACE);
            buffer.append(cast.getRowConstructor() != null ? cast.getRowConstructor() : cast.getType());
            buffer.append(")");
        } else {
            cast.getLeftExpression().accept(this);
            buffer.append("::");
            buffer.append(cast.getType());
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo, OPTIONAL_WHITE_SPACE + "%" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append("EXTRACT(").append(OPTIONAL_WHITE_SPACE).append(eexpr.getName());
        buffer.append(OPTIONAL_WHITE_SPACE).append("FROM").append(OPTIONAL_WHITE_SPACE);
        eexpr.getExpression().accept(this);
        buffer.append(')');
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        for (Iterator<ExpressionList> it = multiExprList.getExpressionLists().iterator(); it.hasNext(); ) {
            it.next().accept(this);
            if (it.hasNext()) {
                buffer.append(",").append(OPTIONAL_WHITE_SPACE);
            }
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(iexpr);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(jdbcNamedParameter);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(oexpr);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr, OPTIONAL_WHITE_SPACE + rexpr.getStringExpression() + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr) {
        visitBinaryExpression(rexpr, OPTIONAL_WHITE_SPACE + rexpr.getStringExpression() + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(jsonExpr);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        visitBinaryExpression(jsonExpr, OPTIONAL_WHITE_SPACE + jsonExpr.getStringExpression() + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(UserVariable userVariable) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(userVariable);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(NumericBind bind) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(bind);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(KeepExpression aexpr) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(aexpr);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(groupConcat);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(ValueListExpression valueList) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(valueList);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        if (rowConstructor.getName() != null) {
            buffer.append(rowConstructor.getName());
        }
        buffer.append("(");

        if (rowConstructor.getColumnDefinitions().isEmpty()) {
            buffer.append("(");
            int i = 0;
            for (ColumnDefinition columnDefinition : rowConstructor.getColumnDefinitions()) {
                buffer.append(i > 0 ? "," + OPTIONAL_WHITE_SPACE : "").append(columnDefinition.toString());
                i++;
            }
            buffer.append(")");
        } else {
            boolean first = true;
            for (Expression expr : rowConstructor.getExprList().getExpressions()) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(",").append(OPTIONAL_WHITE_SPACE);
                }
                expr.accept(this);
            }
        }
        buffer.append(")");
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(rowGetExpression);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(OracleHint hint) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(hint);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(timeKeyExpression);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(literal);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(NextValExpression nextVal) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(nextVal.isUsingNextValueFor() ? "NEXT" + REQUIRED_WHITE_SPACE + "VALUE" +
                REQUIRED_WHITE_SPACE + "FOR" + OPTIONAL_WHITE_SPACE : "NEXTVAL" + REQUIRED_WHITE_SPACE + "FOR"
                + OPTIONAL_WHITE_SPACE).append(nextVal.getName());
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(CollateExpression col) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(col.getLeftExpression().toString()).append(OPTIONAL_WHITE_SPACE).append("COLLATE")
                .append(OPTIONAL_WHITE_SPACE).append(col.getCollate());
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr, (expr.isNot() ? "NOT" + REQUIRED_WHITE_SPACE : "") + OPTIONAL_WHITE_SPACE
                + "SIMILAR" + REQUIRED_WHITE_SPACE + "TO" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(ArrayExpression array) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(array);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(aThis);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(VariableAssignment variableAssignment) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        variableAssignment.getVariable().accept(this);
        buffer.append(REQUIRED_WHITE_SPACE).append(variableAssignment.getOperation()).append(REQUIRED_WHITE_SPACE);
        variableAssignment.getExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(XMLSerializeExpr expr) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        //xmlserialize(xmlagg(xmltext(COMMENT_LINE) ORDER BY COMMENT_SEQUENCE) as varchar(1024))
        buffer.append("xmlserialize(xmlagg(xmltext(");
        expr.getExpression().accept(this);
        buffer.append(")");
        if (expr.getOrderByElements() != null) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("ORDER BY").append(OPTIONAL_WHITE_SPACE);
            for (Iterator<OrderByElement> i = expr.getOrderByElements().iterator(); i.hasNext(); ) {
                buffer.append(i.next().toString());
                if (i.hasNext()) {
                    buffer.append(",").append(OPTIONAL_WHITE_SPACE);
                }
            }
        }
        buffer.append(")").append(OPTIONAL_WHITE_SPACE).append("AS").append(REQUIRED_WHITE_SPACE)
                .append(expr.getDataType()).append(")");
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(TimezoneExpression timezoneExpression) {
        timezoneExpression.getLeftExpression().accept(this);

        for (Expression expr : timezoneExpression.getTimezoneExpressions()) {
            buffer.append(OPTIONAL_WHITE_SPACE).append("AT").append(REQUIRED_WHITE_SPACE).append("TIME")
                    .append(REQUIRED_WHITE_SPACE).append("ZONE").append(OPTIONAL_WHITE_SPACE);
            expr.accept(this);
        }
    }

    @Override
    public void visit(JsonAggregateFunction expression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(expression);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(JsonFunction expression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(expression);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(ConnectByRootOperator connectByRootOperator) {
        buffer.append("CONNECT_BY_ROOT").append(OPTIONAL_WHITE_SPACE);
        connectByRootOperator.getColumn().accept(this);
    }

    @Override
    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer
                .append(oracleNamedFunctionParameter.getName())
                .append(OPTIONAL_WHITE_SPACE)
                .append("=>")
                .append(OPTIONAL_WHITE_SPACE);

        oracleNamedFunctionParameter.getExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(AllColumns allColumns) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(allColumns);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(allTableColumns);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(AllValue allValue) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(allValue);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visit(isDistinctExpression);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(GeometryDistance geometryDistance) {
        visitOldOracleJoinBinaryExpression(geometryDistance,
                                           OPTIONAL_WHITE_SPACE + geometryDistance.getStringExpression() + OPTIONAL_WHITE_SPACE);
    }

    @Override
    protected void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        buffer.append(OPTIONAL_WHITE_SPACE);
        super.visitBinaryExpression(binaryExpression, operator);
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    /**
     * Adds RegEx for both equivalent evaluated {@link BinaryExpression} representations.
     * @param binaryExpression to deparse
     * @param operator between the the expressions of the binary
     */
    protected void visitCommutativeBinaryExpression(BinaryExpression binaryExpression, String operator) {
        buffer.append("(?:");
        buffer.append(OPTIONAL_WHITE_SPACE);
        binaryExpression.getLeftExpression().accept(this);
        buffer.append(operator);
        binaryExpression.getRightExpression().accept(this);

        buffer.append("|");

        binaryExpression.getRightExpression().accept(this);
        buffer.append(operator);
        binaryExpression.getLeftExpression().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE);
        buffer.append(")");
    }

    @Override
    public void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression, String operator) {
        buffer.append(OPTIONAL_WHITE_SPACE);

        expression.getLeftExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT) {
            buffer.append(OLD_ORACLE_JOIN + OPTIONAL_WHITE_SPACE);
        }
        buffer.append(operator);
        expression.getRightExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == SupportsOldOracleJoinSyntax.ORACLE_JOIN_LEFT) {
            buffer.append(OLD_ORACLE_JOIN + OPTIONAL_WHITE_SPACE);
        }
        buffer.append(OPTIONAL_WHITE_SPACE);
    }

    /**
     * Adds the provided table name or alias to the class attribute for further processing.
     * @param tableNameOrAlias {@literal String} to add
     */
    public void addTableNameAlias(String tableNameOrAlias){
        String tableNameOrAliasWithOutQuotationMarks = tableNameOrAlias.replaceAll(QUOTATION_MARK_REGEX, "");
        if(tableNameOrAlias.contains(" ")){
            this.tableNameAliasMap.put(tableNameOrAliasWithOutQuotationMarks.split(" ")[0], tableNameOrAliasWithOutQuotationMarks.split(" ")[1]);
            this.tableNameAliasMap.put(tableNameOrAliasWithOutQuotationMarks.split(" ")[1], tableNameOrAliasWithOutQuotationMarks.split(" ")[0]);
        } else {
            this.tableNameAliasMap.put(tableNameOrAliasWithOutQuotationMarks, tableNameOrAliasWithOutQuotationMarks);
        }
    }

    /**
     * {@return the table name alias map attribute containing one entry K:table name V:alias and one entry K:alias V:table name.}
     */
    public Map<String, String> getTableNameAliasMap() {
        return this.tableNameAliasMap;
    }

    /**
     * Provided map must fulfill schema: one entry K:table name V:alias and one entry K:alias V:table name.
     * @param tableNameAliasMap Map to set
     */
    public void setTableNameAliasMap(Map<String, String> tableNameAliasMap) {
        this.tableNameAliasMap = tableNameAliasMap;
    }

    /**
     * Returns an alias for provided tablename and vice versa.
     * @param input Table name or table name alias
     * @return Counterpart for provided input or null if none found
     */
    public String getRelatedTableNameOrAlias(String input){
        String inputWithOutQuotionMarks = input.replaceAll(QUOTATION_MARK_REGEX, "");
        if(this.tableNameAliasMap.get(inputWithOutQuotionMarks) != null){
            return this.tableNameAliasMap.get(input);
        }
        return null;
    }
}
