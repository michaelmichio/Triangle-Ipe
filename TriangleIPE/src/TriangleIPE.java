import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TriangleIPE {
    Document doc;

    ArrayList<Vector2> vectors;
    int pageNumber = 1;

    public TriangleIPE(ArrayList<Vector2> vectors) {
        this.vectors = vectors;

        loadTemplate();
        if(this.doc != null) {
            modifyTemplate();
        }

    }

    public void loadTemplate() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.doc = builder.parse(this.getClass().getClassLoader().getResourceAsStream("template/Ipe_template_7.2.26.ipe"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void modifyTemplate() {
        // get ipe node & child node
        Node ipeNode = this.doc.getElementsByTagName("ipe").item(0);
        NodeList ipeList = ipeNode.getChildNodes();
        for(int i = 0; i < ipeList.getLength(); i++) {
            Node ipeChildNode = ipeList.item(i);

            if(ipeChildNode.getNodeType() == Node.ELEMENT_NODE) {
                Element ipeChildElement = (Element) ipeChildNode;

                // modify info attributes ( format: D:yyyyMMddHHmmss )
                if(ipeChildElement.getTagName().equals("info")) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    LocalDateTime now = LocalDateTime.now();
                    String attInfo = dtf.format(now);

                    ipeChildElement.setAttribute("created", "D:" + attInfo);
                    ipeChildElement.setAttribute("modified", "D:" + attInfo);
                }
                // build & modify initial page ( page 1 )
                else if(ipeChildElement.getTagName().equals("page")) {
                    NodeList pageList = ipeChildElement.getChildNodes();

                    Node viewNode = null;
                    for(int j = 0; j < pageList.getLength(); j++) {
                        Node pageChildNode = pageList.item(j);

                        if(pageChildNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element pageChildElement = (Element) pageChildNode;

                            // rename & set layer name from 1 to n
                            if(pageChildElement.getTagName().equals("layer")) {
                                pageChildElement.setAttribute("name", Integer.toString(this.pageNumber));
                            }
                            else if(pageChildElement.getTagName().equals("view")) {
                                viewNode = pageChildElement;
                                pageChildElement.setAttribute("layers", Integer.toString(1));
                                pageChildElement.setAttribute("active", Integer.toString(1));
                            }
                        }
                    }

                    // new layer
                    for(int j = 2; j < 2 + vectors.size(); j++) {
                        Element element = ipeChildElement.getOwnerDocument().createElement("layer");
                        ipeChildElement.insertBefore(element, viewNode);
                        ipeChildElement.insertBefore(this.doc.createTextNode("\n"), viewNode);

                        Attr attr = ipeChildElement.getOwnerDocument().createAttribute("name");
                        attr.setValue(String.valueOf(j));
                        element.setAttributeNode(attr);
                    }

                    // new view
                    String layers = "1";
                    for(int j = 2; j < 2 + vectors.size(); j++) {
                        Element element = ipeChildElement.getOwnerDocument().createElement("view");
                        ipeChildElement.appendChild(element);
                        ipeChildElement.appendChild(this.doc.createTextNode("\n"));

                        layers += " " + j;
                        Attr attr = ipeChildElement.getOwnerDocument().createAttribute("layers");
                        attr.setValue(layers);
                        element.setAttributeNode(attr);

                        attr = ipeChildElement.getOwnerDocument().createAttribute("active");
                        attr.setValue(String.valueOf(j));
                        element.setAttributeNode(attr);
                    }

                    // declare input to initial page ( page 1 )
                    for(int j = 0; j < this.vectors.size(); j++) {
                        Element element = ipeChildElement.getOwnerDocument().createElement("use");
                        ipeChildElement.appendChild(element);

                        if(j == 0) {
                            Attr attr = element.getOwnerDocument().createAttribute("layer");
                            attr.setValue(Integer.toString(pageNumber));
                            element.setAttributeNode(attr);
                        }

                        Attr attr = element.getOwnerDocument().createAttribute("name");
                        attr.setValue("mark/disk(sx)");
                        element.setAttributeNode(attr);

                        attr = element.getOwnerDocument().createAttribute("pos");
                        attr.setValue(this.vectors.get(j).toString());
                        element.setAttributeNode(attr);

                        attr = element.getOwnerDocument().createAttribute("size");
                        attr.setValue("normal");
                        element.setAttributeNode(attr);

                        attr = element.getOwnerDocument().createAttribute("stroke");
                        attr.setValue("black");
                        element.setAttributeNode(attr);

                        ipeChildElement.appendChild(this.doc.createTextNode("\n"));
                    }

                    // generate solution
                    for(int j = 1; j < vectors.size() + 1; j++) {
                        Element element;
                        Attr attr;

                        this.pageNumber++;

                        // path
                        element = ipeChildElement.getOwnerDocument().createElement("path");
                        ipeChildElement.appendChild(element);

                        attr = element.getOwnerDocument().createAttribute("layer");
                        attr.setValue(String.valueOf(pageNumber));
                        element.setAttributeNode(attr);

                        attr = element.getOwnerDocument().createAttribute("stroke");
                        attr.setValue("black");
                        element.setAttributeNode(attr);

                        // generate steps
                        Text text;
                        text = this.doc.createTextNode("\n" + vectors.get(j - 1) + " m");
                        element.appendChild(text);
                        if(j < vectors.size()) {
                            text = this.doc.createTextNode("\n" + vectors.get(j) + " l\n");
                            element.appendChild(text);
                        }
                        else {
                            text = this.doc.createTextNode("\n" + vectors.get(0) + " l\n");
                            element.appendChild(text);
                        }

                        ipeChildElement.appendChild(this.doc.createTextNode("\n"));
                    }

                }
            }

        }
    }

    public void printResultAsIpe() {
        if(this.doc == null) {
            System.out.println("Document is null.");
            return;
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DOMSource source = new DOMSource(this.doc);
        StreamResult streamResult = new StreamResult(new File("output.ipe"));
        try {
            transformer.transform(source, streamResult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Successfully printed.");
    }

}
