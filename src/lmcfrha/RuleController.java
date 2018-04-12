package lmcfrha;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
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
    Document doc=null;


    @FXML
    public Label helloWorld;
    @FXML
    public Button file;
    @FXML
    public Button create;
    @FXML
    public Button read;
    @FXML
    public Button update;
    @FXML
    public Button delete;
    @FXML
    public TreeView tree;


    public RuleController() throws ParserConfigurationException {
    }


    public void loadFile(MouseEvent mouseEvent) throws IOException, SAXException {

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
        File f=fileChooser.showOpenDialog(file.getScene().getWindow());

/*            Object o = u.unmarshal(f);  */
        try {
            doc = reader.read(f);
            XmlElement root = new XmlElement(doc.getRootElement());
            TreeItem<XmlElement> rootXmlElement=new TreeItem<>(root);
            XMLTreeItemGen.buildTree(rootXmlElement);
            tree.setRoot(rootXmlElement);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        /* Enable the buttons */
        create.setDisable(false);
        read.setDisable(false);
        update.setDisable(false);
        delete.setDisable(false);




    }
}
