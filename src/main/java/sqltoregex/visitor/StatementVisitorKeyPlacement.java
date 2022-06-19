package sqltoregex.visitor;

import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.springframework.util.Assert;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatementVisitorKeyPlacement extends StatementVisitorAdapter {
    private KeyPlacementOption keyPlacementOption;

    public StatementVisitorKeyPlacement(KeyPlacementOption keyPlacementOption){
        Assert.notNull(keyPlacementOption, "keyPlacementOption must not be null");
        this.keyPlacementOption = keyPlacementOption;
    }

    @Override
    public void visit(CreateTable createTable) {
        switch (this.keyPlacementOption){
            case USER_INPUT -> {}
            case ONLY_COLUMN -> this.pushAllIndizesInColumns(createTable);
            case ONLY_CONSTRAINT -> this.pushAllIndizesInConstraints(createTable);
        }
    }

    private void pushAllIndizesInConstraints(CreateTable createTable){
        List<ColumnDefinition> columnDefinitionList = createTable.getColumnDefinitions();

        for (ColumnDefinition definition : columnDefinitionList) {
            Index index = this.generateIndex(definition);
            if (index != null){
                createTable.getIndexes().add(index);
            }
            this.deleteAllColumnIndizes(definition);
        }
    }

    private void pushAllIndizesInColumns(CreateTable createTable){
        if (createTable.getIndexes() == null){
            return;
        }

        List<Index> indexList = new LinkedList<>();
        Map<String, ColumnDefinition> columnDefinitionMap = this.createColumnMap(createTable.getColumnDefinitions());

        for (Index index : createTable.getIndexes()){
            if (index.getType().equals("PRIMARY KEY")){
                if (index.getColumns().size() == 1){
                    String indexColumn = index.getColumns().get(0).getColumnName();
                    List<String> columnSpecs = columnDefinitionMap.get(indexColumn).getColumnSpecs();
                    if (columnSpecs == null){
                        columnSpecs = new LinkedList<>();
                        columnDefinitionMap.get(indexColumn).setColumnSpecs(columnSpecs);
                    }
                    columnSpecs.addAll(List.of("PRIMARY", "KEY"));
                }
                continue;
            }
            if (index.getType().equals("UNIQUE KEY")){
                if (index.getColumns().size() == 1){
                    String indexColumn = index.getColumns().get(0).getColumnName();
                    columnDefinitionMap.get(indexColumn).getColumnSpecs()
                            .addAll(List.of("UNIQUE", "KEY", indexColumn));
                }
                continue;
            }
            indexList.add(index);
        }

        createTable.setIndexes(indexList);
    }

    private Map<String, ColumnDefinition> createColumnMap(List<ColumnDefinition> columnDefinitionList){
        Map<String, ColumnDefinition> columnDefinitionMap = new HashMap<>();

        for (ColumnDefinition columnDefinition : columnDefinitionList){
            columnDefinitionMap.put(columnDefinition.getColumnName(), columnDefinition);
        }

        return columnDefinitionMap;
    }

    private Index generateIndex(ColumnDefinition columnDefinition){
        String columnDefString = "";
        List<String> columnSpecs = columnDefinition.getColumnSpecs();
        if (columnSpecs != null) {
            columnDefString = String.join(" ", columnDefinition.getColumnSpecs());
        }

        Pattern pPrimary = Pattern.compile("(?<!UNIQUE.*)(PRIMARY)?\\s*KEY");
        Matcher mPrimary = pPrimary.matcher(columnDefString);
        if (mPrimary.find()){
            return new Index().withType("PRIMARY KEY").withColumns(List.of(new Index.ColumnParams(columnDefinition.getColumnName())));
        }else if(columnDefString.matches(".*?UNIQUE\\s*(KEY)?")){
            return new Index().withType("UNIQUE KEY").withColumns(List.of(new Index.ColumnParams(columnDefinition.getColumnName())));
        }
        return null;
    }

    private void deleteAllColumnIndizes(ColumnDefinition columnDefinition){
        List<String> columnSpecsList = new LinkedList<>();

        if (columnDefinition.getColumnSpecs() == null){
            return;
        }

        Iterator<String> iterator = columnDefinition.getColumnSpecs().iterator();
        while (iterator.hasNext()){
            String spec = iterator.next();
            while (spec.matches("(UNIQUE|PRIMARY|KEY|\\(.*\\))")) {
                if (iterator.hasNext()){
                    spec = iterator.next();
                }else{
                    spec = "";
                }
            }
            if (!spec.isEmpty()){
                columnSpecsList.add(spec);
            }

        }
        columnDefinition.setColumnSpecs(columnSpecsList);
    }

    /**
     * Holds all possible placement options for (primary) keys
     */
    public enum KeyPlacementOption{
        USER_INPUT,
        ONLY_CONSTRAINT,
        ONLY_COLUMN
    }
}
