package lmcfrha;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.util.NodeComparator;
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

    static TreeItem<XmlElement> buildTree(TreeItem<XmlElement> docXmlElement, DOMElement toSelect) {
        NodeComparator comparator = new NodeComparator();
        TreeItem<XmlElement> selected = null;

        // Get current root element and rebuild from there.
        DOMElement e = docXmlElement.getValue().getE();
        if (comparator.compare(e,toSelect) == 0) selected = docXmlElement;

        /* Get the children elements, turn them into XmlElements
           TreeItems and add as children
         */
        for (Iterator<Element> it = e.elementIterator(); it.hasNext();) {
            DOMElement element = (DOMElement) it.next();
            TreeItem<XmlElement> child=new TreeItem<>(new XmlElement(element));
            if (comparator.compare(element,toSelect) == 0) selected = child;
            child.setExpanded(true);
            docXmlElement.getChildren().add(child);
            if (selected != null) buildTree(child);
            else selected = buildTree(child, toSelect);
        }
        return selected;
    }
}


