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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author bcorrea
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ConfigurationServiceException, AuthenticationServiceException, ApplicationServiceException, UserNotAuthorizedException, JDOMException, ApplicationConverterException {

        HashMap<String,String> mapProperties = new HashMap<String,String>();
        
        
        ConfigurationService config = new CSGridService();
        InputStream is = new FileInputStream("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/src/main/java/esc-auto.properties");
        Properties properties = new Properties();
        properties.load(is);
        config.configure(properties);

        AuthenticationService authentication = new CSGridAuthenticationService();
        String uuid = authentication.login("brunos", "ee1212", AuthenticationService.Domain.LDAP);

        ApplicationService application = new CSGridApplicationService();
        List<ApplicationData> data = application.list(uuid);

        ApplicationConverterService converter = new CSGridApplicationConverter();

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

                        }else if(parameter instanceof Checkbox){
                        
                            mapProperties.put(parameter.getName(), "boolean");
                            element = new Element("Property");
                            element.setAttribute("name", parameter.getName());
                            element.setAttribute("type", "Bollean");
                            element.setAttribute("default", parameter.getValue());
                            props.addContent(element);
                        }            
                        else if (parameter instanceof InputFile) {

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

                        }
                        

                    }

                }
                
                XMLOutputter xmlOutput = new XMLOutputter();

                // display nice nice
                xmlOutput.setFormat(Format.getPrettyFormat());
                String blockname =  "block"+ aName + vName;
                xmlOutput.output(d, new FileWriter("/home/bcorrea/Desktop/blocks/block_"+blockname + ".xml"));
                

            }
        }

      
            createProperties(mapProperties);
    }
    
    private static void createProperties(HashMap<String,String> mapProperties){
        
           
         Iterator it = mapProperties.entrySet().iterator();
         List<String> properties = new ArrayList();
         properties.add("private String uuid;");
         
         
         while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                
                String line = "private static final " + pair.getValue() + " " + pair.getKey() + " = "  +"\""+pair.getKey()+"\"";
                System.out.println(line);
                properties.add(line);             
                
                
          }
    
    
    }

   
}
