import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TriangleIPE {
    ArrayList<Vector2> vectors;
    int stepNumber = 1;

    public TriangleIPE(ArrayList<Vector2> vectors) {
        this.vectors = vectors;
        System.out.println();
        loadTemplate();
    }

    public void loadTemplate() {
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(this.getClass().getClassLoader().getResourceAsStream("template/Ipe_template_7.2.26.ipe"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // get ipe node
        Node ipeNode = doc.getElementsByTagName("ipe").item(0);
        NodeList ipeList = ipeNode.getChildNodes();
        for(int i = 0; i < ipeList.getLength(); i++) {
            Node ipeChildNode = ipeList.item(i);

            if(ipeChildNode.getNodeType() == Node.ELEMENT_NODE) {
                System.out.println("updating " + ipeList.item(i).getNodeName() + ".");
                Element ipeChildElement = (Element) ipeChildNode;

                // set created and modified info
                if(ipeChildElement.getTagName().equals("info")) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    LocalDateTime now = LocalDateTime.now();
                    String attInfo = dtf.format(now);

                    ipeChildElement.setAttribute("created", "D:" + attInfo);
                    ipeChildElement.setAttribute("modified", "D:" + attInfo);
                }
                // modify page node
                else if(ipeChildElement.getTagName().equals("page")) {
                    NodeList pageList = ipeChildElement.getChildNodes();

                    for(int j = 0; j < pageList.getLength(); j++) {
                        Node pageChildNode = pageList.item(j);
                        if(pageChildNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element pageChildElement = (Element) pageChildNode;

                            // set layer name from 1 to n
                            if(pageChildElement.getTagName().equals("layer")) {
                                pageChildElement.setAttribute("name", Integer.toString(this.stepNumber));
                            }
                            else if(pageChildElement.getTagName().equals("view")) {
                                pageChildElement.setAttribute("layers", Integer.toString(this.stepNumber));
                                pageChildElement.setAttribute("active", Integer.toString(this.stepNumber));
                            }
                        }
                    }

                    // parse input to xml page 1
                    for(int j = 0; j < this.vectors.size(); j++) {
                        Element newElement = ipeChildElement.getOwnerDocument().createElement("use");
                        ipeChildElement.appendChild(newElement);

                        if(j == 0) {
                            Attr attrLayer = newElement.getOwnerDocument().createAttribute("layer");
                            attrLayer.setValue(Integer.toString(stepNumber));
                            newElement.setAttributeNode(attrLayer);
                        }

                        Attr attrName = newElement.getOwnerDocument().createAttribute("name");
                        attrName.setValue("mark/disk(sx)");
                        newElement.setAttributeNode(attrName);

                        Attr attrPos = newElement.getOwnerDocument().createAttribute("pos");
                        attrPos.setValue(this.vectors.get(j).toString());
                        newElement.setAttributeNode(attrPos);

                        Attr attrSize = newElement.getOwnerDocument().createAttribute("size");
                        attrSize.setValue("normal");
                        newElement.setAttributeNode(attrSize);

                        Attr attrStroke = newElement.getOwnerDocument().createAttribute("stroke");
                        attrStroke.setValue("black");
                        newElement.setAttributeNode(attrStroke);

                        ipeChildElement.appendChild(doc.createTextNode("\n"));
                    }
//                    Element edgesElement = ipeChildElement.getOwnerDocument().createElement("path");
                }

                System.out.println(ipeList.item(i).getNodeName() + " updated.");
            }
        }
        // print
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        DOMSource source = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(new File("output.ipe"));
        try {
            transformer.transform(source, streamResult);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public void printPDF() {

    }

}
