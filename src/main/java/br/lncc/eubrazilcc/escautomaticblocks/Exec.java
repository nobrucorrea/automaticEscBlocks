/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.lncc.eubrazilcc.escautomaticblocks;

import br.lncc.sinapad.adapter.csgrid.CSGridService;
import br.lncc.sinapad.adapter.csgrid.application.CSGridApplicationService;
import br.lncc.sinapad.adapter.csgrid.application.converter.CSGridApplicationConverter;
import br.lncc.sinapad.adapter.csgrid.authentication.CSGridAuthenticationService;
import br.lncc.sinapad.core.application.representation.Group;
import br.lncc.sinapad.core.application.representation.Parameter;
import br.lncc.sinapad.core.application.representation.Representation;
import br.lncc.sinapad.core.application.representation.element.Checkbox;
import br.lncc.sinapad.core.application.representation.element.InputFile;
import br.lncc.sinapad.core.application.representation.element.OutputFile;
import br.lncc.sinapad.core.application.representation.element.Text;
import br.lncc.sinapad.core.application.representation.element.TextDouble;
import br.lncc.sinapad.core.application.representation.element.TextInteger;
import br.lncc.sinapad.core.data.ApplicationData;
import br.lncc.sinapad.core.exception.ApplicationConverterException;
import br.lncc.sinapad.core.exception.ApplicationServiceException;
import br.lncc.sinapad.core.exception.AuthenticationServiceException;
import br.lncc.sinapad.core.exception.ConfigurationServiceException;
import br.lncc.sinapad.core.exception.UserNotAuthorizedException;
import br.lncc.sinapad.core.service.application.ApplicationService;
import br.lncc.sinapad.core.service.application.converter.ApplicationConverterService;
import br.lncc.sinapad.core.service.authentication.AuthenticationService;
import br.lncc.sinapad.core.service.configuration.ConfigurationService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 *
 * @author bcorrea
 */
public class Exec {

    List<ApplicationData> apps;

    File rootDirectory = null;
    File sourceDirectory = null;
    File mainDirectory = null;
    File assemblyDirectory = null;
    File javaDirectory = null;
    File libDirectory = null;
    File resourceDirectory = null;
    File testDirectory = null;
    ApplicationService appService;
    String uuid;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, JDOMException, ConfigurationServiceException, AuthenticationServiceException, ApplicationServiceException, UserNotAuthorizedException, ApplicationConverterException {

        new Exec().readAlgorithms();
    }

    public void readAlgorithms() throws IOException, ParserConfigurationException, SAXException, JDOMException, ConfigurationServiceException, AuthenticationServiceException, ApplicationServiceException, UserNotAuthorizedException, ApplicationConverterException {

        ConfigurationService config = new CSGridService();
        InputStream is = new FileInputStream("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/src/main/java/esc-auto.properties");
        Properties properties = new Properties();
        properties.load(is);
        config.configure(properties);

        AuthenticationService authentication = new CSGridAuthenticationService();
        uuid = authentication.login("brunos", "ee1212", AuthenticationService.Domain.LDAP);

        ApplicationService application = new CSGridApplicationService();
        List<ApplicationData> data = application.list(uuid);

        ApplicationConverterService converter = new CSGridApplicationConverter();

        for (ApplicationData app : data) {

            String aName = app.getName();

            List<ApplicationData.VersionData> versions = app.getVersions();

            for (ApplicationData.VersionData version : versions) {
                String vName = version.getVersion();
                //InputStream stream = application.config(uuid, aName, vName);
                //Representation representation = converter.convert(stream, null);

                String block = aName + "_" + vName;
                //System.out.println(representation + " " + aName + " " + vName);

                createEscStructure(block);
                System.out.println("Struct block " + block + " created");

            }
        }

        this.createServiceXML(data, application, converter);

    }

    public void createEscStructure(String algorithm) throws IOException, ParserConfigurationException, SAXException, JDOMException {

        //TREE DIRECTORIES
        //src
        //main
        //assembly
        //java
        //lib
        //resources
        //test
        rootDirectory = new File("blocks/" + algorithm);
        rootDirectory.mkdir();

        File srcDir = new File("FirstOne");
        File destDir = new File(rootDirectory.getAbsolutePath());
        System.out.println(rootDirectory.getAbsolutePath());
        FileUtils.copyDirectory(srcDir, destDir);

        //changing MyService file
        String imports = "$IMPORTS";
        String className = "$CLASS_NAME";
        String properties = "$PROPERTIES";
        String preExecute = "$PRE_EXECUTE";
        String execute = "$EXECUTE";
        String posExecute = "$POS_EXECUTE";
        String upload = "$UPLOAD";
        String download = "$DOWNLOAD";
        String end = "$END";

        String[] template = {imports, className, properties, preExecute, execute, posExecute, upload, download, end, "//"};

        //crating tags on MyService.java File  with tags to insert code
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("blocks/" + algorithm + "/src/main/java/MyService.java"));

