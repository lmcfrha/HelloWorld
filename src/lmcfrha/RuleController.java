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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;


public class RuleController {


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
    private Button saveButton;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;


    public RuleController() throws ParserConfigurationException {
    }

    @FXML
    public void loadFile() throws IOException, SAXException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open XML File");
        f = fileChooser.showOpenDialog(vBox.getScene().getWindow());
        try {
            doc = (DOMDocument) reader.read(f);
            XmlElement root = new XmlElement((DOMElement) doc.getRootElement());
            TreeItem<XmlElement> rootXmlElement = new TreeItem<>(root);
            XMLTreeItemGen.buildTree(rootXmlElement);
            tree.setRoot(rootXmlElement);
            prepare(tree);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveAs() throws IOException, SAXException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save XML File");
        File f = fileChooser.showSaveDialog(vBox.getScene().getWindow());
        // Create a file named as person.xml
        FileOutputStream fos = new FileOutputStream(f);
        // Create the pretty print of xml document.
        OutputFormat format = OutputFormat.createPrettyPrint();
        // Create the xml writer by passing outputstream and format
        XMLWriter writer = new XMLWriter(fos, format);
        // Write to the xml document
        //doc.getRootElement().detach();
        doc.setRootElement(((TreeItem<XmlElement>) tree.getRoot()).getValue().getE());
        writer.write(doc);
        //writer.write((((TreeItem<XmlElement>) tree.getRoot()).getValue().getE()));
        // Flush after done
        writer.flush();

    }

    @FXML
    public void save() throws IOException, SAXException {
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
        parentElement.replaceChild(newElement,oldElement);
        // Prepare the new root Item from the new root element
        XmlElement newRoot = new XmlElement(root);
        TreeItem<XmlElement> newRootItem = new TreeItem<>(newRoot);
        newRootItem.setExpanded(true);
        // Rebuild the tree from the new root item and make it the tree root.
        XMLTreeItemGen.buildTree(newRootItem);
        tree.setRoot(newRootItem);
    }

    @FXML
    private void addClicked() {
        if (addButton.isDisabled()) return;
        TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
        // Get the XPath starting from the tree root
        String elementXPath = getXPathFromRoot(selectedItem, "");
        // Insert the new node from the text edit pane in the root element, before the old one
        DOMElement root = ((TreeItem<XmlElement>) tree.getRoot()).getValue().getE();
        DOMElement oldElement = (DOMElement) root.selectSingleNode(elementXPath);
        DOMElement newElement = textToXml(elementEditor.getText()).getE();
        DOMElement parentElement = (DOMElement) oldElement.getParentNode();
        parentElement.insertBefore(newElement,oldElement);
        // Prepare the new root Item from the new root element
        XmlElement newRoot = new XmlElement(root);
        TreeItem<XmlElement> newRootItem = new TreeItem<>(newRoot);
        newRootItem.setExpanded(true);
        // Rebuild the tree from the new root item and make it the tree root.
        XMLTreeItemGen.buildTree(newRootItem);
        tree.setRoot(newRootItem);

    }

    @FXML
    private void deleteClicked() {
        if (deleteButton.isDisabled()) return;
        TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
        // Get the XPath starting from the tree root
        String elementXPath = getXPathFromRoot(selectedItem, "");
        // Replace the node in the root element with the new one saved from the text edit pane
        DOMElement root = ((TreeItem<XmlElement>) tree.getRoot()).getValue().getE();
        DOMElement toDelete = (DOMElement) root.selectSingleNode(elementXPath);
        toDelete.detach();
        // Prepare the new root Item from the new root element
        XmlElement newRoot = new XmlElement(root);
        TreeItem<XmlElement> newRootItem = new TreeItem<>(newRoot);
        newRootItem.setExpanded(true);
        // Rebuild the tree from the new root item and make it the tree root.
        XMLTreeItemGen.buildTree(newRootItem);
        tree.setRoot(newRootItem);
        elementEditor.clear();
    }

    private class XmlElementTreeCellImpl extends TreeCell<XmlElement> {

        private TextField textField;

        public XmlElementTreeCellImpl() {
        }

        @Override
        public void startEdit() {
            super.startEdit();
            elementEditor.setText("edit HERE!!!");

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(((XmlElement) getItem()).toString());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(XmlElement item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                }
            }

        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(new XmlElement(new DOMElement("Zory")));
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    private void prepare(TreeView tree) {
//        tree.setEditable(true);
        // Set the customized cell factory producing the XmlElementTreeCellImpl
        tree.setCellFactory(new Callback<TreeView<XmlElement>,TreeCell<XmlElement>>(){
            @Override
            public TreeCell<XmlElement> call(TreeView<XmlElement> p) {
                return new XmlElementTreeCellImpl();
            }
        });
        // Get the element editor pane to display the currently element selected
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
        while (sibling != null) {
            psibling++;
            sibling = sibling.previousSibling();
        }
        sibling = treeItem.nextSibling();
        while (sibling != null) {
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
