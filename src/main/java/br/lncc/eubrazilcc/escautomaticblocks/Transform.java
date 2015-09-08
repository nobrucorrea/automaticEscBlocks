/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.lncc.eubrazilcc.escautomaticblocks;

import br.lncc.sinapad.adapter.csgrid.algorithm.xml.element.Tag.*;

import br.lncc.sinapad.adapter.csgrid.application.converter.CSGridApplicationConverter;
import br.lncc.sinapad.core.application.representation.Group;
import br.lncc.sinapad.core.application.representation.Parameter;
import br.lncc.sinapad.core.application.representation.Representation;
import br.lncc.sinapad.core.data.ApplicationData;
import br.lncc.sinapad.core.exception.ApplicationConverterException;
import br.lncc.sinapad.core.exception.ApplicationServiceException;
import br.lncc.sinapad.core.exception.UserNotAuthorizedException;
import br.lncc.sinapad.core.service.application.ApplicationService;
import br.lncc.sinapad.core.service.application.converter.ApplicationConverterService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
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
public class Transform {

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

    public Transform(List<ApplicationData> apps, ApplicationService appService, String uuid) throws IOException, SAXException, ParserConfigurationException, JDOMException, ApplicationConverterException, ApplicationServiceException, UserNotAuthorizedException {

        this.apps = apps;
        this.appService = appService;
        this.uuid = uuid;

        for (ApplicationData app : apps) {

            String aName = app.getName();

            List<ApplicationData.VersionData> versions = app.getVersions();

            for (ApplicationData.VersionData version : versions) {
                String vName = version.getVersion();
                //InputStream stream = application.config(uuid, aName, vName);
                //Representation representation = converter.convert(stream, null);

                String block = aName + "_" + vName;
                //System.out.println(representation + " " + aName + " " + vName);

                //createEscStructure(block);
                //System.out.println("Struct block " + block + " created");

                transformXMLMyService(aName, vName);
            }
        }
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

        String[] template = {imports, className, properties,  preExecute, execute, posExecute, upload, download, end,"iii"};

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
           
            esqueleto = esqueleto.replace("$END", "}");
                    
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

    private void transformXMLMyService(String algorithm, String version) throws ParserConfigurationException, SAXException, IOException, JDOMException, ApplicationConverterException, ApplicationServiceException, UserNotAuthorizedException {

        InputStream stream = appService.config(this.uuid, algorithm, version);
        ApplicationConverterService converter = new CSGridApplicationConverter();

        Representation representation = converter.convert(stream, null);

        //parametros
        //representation..getGroups().get(0).getParameters().get(0).;
        System.out.println("###########" + algorithm + version + "##############");
        System.out.println("group");
        for (Group group : representation.getGroups()) {

            System.out.println(group.getLabel());

            for (Parameter parameter : group.getParameters()) {
                
                
                
                System.out.println("PARAMETER " + parameter.getClass().toString());

                System.out.println("Type" + parameter.getClass().getName());
                System.out.println("Label  " + parameter.getLabel());
                System.out.println("Name " + parameter.getName());
                System.out.println("Value " + parameter.getValue());
            }

        }

    }
    
    
    private Element csGRid2EscParameter(Parameter parameter){
        
        Element element= null;
        
        if( parameter instanceof TextInteger ){
    
             element = new Element("Property");
             element.setAttribute("name", parameter.getName());
             element.setAttribute("type", "Integer");
             element.setAttribute("default", parameter.getValue());
        }
        else if(parameter instanceof TextDouble ){
                
             element = new Element("Property");
             element.setAttribute("name", parameter.getName());
             element.setAttribute("type", "Double");
             element.setAttribute("default", parameter.getValue());
        
        } else if(parameter instanceof Text){
        
             element = new Element("Property");
             element.setAttribute("name", parameter.getName());
             element.setAttribute("type", "String");
             element.setAttribute("default", parameter.getValue());
    
        } else if(parameter instanceof InputFile){
            
             element = new Element("Input");
             element.setAttribute("name", parameter.getName());
             element.setAttribute("type", "file-wrapper");            
        
        }else if(parameter instanceof OutputFile){
        
            
             element = new Element("Output");
             element.setAttribute("name", parameter.getName());
             element.setAttribute("type", "file-wrapper");            
            
        }
        
        
        
        
        
        
        return element;
    
    
    }
    
    private void readinputxml() throws IOException, JDOMException{
    
    
        File f = new File("/home/bcorrea/Desktop/block.xml");

        //Criamos uma classe SAXBuilder que vai processar o XML4  
        SAXBuilder sb = new SAXBuilder();

        //Este documento agora possui toda a estrutura do arquivo.  
        Document d = sb.build(f);

        //Recuperamos o elemento root  
        Element root = d.getRootElement();

        Element name = root.getChild("Name");
        name.setText("DANCE");
        
        Element category = root.getChild("Category");
        category.setText("CSGrid");

        
        /*
        
        <Property name="Copy Input" default="true" type="Boolean" description="If set, input data will be copied to the output. Otherwise, an empty data set will be produced."/>
        */
        Element inputs = root.getChild("Properties");
        Element input = new Element("Property");
        input.setAttribute("name", "Copy Input2");
        input.setAttribute("type", "Boolean");
        input.setAttribute("default", "true");
        
        inputs.addContent(input);
        

        XMLOutputter xmlOutput = new XMLOutputter();

        // display nice nice
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(d, new FileWriter("/home/bcorrea/Desktop/block_update.xml"));
    }

}
