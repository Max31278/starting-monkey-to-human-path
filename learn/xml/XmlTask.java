/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PO41.Koval.wdad.learn.xml;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author 000
 */
public class XmlTask {
    private class Registration {
        public double coldwaterRegistration = 0;
        public double hotwaterRegistration = 0;
        public double electricityRegistration = 0;
        public double gasRegistration = 0;
    }
    
    private Document doc;
    private final String path ="src/PO41/Koval/wdad/learn/xml/housekeeper.xml";
    
    public XmlTask()  throws IOException, ParserConfigurationException, SAXException{
        generateDocument();
    }
    
    private void generateDocument() throws IOException, ParserConfigurationException, SAXException{
        File xmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
    }
    
    private void updateDocument() throws IOException{
        DOMImplementationLS domImplementationLS =
            (DOMImplementationLS)doc.getImplementation().getFeature("LS", "3.0");
        LSOutput lsOutput = domImplementationLS.createLSOutput();
        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            lsOutput.setByteStream(outputStream);
            LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
            lsSerializer.write(doc, lsOutput);
        }
    }
    
    private NodeList getFlatsBuild(NodeList buildings, String street, int buildingNumber){
        NodeList flats = null;
        NamedNodeMap buildingAttributes;
        if (buildings != null) {
            for (int i=0; i < buildings.getLength(); i++) {
                buildingAttributes = buildings.item(i).getAttributes();
                if (buildingAttributes.getNamedItem("street").getNodeValue().equals(street)&&
                        Integer.valueOf(buildingAttributes.getNamedItem("number").getNodeValue()) == buildingNumber){
                    flats = buildings.item(i).getChildNodes();
                }
            }
        }
        return flats;
    }
    
    private NodeList getRegistrationFlat(NodeList flats, int numberFlat){
        NodeList registration = null;
        NamedNodeMap flatsAttributes;
        if (flats != null){
            for (int i=0; i< flats.getLength(); i++){
                flatsAttributes = flats.item(i).getAttributes();
                if (Integer.valueOf(flatsAttributes.getNamedItem("number").getNodeValue()) == numberFlat) {
                    registration = flats.item(i).getChildNodes();
                }
            }
        }
        return registration;
    }
    
    private Node getLastRegistration(NodeList registrations){
        Node registration = null;
        NamedNodeMap registrationsAttributes;
        if (registrations != null){
            int lastYearRegistration=0;
            int lastMonthRegistration = 0;
            for (int i=0; i<registrations.getLength(); i++){
                registrationsAttributes = registrations.item(i).getAttributes();
                if(Integer.valueOf(registrationsAttributes.getNamedItem("year").getNodeValue()) >= lastYearRegistration &&
                   Integer.valueOf(registrationsAttributes.getNamedItem("month").getNodeValue()) >= lastMonthRegistration){
                    registration = registrations.item(i);
                }
            }
        }
        return registration;
    }
    
    private Node getPrevRegistration(NodeList registrations, int lastYearRegistration, int lastMonthRegistration){
        Node registration = null;
        NamedNodeMap registrationsAttributes;
        if (registrations != null){
            for (int i=0; i<registrations.getLength(); i++){
                registrationsAttributes = registrations.item(i).getAttributes();
                if(Integer.valueOf(registrationsAttributes.getNamedItem("year").getNodeValue()) >= lastYearRegistration &&
                   Integer.valueOf(registrationsAttributes.getNamedItem("month").getNodeValue()) >= lastMonthRegistration-1){
                    registration = registrations.item(i);
                }
            }
        }
        if (registration == null){
            for (int i=0; i<registrations.getLength(); i++){
                registrationsAttributes = registrations.item(i).getAttributes();
                if(Integer.valueOf(registrationsAttributes.getNamedItem("year").getNodeValue()) >= lastYearRegistration - 1 &&
                   Integer.valueOf(registrationsAttributes.getNamedItem("month").getNodeValue()) >= 12){
                    registration = registrations.item(i);
                }
            }
        }
        return registration;
    }
    
    private Registration getRegistration(Node registrations){
        Registration reg = new Registration();
        Node attributes;
        for (int i=0; i< registrations.getChildNodes().getLength();i++){
            attributes = registrations.getChildNodes().item(i);
            if (attributes.getNodeName().equals("coldwater")){
                reg.coldwaterRegistration = Integer.valueOf(attributes.getTextContent());
            }
            if (attributes.getNodeName().equals("hotwater")){
                reg.hotwaterRegistration = Integer.valueOf(attributes.getTextContent());
            }
            if (attributes.getNodeName().equals("electricity")){
                reg.electricityRegistration = Integer.valueOf(attributes.getTextContent());
            }
            if (attributes.getNodeName().equals("gas")){
                reg.gasRegistration = Integer.valueOf(attributes.getTextContent());
            }
        }
        return reg;            
    }
    
    public double getBill(String street, int buildingNumber, int flatNumber){
        double sum=0;
        NodeList tariffs = doc.getElementsByTagName("tariffs");       
        NodeList buildings = doc.getElementsByTagName("buildings");

        NodeList flats = getFlatsBuild(buildings, street, buildingNumber);
        NodeList registrations = getRegistrationFlat(flats, flatNumber);
        
        Node lastRegistrations = getLastRegistration(registrations);
        Node prevRegistrations = null;
        
        int lastYearRegistration = 0;
        int lastMonthRegistration = 0;
         if (lastRegistrations != null) {
             NamedNodeMap attributesRegistration = lastRegistrations.getAttributes();
             lastYearRegistration = Integer.valueOf(attributesRegistration.getNamedItem("year").getNodeValue());
             lastMonthRegistration = Integer.valueOf(attributesRegistration.getNamedItem("month").getNodeValue());
             prevRegistrations = getPrevRegistration(registrations, lastYearRegistration, lastMonthRegistration);
         }
         
         Registration reg = new Registration();
         if (lastRegistrations != null && prevRegistrations !=null){
             Registration lastRegistration = getRegistration(lastRegistrations);
             Registration prevRegistration = getRegistration(prevRegistrations);
             reg.coldwaterRegistration = lastRegistration.coldwaterRegistration-prevRegistration.coldwaterRegistration;
             reg.hotwaterRegistration = lastRegistration.hotwaterRegistration - prevRegistration.hotwaterRegistration;
             reg.electricityRegistration = lastRegistration.electricityRegistration - prevRegistration.electricityRegistration;
             reg.gasRegistration = lastRegistration.gasRegistration - prevRegistration.gasRegistration;             
         }
         
         NamedNodeMap tariffsAttributes = tariffs.item(0).getAttributes();
         sum = reg.coldwaterRegistration * Double.parseDouble(tariffsAttributes.getNamedItem("coldwater").getNodeValue())+
                 reg.hotwaterRegistration * Double.parseDouble(tariffsAttributes.getNamedItem("hotwater").getNodeValue())+
                 reg.electricityRegistration * Double.parseDouble(tariffsAttributes.getNamedItem("electricity").getNodeValue())+
                 reg.gasRegistration * Double.parseDouble(tariffsAttributes.getNamedItem("gas").getNodeValue());
         
         return sum;
    }
    
    public void setTariffs(String tariffName, double newValue) throws IOException{
        NodeList tariffs = doc.getElementsByTagName("tariffs");
        NamedNodeMap tariffsAttributes = tariffs.item(0).getAttributes();
        tariffsAttributes.getNamedItem(tariffName).setNodeValue(String.valueOf(newValue));
        updateDocument();
    }
    
    public void addRegistration (String street, int buildingNumber, int flatNumber, int year, int month,
                                 double coldWater, double hotWater, double electricity, double gas) throws IOException {
        NodeList buildings = doc.getElementsByTagName("building");
        NodeList flats = getFlatsBuild(buildings, street, buildingNumber);
        NamedNodeMap flatsAttributes;
        for (int i = 0; i < flats.getLength(); i++) {
            flatsAttributes = flats.item(i).getAttributes();
            if(Integer.valueOf(flatsAttributes.getNamedItem("number").getNodeValue()) == flatNumber){
                Node newRegestration = doc.createElement("registration");
                
                ((Element)newRegestration).setAttribute("year", String.valueOf(year));
                ((Element)newRegestration).setAttribute("month", String.valueOf(month));
                
                Node coldwater = doc.createElement("coldwater");
                coldwater.setNodeValue(String.valueOf(coldWater));
                
                Node hotwater = doc.createElement("hotwater");
                hotwater.setNodeValue(String.valueOf(hotWater));
                
                Node el = doc.createElement("electricity");
                el.setNodeValue(String.valueOf(electricity));
                
                Node g = doc.createElement("gas");
                g.setNodeValue(String.valueOf(gas));
                
                newRegestration.appendChild(coldwater);
                newRegestration.appendChild(hotwater);
                newRegestration.appendChild(el);
                newRegestration.appendChild(g);
                 
                flats.item(i).appendChild(newRegestration);
                updateDocument();
            }
        }
    }
}

