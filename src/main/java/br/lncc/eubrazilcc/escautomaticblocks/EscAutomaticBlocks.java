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
import br.lncc.sinapad.core.application.representation.Representation;
import br.lncc.sinapad.core.data.ApplicationData;
import br.lncc.sinapad.core.data.ApplicationData.VersionData;
import br.lncc.sinapad.core.exception.ApplicationServiceException;
import br.lncc.sinapad.core.exception.AuthenticationServiceException;
import br.lncc.sinapad.core.exception.ConfigurationServiceException;
import br.lncc.sinapad.core.exception.UserNotAuthorizedException;
import br.lncc.sinapad.core.service.application.ApplicationService;
import br.lncc.sinapad.core.service.application.converter.ApplicationConverterService;
import br.lncc.sinapad.core.service.authentication.AuthenticationService;
import br.lncc.sinapad.core.service.authentication.AuthenticationService.Domain;
import br.lncc.sinapad.core.service.configuration.ConfigurationService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author bcorrea
 */
public class EscAutomaticBlocks {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        ConfigurationService config = new CSGridService();
        InputStream is = new FileInputStream("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/src/main/java/esc-auto.properties");
        Properties properties = new Properties();
        properties.load(is);
        config.configure(properties);

        AuthenticationService authentication = new CSGridAuthenticationService();
        String uuid = authentication.login("brunos", "ee1212", Domain.LDAP);

        ApplicationService application = new CSGridApplicationService();
        List<ApplicationData> data = application.list(uuid);

        ApplicationConverterService converter = new CSGridApplicationConverter();

        /*for (ApplicationData app : data) {
         String aName = app.getName();
         List<VersionData> versions = app.getVersions();
         for (VersionData version : versions) {
         String vName = version.getVersion();
         InputStream stream = application.config(uuid, aName, vName);
         Representation representation = converter.convert(stream, null);
                
         System.out.println(representation + " " + aName + " " + vName);
         }
         }*/
        //ApplicationService appService, String uuid
        
        Transform transform = new Transform(data, application, uuid);

    }

     /*public static void main(String[] args) throws FileNotFoundException, IOException, ConfigurationServiceException, AuthenticationServiceException, ApplicationServiceException, UserNotAuthorizedException  {

        ConfigurationService config = new CSGridService();
        InputStream is = new FileInputStream("/home/bcorrea/NetBeansProjects/eSCAutomaticBlocks/src/main/java/esc-auto.properties");
        Properties properties = new Properties();
        properties.load(is);
        config.configure(properties);

        AuthenticationService authentication = new CSGridAuthenticationService();
        String uuid = authentication.login("brunos", "ee1212", Domain.LDAP);

        ApplicationService application = new CSGridApplicationService();
        List<ApplicationData> data = application.list(uuid);

        ApplicationConverterService converter = new CSGridApplicationConverter();

        for (ApplicationData app : data) {
            String aName = app.getName();
            List<VersionData> versions = app.getVersions();
            for (VersionData version : versions) {
                String vName = version.getVersion();
                InputStream stream = application.config(uuid, aName, vName);
                String fileName ="/home/bcorrea/Desktop/configs/" + aName + vName;

                OutputStream os = new FileOutputStream(fileName);

                byte[] buffer = new byte[1024];
                int bytesRead;
                //read from is to buffer
                while ((bytesRead = stream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                //flush OutputStream to write any buffered data to file
                os.flush();
                os.close();
                /*Representation representation = converter.convert(stream, null);
                
                 System.out.println(representation + " " + aName + " " + vName);*/
            /*}
        }

    }*/

}
