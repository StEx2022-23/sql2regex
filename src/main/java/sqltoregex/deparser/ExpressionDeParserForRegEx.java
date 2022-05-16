package sqltoregex.deparser;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ExpressionDeParserForRegEx extends ExpressionDeParser {
    private static final String REQUIRED_WHITE_SPACE = "\\s+";
    private static final String OPTIONAL_WHITE_SPACE = "\\s*";
    public static final String NOT = "NOT";


    public ExpressionDeParserForRegEx() {
        super();
    }

    public ExpressionDeParserForRegEx(SelectVisitor selectVisitor, StringBuilder buffer) {
        super(selectVisitor, buffer);
    }

    protected void visitCommutativeBinaryExpression(BinaryExpression binaryExpression, String operator){
        binaryExpression.getLeftExpression().accept(this);
        buffer.append(operator);
        binaryExpression.getRightExpression().accept(this);

        buffer.append("|");

        binaryExpression.getRightExpression().accept(this);
        buffer.append(operator);
        binaryExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Addition addition) {
        visitCommutativeBinaryExpression(addition, OPTIONAL_WHITE_SPACE + "+" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitCommutativeBinaryExpression(andExpression, andExpression.isUseOperator() ? OPTIONAL_WHITE_SPACE + "&&" + OPTIONAL_WHITE_SPACE : OPTIONAL_WHITE_SPACE + "AND" + OPTIONAL_WHITE_SPACE);
    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        if (between.isNot()) {
            buffer.append(OPTIONAL_WHITE_SPACE + "NOT");
        }

        buffer.append(OPTIONAL_WHITE_SPACE + "BETWEEN" + OPTIONAL_WHITE_SPACE);
        between.getBetweenExpressionStart().accept(this);
        buffer.append(OPTIONAL_WHITE_SPACE + "AND" + OPTIONAL_WHITE_SPACE);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitOldOracleJoinBinaryExpression(equalsTo, OPTIONAL_WHITE_SPACE + "=" + OPTIONAL_WHITE_SPACE);
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
        buffer.append(doubleValue.toString());
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
        if (notExpr.isExclamationMark()) {
            buffer.append("!" + OPTIONAL_WHITE_SPACE);
        } else {
            buffer.append(OPTIONAL_WHITE_SPACE + NOT + OPTIONAL_WHITE_SPACE);
        }
        notExpr.getExpression().accept(this);
    }

    @Override
    public void visit(BitwiseRightShift expr) {
        super.visit(expr);
    }

    @Override
    public void visit(BitwiseLeftShift expr) {
        super.visit(expr);
    }

    @Override
    public void visitOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression expression, String operator) {
        int isOracleSyntax = 0;

        expression.getLeftExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
            buffer.append("(+)");
            isOracleSyntax = EqualsTo.ORACLE_JOIN_RIGHT;
        }
        buffer.append(operator);
        expression.getRightExpression().accept(this);
        if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
            buffer.append("(+)");
            isOracleSyntax = EqualsTo.ORACLE_JOIN_LEFT;
        }

        if (isOracleSyntax != 0){
            expression.getRightExpression().accept(this);
            if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_LEFT) {
                buffer.append("(+)");
            }
            buffer.append(operator);
            expression.getLeftExpression().accept(this);
            if (expression.getOldOracleJoinSyntax() == EqualsTo.ORACLE_JOIN_RIGHT) {
                buffer.append("(+)");
            }
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        super.visit(inExpression);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        super.visit(fullTextSearch);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        super.visit(signedExpression);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        super.visit(isNullExpression);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        super.visit(isBooleanExpression);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        super.visit(jdbcParameter);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        super.visit(likeExpression);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        super.visit(existsExpression);
    }

    @Override
    public void visit(LongValue longValue) {
        super.visit(longValue);
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
    }

    @Override
    public void visit(Multiplication multiplication) {
        super.visit(multiplication);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
    }

    @Override
    public void visit(NullValue nullValue) {
        super.visit(nullValue);
    }

    @Override
    public void visit(OrExpression orExpression) {
        super.visit(orExpression);
    }

    @Override
    public void visit(XorExpression xorExpression) {
        super.visit(xorExpression);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        super.visit(parenthesis);
    }

    @Override
    public void visit(StringValue stringValue) {
        super.visit(stringValue);
    }

    @Override
    public void visit(Subtraction subtraction) {
        super.visit(subtraction);
    }

    @Override
    protected void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
        super.visitBinaryExpression(binaryExpression, operator);
    }

    @Override
    public void visit(SubSelect subSelect) {
        super.visit(subSelect);
    }

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
    }

    @Override
    public void visit(Function function) {
        super.visit(function);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        super.visit(expressionList);
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        super.visit(namedExpressionList);
    }

    @Override
    public SelectVisitor getSelectVisitor() {
        return super.getSelectVisitor();
    }

    @Override
    public void setSelectVisitor(SelectVisitor visitor) {
        super.setSelectVisitor(visitor);
    }

    @Override
    public void visit(DateValue dateValue) {
        super.visit(dateValue);
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        super.visit(timestampValue);
    }

    @Override
    public void visit(TimeValue timeValue) {
        super.visit(timeValue);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        super.visit(caseExpression);
    }

    @Override
    public void visit(WhenClause whenClause) {
        super.visit(whenClause);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        super.visit(anyComparisonExpression);
    }

    @Override
    public void visit(Concat concat) {
        super.visit(concat);
    }

    @Override
    public void visit(Matches matches) {
        super.visit(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        super.visit(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        super.visit(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        super.visit(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast) {
        super.visit(cast);
    }

    @Override
    public void visit(TryCastExpression cast) {
        super.visit(cast);
    }

    @Override
    public void visit(Modulo modulo) {
        super.visit(modulo);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        super.visit(aexpr);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        super.visit(eexpr);
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        super.visit(multiExprList);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        super.visit(iexpr);
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        super.visit(jdbcNamedParameter);
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        super.visit(oexpr);
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        super.visit(rexpr);
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr) {
        super.visit(rexpr);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        super.visit(jsonExpr);
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        super.visit(jsonExpr);
    }

    @Override
    public void visit(UserVariable var) {
        super.visit(var);
    }

    @Override
    public void visit(NumericBind bind) {
        super.visit(bind);
    }

    @Override
    public void visit(KeepExpression aexpr) {
        super.visit(aexpr);
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        super.visit(groupConcat);
    }

    @Override
    public void visit(ValueListExpression valueList) {
        super.visit(valueList);
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        super.visit(rowConstructor);
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        super.visit(rowGetExpression);
    }

    @Override
    public void visit(OracleHint hint) {
        super.visit(hint);
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        super.visit(timeKeyExpression);
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        super.visit(literal);
    }

    @Override
    public void visit(NextValExpression nextVal) {
        super.visit(nextVal);
    }

    @Override
    public void visit(CollateExpression col) {
        super.visit(col);
    }

    @Override
    public void visit(SimilarToExpression expr) {
        super.visit(expr);
    }

    @Override
    public void visit(ArrayExpression array) {
        super.visit(array);
    }

    @Override
    public void visit(ArrayConstructor aThis) {
        super.visit(aThis);
    }

    @Override
    public void visit(VariableAssignment var) {
        super.visit(var);
    }

    @Override
    public void visit(XMLSerializeExpr expr) {
        super.visit(expr);
    }

    @Override
    public void visit(TimezoneExpression var) {
        super.visit(var);
    }

    @Override
    public void visit(JsonAggregateFunction expression) {
        super.visit(expression);
    }

    @Override
    public void visit(JsonFunction expression) {
        super.visit(expression);
    }

    @Override
    public void visit(ConnectByRootOperator connectByRootOperator) {
        super.visit(connectByRootOperator);
    }

    @Override
    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        super.visit(oracleNamedFunctionParameter);
    }

    @Override
    public void visit(AllColumns allColumns) {
        super.visit(allColumns);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        super.visit(allTableColumns);
    }

    @Override
    public void visit(AllValue allValue) {
        super.visit(allValue);
    }
}
