package lmcfrha;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.dom4j.*;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;


public class XMLhelperController {


    private SAXReader reader = new SAXReader(DOMDocumentFactory.getInstance());
    private DOMDocument doc = null;
    private String xmlVersion, xmlEncoding, xmlStandalone;
    private File f = null;

    @FXML
    public VBox vBox;
    @FXML
    public TreeView tree;
    @FXML
    public TextArea  elementEditor;
    @FXML
    public TextArea  messages;
    @FXML
    private Button saveButton;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;


    public XMLhelperController() throws ParserConfigurationException {
    }

    @FXML
    public void loadFile()  {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open XML File");
        f = fileChooser.showOpenDialog(vBox.getScene().getWindow());
        try {
            doc = (DOMDocument) reader.read(f);
            XmlElement root = new XmlElement((DOMElement) doc.getRootElement());
            TreeItem<XmlElement> rootXmlElement = new TreeItem<>(root);
            XMLTreeItemGen.buildTree(rootXmlElement, false);
            tree.setRoot(rootXmlElement);
            prepare(tree);
        } catch (Exception e) {
            e.printStackTrace();
            messages.appendText(e.toString());

        }
    }

    @FXML
    public void saveAs()  {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save XML File");
        File f = fileChooser.showSaveDialog(vBox.getScene().getWindow());
        // Create a file named as person.xml
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Create the pretty print of xml document.
        OutputFormat format = OutputFormat.createPrettyPrint();
        // Create the xml writer by passing outputstream and format
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(fos, format);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            messages.appendText(e.toString());
        }
        // Write to the xml document
        //doc.getRootElement().detach();
        doc.setRootElement(((TreeItem<XmlElement>) tree.getRoot()).getValue().getE());
        try {
            writer.write(doc);
        } catch (IOException e) {
            e.printStackTrace();
            messages.appendText(e.toString());
        }
        //writer.write((((TreeItem<XmlElement>) tree.getRoot()).getValue().getE()));
        // Flush after done
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            messages.appendText(e.toString());
        }

    }

    @FXML
    public void save() throws IOException, SAXException {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            // Create the pretty print of xml document.
            OutputFormat format = OutputFormat.createPrettyPrint();
            // Create the xml writer by passing outputstream and format
            XMLWriter writer = new XMLWriter(fos, format);
            // Write to the xml document
            doc.setRootElement(((TreeItem<XmlElement>) tree.getRoot()).getValue().getE());
            writer.write(doc);
            //writer.write((((TreeItem<XmlElement>) tree.getRoot()).getValue().getE()));
            // Flush after done
            writer.flush();
        } catch (Exception e) {
            messages.appendText(e.toString());
        }
    }


    @FXML
    public void close() throws IOException, SAXException {
        System.exit(0);
    }

    @FXML
    private void textAreaClicked() {
        addButton.setDisable(false);
        saveButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    @FXML
    private void saveClicked() {
        try {
            if (saveButton.isDisabled()) return;
            // Transform the text in the editor pane to a Document, then a TreeItem
            TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
            // Get the XPath starting from the tree root
            String elementXPath = getXPathFromRoot(selectedItem, "");
            // Replace the node in the root element with the new one saved from the text edit pane
            DOMElement root = ((TreeItem<XmlElement>) tree.getRoot()).getValue().getE();
            DOMElement oldElement = (DOMElement) root.selectSingleNode(elementXPath);
            DOMElement newElement = textToXml(elementEditor.getText()).getE();
            DOMElement parentElement = (DOMElement) oldElement.getParentNode();
            // Prepare the new root Item from the new root element
            XmlElement newRoot;
            if (parentElement != null) {
                parentElement.replaceChild(newElement, oldElement);
                newRoot = new XmlElement(root);
            } else newRoot = new XmlElement(newElement);
            // Prepare the new root Item from the new root element
            TreeItem<XmlElement> newRootItem = new TreeItem<>(newRoot);
            //newRootItem.setExpanded(true);
            // Rebuild the tree from the new root item and make it the tree root.
            TreeItem<XmlElement> toSelect = XMLTreeItemGen.buildTree(newRootItem, newElement, false);
            tree.setRoot(newRootItem);
            if (toSelect != null) tree.getSelectionModel().select(toSelect);
            tree.scrollTo(tree.getRow(toSelect)-5);
        } catch (Exception e) {
            messages.appendText(e.toString());
        }

    }

    @FXML
    private void addClicked() {
        try {
            if (addButton.isDisabled()) return;
            TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
            // Get the XPath starting from the tree root
            String elementXPath = getXPathFromRoot(selectedItem, "");
            // Insert the new node from the text edit pane in the root element, before the old one
            DOMElement root = ((TreeItem<XmlElement>) tree.getRoot()).getValue().getE();
            DOMElement oldElement = (DOMElement) root.selectSingleNode(elementXPath);
            DOMElement newElement = textToXml(elementEditor.getText()).getE();
            DOMElement parentElement = (DOMElement) oldElement.getParentNode();
            parentElement.insertBefore(newElement, oldElement);
            // Prepare the new root Item from the new root element
            XmlElement newRoot = new XmlElement(root);
            TreeItem<XmlElement> newRootItem = new TreeItem<>(newRoot);
            //newRootItem.setExpanded(true);
            // Rebuild the tree from the new root item and make it the tree root.
            TreeItem<XmlElement> toSelect = XMLTreeItemGen.buildTree(newRootItem, newElement, false);
            tree.setRoot(newRootItem);
            if (toSelect != null) {
                tree.getSelectionModel().select(toSelect);
                tree.scrollTo(tree.getRow(toSelect)-5);
                tree.getFocusModel().focus(tree.getRow(toSelect));
            }
        } catch (Exception e) {
            messages.appendText(e.toString());
        }
    }

    @FXML
    private void deleteClicked() {
        try {
            if (deleteButton.isDisabled()) return;
            TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
            // Get the XPath starting from the tree root
            String elementXPath = getXPathFromRoot(selectedItem, "");
            // Replace the node in the root element with the new one saved from the text edit pane
            DOMElement root = ((TreeItem<XmlElement>) tree.getRoot()).getValue().getE();
            DOMElement toDelete = (DOMElement) root.selectSingleNode(elementXPath);
            DOMElement parent = (DOMElement) toDelete.getParent();
            toDelete.detach();
            // Prepare the new root Item from the new root element
            XmlElement newRoot = new XmlElement(root);
            TreeItem<XmlElement> newRootItem = new TreeItem<>(newRoot);
            //newRootItem.setExpanded(true);
            // Rebuild the tree from the new root item and make it the tree root.
            TreeItem<XmlElement> toSelect = XMLTreeItemGen.buildTree(newRootItem, parent, false);
            tree.setRoot(newRootItem);
            if (toSelect != null) {
                tree.getSelectionModel().select(toSelect);
            }
            toSelect.setExpanded(true);
            tree.scrollTo(tree.getRow(toSelect)-5);
            tree.getFocusModel().focus(tree.getRow(toSelect));
            //elementEditor.clear();
        } catch (Exception e) {
            messages.appendText(e.toString());
        }
    }





    private void prepare(TreeView tree) {

        tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) newValue;
                if (newValue != null) elementEditor.setText(selectedItem.getValue().toStringFull());
                addButton.setDisable(true);
                saveButton.setDisable(true);
                deleteButton.setDisable(true);

            }
        });
    }
    private XmlElement textToXml(String text) {

        StringReader newTextReader = new StringReader(text);
        InputSource input = new InputSource(newTextReader);
        SAXReader reader = new SAXReader(DOMDocumentFactory.getInstance());
        DOMDocument changedItem = null;
        try {
            changedItem = (DOMDocument) reader.read(input);
        } catch (DocumentException e) {
            e.printStackTrace();
            messages.appendText(e.toString());
        }

        return (new XmlElement((DOMElement) changedItem.getRootElement()));

    }
    private String getXPathFromRoot(TreeItem<XmlElement> treeItem, String trailingPath) {
        StringBuilder node = new StringBuilder("/");
        node.append(treeItem.getValue().getE().getName());
        Integer index= new Integer(0);
        int psibling=0;
        int nsibling=0;
        TreeItem<XmlElement> sibling = treeItem.previousSibling();
        while (sibling != null && treeItem.getValue().getE().getName().equals(sibling.getValue().getE().getName())) {
            psibling++;
            sibling = sibling.previousSibling();
        }
        sibling = treeItem.nextSibling();
        while (sibling != null && treeItem.getValue().getE().getName().equals(sibling.getValue().getE().getName())) {
            nsibling++;
            sibling = sibling.nextSibling();
        }
        if (psibling != 0) {
            node.append("[");
            node.append(++psibling);
            node.append("]");
        }
        else if (psibling == 0 && nsibling !=0) {
            node.append("[1]");
        }
        if (treeItem.getParent() == null) return node.append(trailingPath).toString();
        else return getXPathFromRoot(treeItem.getParent(),node.append(trailingPath).toString());
    }
}
