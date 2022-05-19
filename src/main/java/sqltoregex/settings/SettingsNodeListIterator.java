package sqltoregex.settings;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SettingsNodeListIterator implements Iterable<Node> {
    private final NodeList NODE_LIST;

    public SettingsNodeListIterator(final NodeList NODE_LIST) {
        this.NODE_LIST = NODE_LIST;
    }

    @Override
    public Iterator<Node> iterator() {
        return new Iterator<>() {
            private int currentIndex = -1;
            private Node lastNode = null;

            @Override
            public boolean hasNext() {
                boolean isReplaced =
                        lastNode != null && currentIndex < NODE_LIST.getLength() && lastNode != NODE_LIST.item(
                        currentIndex);
                return currentIndex + 1 < NODE_LIST.getLength() || isReplaced;
            }

            @Override
            public Node next() {
                if (hasNext()) {
                    if (lastNode != null && currentIndex < NODE_LIST.getLength() && lastNode != NODE_LIST.item(
                            currentIndex)) {
                        lastNode = NODE_LIST.item(currentIndex);
                    } else {
                        currentIndex++;
                        lastNode = NODE_LIST.item(currentIndex);
                    }
                    return lastNode;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
