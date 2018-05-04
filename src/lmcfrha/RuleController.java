package lmcfrha;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class RuleController {


    SAXReader reader = new SAXReader();
    Document doc = null;



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

    void prepare(TreeView tree) {
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
                elementEditor.setText(selectedItem.getValue().toStringFull());
                addButton.setDisable(true);
                saveButton.setDisable(true);
                deleteButton.setDisable(true);

            }
        });
    }

    public RuleController() throws ParserConfigurationException {
    }

    @FXML
    public void loadFile() throws IOException, SAXException {


        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance("cac");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        Unmarshaller u = null;
        try {
            u = jc.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open XML File");
        File f = fileChooser.showOpenDialog(vBox.getScene().getWindow());

        /*            Object o = u.unmarshal(f);  */
        try {
            doc = reader.read(f);
            XmlElement root = new XmlElement(doc.getRootElement());
            TreeItem<XmlElement> rootXmlElement = new TreeItem<>(root);
            XMLTreeItemGen.buildTree(rootXmlElement);
            tree.setRoot(rootXmlElement);
            prepare(tree);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
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
        elementEditor.setText("pushed when enabled");
    }
    @FXML
    private void addClicked() {
        if (saveButton.isDisabled()) return;
        TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
        selectedItem.getParent().getChildren().removeAll(selectedItem);
    }
    @FXML
    private void deleteClicked() {
        if (saveButton.isDisabled()) return;
        TreeItem<XmlElement> selectedItem = (TreeItem<XmlElement>) tree.getSelectionModel().getSelectedItem();
        selectedItem.getParent().getChildren().removeAll(selectedItem);
    }

    @FXML
    private void displayElement(MouseEvent event) {

           int i=0;
    }
    private class XmlElementTreeCellImpl extends TreeCell<XmlElement> {

        private TextField textField;

        public XmlElementTreeCellImpl() {
//            setOnMouseClicked(evt -> elementEditor.setText(getItem().toStringFull()));

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
                        commitEdit(new XmlElement(new DefaultElement("Zory")));
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

}
