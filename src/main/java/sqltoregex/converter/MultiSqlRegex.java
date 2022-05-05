package sqltoregex.converter;

import java.util.List;

public class MultiSqlRegex {
    private List<SqlRegex> multiSqlRegexStatements;

    public MultiSqlRegex(List<SqlRegex> multsqlregexstatements){
        this.multiSqlRegexStatements = multsqlregexstatements;
    }

    public void setMultiSqlRegex(List<SqlRegex> sqlregexlist){
        this.multiSqlRegexStatements = sqlregexlist;
    }

    public List<SqlRegex> getMultiSqlRegex(){
        return this.multiSqlRegexStatements;
    }

    public void convert(){
        for(SqlRegex sqlregex : multiSqlRegexStatements){
            sqlregex.convert();
        }
    }

    @Override
    public String toString(){
        StringBuilder json = new StringBuilder();
        json.append("[");
        for(SqlRegex sqlregex : this.getMultiSqlRegex()) {
            json.append("{\"sql\":\"").append(sqlregex.getSql()).append("\", \"regex\":\"").append(sqlregex.getRegex()).append("\"},");
        }
        json.deleteCharAt(json.length() - 1);
        json.append("]");
        return json.toString();
    }

}
