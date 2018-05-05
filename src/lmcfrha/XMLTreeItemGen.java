package lmcfrha;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.w3c.dom.Document;

import java.util.Iterator;

public class XMLTreeItemGen {



    static void buildTree(TreeItem<XmlElement> docXmlElement) {
        // Get current root element and rebuild from there.
        DOMElement e = docXmlElement.getValue().getE();
        /* Get the children elements, turn them into XmlElements
           TreeItems and add as children
         */
        for (Iterator<Element> it = e.elementIterator(); it.hasNext();) {
            DOMElement element = (DOMElement) it.next();
            TreeItem<XmlElement> child=new TreeItem<>(new XmlElement(element));
            child.setExpanded(true);
            docXmlElement.getChildren().add(child);
            buildTree(child);
        }
    }

}


