package sqltoregex.property;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SettingsNodeListIterator implements Iterable<Node>{
    private final NodeList nodeList;

    public SettingsNodeListIterator(final NodeList nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public Iterator<Node> iterator() {
        return new Iterator<>() {
            private int currentIndex = -1;
            private Node lastNode = null;

            @Override
            public boolean hasNext() {
                boolean isReplaced = lastNode != null && currentIndex < nodeList.getLength() && lastNode != nodeList.item(currentIndex);
                return currentIndex + 1 < nodeList.getLength() || isReplaced;
            }

            @Override
            public Node next() {
                if (hasNext()) {
                    if (lastNode != null && currentIndex < nodeList.getLength() && lastNode != nodeList.item(currentIndex)) {
                        lastNode = nodeList.item(currentIndex);
                    } else {
                        currentIndex++;
                        lastNode = nodeList.item(currentIndex);
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
