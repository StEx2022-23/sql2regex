package sqltoregex.deparser;

import net.sf.jsqlparser.util.deparser.LimitDeparser;

public class LimitDeParserForRegEx extends LimitDeparser {
    public LimitDeParserForRegEx(StringBuilder buffer) {
        super(buffer);
    }
}
