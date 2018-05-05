package lmcfrha;


import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

import java.util.Iterator;

/* Wrapping for Element so the toString can
   be overridden and the tree view displays the
   element's tags and attributes(otherwise you get the
   Object's toString, not too nice)
   Also has the toStringFull to show the element's content too
 */
public class XmlElement {
    private DOMElement e;

    XmlElement(DOMElement e) {
        this.setE(e);
    }

    @Override
    public String toString() {
        StringBuilder entry= new StringBuilder();
        entry.append(getE().getName());
        Iterator<Attribute> itr = getE().attributeIterator();
        while (itr.hasNext()) {
            Attribute at = itr.next();
            entry.append(" ");
            entry.append(at.getName());
            entry.append("=");
            entry.append(at.getValue());
        }
        return entry.toString();
    }
    public String toStringFull() {
        return getE().asXML();
    }
    public DOMElement getE() {
        return e;
    }

    public void setE(DOMElement e) {
        this.e = e;
    }
}
