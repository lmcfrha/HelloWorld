package lmcfrha;


import org.dom4j.Element;
/* Wrapping for Element so the toString can
   be overridden and the tree view displays the
   element's tags (otherwise you get the Object's toString,
   not too nice
 */
public class XmlElement {
    private Element e;

    XmlElement(Element e) {
        this.setE(e);
    }

    @Override
    public String toString() {
        return getE().getName();
    }

    public Element getE() {
        return e;
    }

    public void setE(Element e) {
        this.e = e;
    }
}