            for (String part : template) {
                System.out.println(part);
                out.write(part);
                out.write("\n");
            }

            out.close();

            BufferedReader fileService = new BufferedReader(new FileReader("blocks/" + algorithm + "/src/main/java/MyService.java"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            String esqueleto = "";
            //System.out.println( esqueleto.contains("$END"));
            while ((line = fileService.readLine()) != null && fileService.ready()) {
                sb.append(line + "\r\n");

                esqueleto = sb.toString();

            }

            //troca as tags do esqueleto pelos metodos com os algoritmos que realmene serao executados
            esqueleto = esqueleto.replace("$CLASS_NAME", "public class MyService implements WorkflowBlock {");

            esqueleto = esqueleto.replace("$END", "}\r\n");

            File directory = new File("blocks/esqueleto/");
            File[] files = directory.listFiles();

            for (File file : files) {
                if (!file.isDirectory()) {

                    BufferedReader templateContent = new BufferedReader(new FileReader("blocks/esqueleto/" + file.getName()));
                    StringBuilder stringlB = new StringBuilder();
                    String line2 = "";

                    while ((line2 = templateContent.readLine()) != null && templateContent.ready()) {
                        stringlB.append(line2 + "\r\n");
                    }

                    String nome = "$" + file.getName().toUpperCase();

                    esqueleto = esqueleto.replace(nome, stringlB.toString());

                    BufferedWriter outService = new BufferedWriter(new FileWriter("blocks/" + algorithm + "/src/main/java/MyService.java"));
                    outService.write(esqueleto);
                    outService.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createServiceXML(List<ApplicationData> data, ApplicationService application, ApplicationConverterService converter) throws JDOMException, IOException, ApplicationServiceException, UserNotAuthorizedException, ApplicationConverterException {

        HashMap<String, String> mapProperties = new HashMap<String, String>();

        //lendo arquivo xml
        File f = new File("/home/bcorrea/Desktop/block.xml");

        //Criamos uma classe SAXBuilder que vai processar o XML4  
        SAXBuilder sb = new SAXBuilder();

        //Este documento agora possui toda a estrutura do arquivo.  
        Document d = sb.build(f);

        //Recuperamos o elemento root  
        Element root = d.getRootElement();

        for (ApplicationData app : data) {

            String aName = app.getName();
            Element name = root.getChild("Name");
            name.setText(aName);
            Element category = root.getChild("Category");
            category.setText("CSGrid");
            Element props = root.getChild("Properties");
            Element inputs = root.getChild("Inputs");
            Element outputs = root.getChild("Outputs");
            
            

            List<ApplicationData.VersionData> versions = app.getVersions();

            for (ApplicationData.VersionData version : versions) {
                String vName = version.getVersion();
                InputStream stream = application.config(uuid, aName, vName);
                Representation representation = converter.convert(stream, null);

                String block = aName + "_" + vName;
                System.out.println(aName + " " + vName);
                ChangePom(block, "/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks/" +  block + "/pom.xml");

                //representation.getGroups().get(0).getParameters().get(0).;
                for (Group group : representation.getGroups()) {

                    System.out.println(group.getLabel());

                    for (Parameter parameter : group.getParameters()) {

                        Element element = null;

                        if (parameter instanceof TextInteger) {

                            mapProperties.put(parameter.getName(), "int");

                            element = new Element("Property");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "Integer");
                            element.setAttribute("default", parameter.getValue());
                            props.addContent(element);
                        } else if (parameter instanceof TextDouble) {

                            mapProperties.put(parameter.getName(), "double");
                            element = new Element("Property");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "Double");
                            element.setAttribute("default", parameter.getValue());
                            props.addContent(element);

                        } else if (parameter instanceof Text) {

                            mapProperties.put(parameter.getName(), "String");
                            element = new Element("Property");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "String");
                            element.setAttribute("default", parameter.getValue());
                            props.addContent(element);

                        } else if (parameter instanceof Checkbox) {

                            mapProperties.put(parameter.getName(), "boolean");
                            element = new Element("Property");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "Boolean");
                            element.setAttribute("default", parameter.getValue());
                            props.addContent(element);
                        } else if (parameter instanceof InputFile) {

                            mapProperties.put(parameter.getName(), "String");
                            element = new Element("Input");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "file-wrapper");
                            inputs.addContent(element);

                        } else if (parameter instanceof OutputFile) {

                            mapProperties.put(parameter.getName(), "String");
                            element = new Element("Output");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "file-wrapper");
                            outputs.addContent(element);

                        }/*else{
                        
                        throw new RuntimeException("parametro" + parameter.getName() + " sem suporte ");
                        
                        }*/

                    }

                }

                XMLOutputter xmlOutput = new XMLOutputter();

                // display nice nice
                xmlOutput.setFormat(Format.getPrettyFormat());
                String blockname = "block" + aName + vName;
                //xmlOutput.output(d, new FileWriter("/home/bcorrea/Desktop/blocks/block_"+blockname + ".xml"));
                xmlOutput.output(d, new FileWriter("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks/" + block + "/src/main/resources/service" + ".xml"));
                ///home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks/DANCE_2.0.0/src/main/resources

                String propriedades = createProperties(mapProperties);

                File javaFile = new File("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks/" + block + "//src/main/java/MyService.java");

                BufferedReader content = new BufferedReader(new FileReader(javaFile));
                StringBuilder stringlB = new StringBuilder();
                String line = "";

                while ((line = content.readLine()) != null && content.ready()) {
                    stringlB.append(line + "\r\n");
                }
                
                String squeleton = stringlB.toString();
                
                squeleton = squeleton.replace( "$PROPERTIES", propriedades);
                
                
                 BufferedWriter outService = new BufferedWriter(new FileWriter("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks/" + block + "//src/main/java/MyService.java"));
                 outService.write(squeleton);
                 outService.close();

            }
        }

    }
    
    
    private void ChangePom(String nameProject, String path) throws JDOMException{
        
        try {
		String filepath = path;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = docBuilder.parse(filepath);

		// Get the root element
		org.w3c.dom.Node company = doc.getFirstChild();

		// Get the staff element , it may not working if tag has spaces, or
		// whatever weird characters in front...it's better to use
		// getElementsByTagName() to get it directly.
		// Node staff = company.getFirstChild();

		// Get the staff element by tag name directly
		org.w3c.dom.Node staff = doc.getElementsByTagName("artifactId").item(0);
                staff.setTextContent(nameProject);
		

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
 		StreamResult result = new StreamResult(new File(filepath));
		transformer.transform(source, result);

		System.out.println("Done");

	   } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	   } catch (TransformerException tfe) {
		tfe.printStackTrace();
	   } catch (IOException ioe) {
		ioe.printStackTrace();
	   } catch (SAXException sae) {
		sae.printStackTrace();
	   }
        
        
    
    
    }

    private String createProperties(HashMap<String, String> mapProperties) {

        Iterator it = mapProperties.entrySet().iterator();
        List<String> properties = new ArrayList();
        StringBuilder atributos = new StringBuilder();
       
        

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            if( ( pair.getValue().equals("int") )  || (pair.getValue().equals("double"))  ){
            
                String line = "private static final " + pair.getValue() + " " + pair.getKey() + " = " + " 0";
            }else if(pair.getValue().equals("boolean")){
            
                String line = "private static final " + pair.getValue() + " " + pair.getKey() + " = " + " false";
            
            }else{
            
            String line = "private static final " + pair.getValue() + " " + pair.getKey() + " = " + "\"" + pair.getKey() + "\"";
            atributos.append(line);
            }
            atributos.append(";\r\n");

        }

        System.out.println(atributos.toString());
        return atributos.toString();

    }

        //home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks/DANCE_2.0.0/src/main/java
        /*File directory = new File("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/blocks");
     File[] files = directory.listFiles();

     for (File f : files) {

     String path = "/src/main/java" + f.getName();

     BufferedReader content = new BufferedReader(new FileReader(path));
     StringBuilder stringlB = new StringBuilder();
     String line2 = "";

     while ((line2 = content.readLine()) != null && content.ready()) {
     stringlB.append(line2 + "\r\n");
     }

     String service = stringlB.toString();

     service = service.replace(line2, service)

     }*/
}


