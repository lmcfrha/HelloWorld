package lmcfrha;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.dom4j.Element;
import org.w3c.dom.Document;

import java.util.Iterator;

public class XMLTreeItemGen {

TreeItem<XmlElement> genTree(org.dom4j.Document doc) {
    XmlElement root;
    root = new XmlElement(doc.getRootElement());
    TreeItem<XmlElement> rootXmlElement=new TreeItem<>(root);
    buildTree(rootXmlElement);
    return rootXmlElement;
    }
    static void buildTree(TreeItem<XmlElement> docXmlElement) {
        Element e = docXmlElement.getValue().getE();
        /* Get the children elements, turn them into XmlElements
           TreeItems and add as children
         */
        for (Iterator<Element> it = e.elementIterator(); it.hasNext();) {
            Element element = it.next();
            TreeItem<XmlElement> child=new TreeItem<>(new XmlElement(element));
            docXmlElement.getChildren().add(child);
            buildTree(child);
        }

    }
}


