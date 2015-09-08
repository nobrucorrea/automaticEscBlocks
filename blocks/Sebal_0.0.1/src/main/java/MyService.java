import com.connexience.server.workflow.*;
import com.connexience.server.workflow.service.DataProcessorException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MyService implements WorkflowBlock {
private String uuid;
private String user;
private String password;
private String loginURL;
private String outputFileName;
private String submissionDirectory;
private File saida;
List<String> outputFileList = new ArrayList();
List<String> inputFileList = new ArrayList();

@Override
public void preExecute(BlockEnvironment env) throws MalformedURLException, IOException, JSONException, InterruptedException {
        URL url;

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties prop = new Properties();
        prop.load(inputStream);
        this.user = prop.getProperty("CSGRID_USER");
        this.password = prop.getProperty("CSGRID_PASSWORD");
        this.loginURL = prop.getProperty("LOGIN_URL");

        url = new URL(this.loginURL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // for https
        //HttpsURLConnection connection = (HttpsURLConnection)
        // url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        StringBuilder parameters = new StringBuilder();
        parameters.append("username=");
        parameters.append(this.user);
        parameters.append("&");
        parameters.append("password=");
        parameters.append(this.password);

        //String parameters = "username=brunos&password=ee1212";
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parameters.toString());
        wr.flush();
        wr.close();
        int code = connection.getResponseCode();
        // 200 is http success code
        if (code == 200) {
            System.out.println("Login OK");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = "";
            String line = null;
            while ((line = in.readLine()) != null) {
                response += line;
            }
            in.close();
            JSONObject json = new JSONObject(response);
            int codeUuid = json.getInt("code");
            System.out.println(codeUuid);
            if (codeUuid == 200) {

                this.uuid = json.getString("uuid");
                System.out.println("uuid" + this.uuid);

                
                //creating submission directory to storing submission data
                //teh directory name is the date and time of submission
                createSubmissionDirectory();
                Thread.sleep(5000);

            } else {

                throw new RuntimeException("login failure");

            }

        }
    }


private void createSubmissionDirectory() throws MalformedURLException, IOException, JSONException {

        /*
         uuid - Universally unique identifier of the user.
         project - A project owned by the user.
         parents - (optional) The parents directory. Null if there is no parents. Parents are separated using the '/' character.
         file - The file name.
         directory - True if it is a directory or false if it is a file.   
         */
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        this.submissionDirectory = sdf.format(cal.getTime());
        
        URL url = new URL("http://sinapad-01.sinapad.lncc.br:8080/sinapad-rest/rest/file/create");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // for https
        //HttpsURLConnection connection = (HttpsURLConnection)
        // url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        StringBuilder parameters = new StringBuilder();
        parameters.append("uuid=");
        parameters.append(this.uuid);
        parameters.append("&");
        parameters.append("project=eSC&");
        //parameters.append("parents=/&");
        parameters.append("file=");
        parameters.append(this.submissionDirectory);
        parameters.append("&");
        parameters.append("directory=true");
        

        //String parameters = "username=brunos&password=ee1212";
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parameters.toString());
        wr.flush();
        wr.close();
        int code = connection.getResponseCode();
        // 200 is http success code
        if (code == 200) {

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = "";
            String line = null;
            while ((line = in.readLine()) != null) {
                response += line;
            }
            in.close();
            JSONObject json = new JSONObject(response);
            int codeUuid = json.getInt("code");
            System.out.println(codeUuid);
            if (codeUuid == 200) {

                System.out.println("creating submission directory  OK");

            } else {

                throw new RuntimeException("creating submission directory failure - CODE " + codeUuid);

            }

        }

}





@Override
public void execute(BlockEnvironment env, BlockInputs inputs, BlockOutputs outputs) throws DataProcessorException, IOException, JSONException, MalformedURLException, InterruptedException {
        //create output file
        //createOutputFile();
        
        //UPLOD FILES TO CSGRID
	List<String> fileNames = null;
        for(String inputFile: inputFileList ){
    
    	List<File> inputFiles = inputs.getInputFiles(inputFile);

        boolean uploadOK = true;
        fileNames = new ArrayList();


		for (File file : inputFiles) {

		    if (!uploadFile(file)) {

		        uploadOK = false;
		        throw new RuntimeException("UPLOAD PROBLEM");

		    } else {

		        fileNames.add(file.getName());
		    }

		}
	}
    	
    
    

        Thread.sleep(5000);
        //RUN ALGORTIHM
        //if (uploadOK) {
        System.out.println("VOU EXECUTAR AGORA");
        URL runURL = new URL("http://sinapad-01.sinapad.lncc.br:8080/sinapad-rest/rest/job-submission/run");
        HttpURLConnection runConnection = (HttpURLConnection) runURL.openConnection();
        runConnection.setRequestMethod("POST");
        runConnection.setDoOutput(true);

        // String runParameters = "uuid=" + uuid + "&project=eSC&application=eSC&version=1.0.0&args=INTERVAL::20";
        StringBuilder runParameters = new StringBuilder();
        runParameters.append("uuid=");
        runParameters.append(this.uuid);
        runParameters.append("&");
        runParameters.append("project=eSC&");
        runParameters.append("application=mc2&");
        runParameters.append("version=2.0.0&");
        //runParameters.append("args=INPUT::/exports.sh");
        runParameters.append("args=INPUT::/");
        runParameters.append(this.submissionDirectory);
        runParameters.append("/");
        runParameters.append(fileNames.get(0));
        runParameters.append(";");
        //runParameters.append("OUTPUT::/saidaesc.txt");
        runParameters.append("OUTPUT::/");
        runParameters.append(this.submissionDirectory);
        runParameters.append("/");
        runParameters.append(this.outputFileName);
        
        System.out.println(runParameters.toString());

        DataOutputStream wr = new DataOutputStream(runConnection.getOutputStream());
        wr.writeBytes(runParameters.toString());
        wr.flush();
        wr.close();

        String runResponse = "";
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(runConnection.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                runResponse += line;
            }
            in.close();
        }

        JSONObject runJson = new JSONObject(runResponse);

        String codeSubmission = runJson.getString("code");

        System.out.println("CODE SUBMISSION RUN " + codeSubmission);

        if (codeSubmission.equals("200")) {

            String jobId = String.valueOf(runJson.getString("jobId"));
            System.out.println("JOBID " + jobId);

            if (isJobFinished(jobId)) {

                System.out.println("Job Finished");

                //DOWNLOAD RESULT FILE
                downloadFile();
                System.out.println("Download executado");
                //insert result in output block
                
                
                
                
                
                
                
            } else {

                throw new RuntimeException("JOB Status Failure");

            }
        } else {

            throw new RuntimeException("RUN SUBMISSION ERROR - CODE " + codeSubmission);
        }
        
        //outputs.setOutputFile(Output_OUTPUT_1, saida);       

    }

    private boolean isJobFinished(String jobId) throws MalformedURLException, IOException, JSONException, InterruptedException {

        String status = "";

        do {
            Thread.sleep(5000);

            URL statusURL = new URL("http://sinapad-01.sinapad.lncc.br:8080/sinapad-rest/rest/job-monitoring/get");
            HttpURLConnection statusConnection = (HttpURLConnection) statusURL.openConnection();
            statusConnection.setRequestMethod("POST");
            statusConnection.setDoOutput(true);

            String statusParameters = "uuid=" + uuid + "&project=eSC&jobId=" + jobId;
            {
                DataOutputStream wr = new DataOutputStream(statusConnection.getOutputStream());
                wr.writeBytes(statusParameters);
                wr.flush();
                wr.close();
            }

            String statusResponse = "";
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(statusConnection.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    statusResponse += line;
                }
                in.close();
            }
            JSONObject statusJson = new JSONObject(statusResponse);
            System.out.println("GET STATUS CODE " + statusJson.getString("code"));
            status = statusJson.getString("status");

        } while (!"DONE".equals(status) && !"FAILED".equals(status) && !"UNDETERMINED".equals(status));

        if (status.equals("DONE")) {
            return true;
        } else {

            return false;
        }

    }


@Override
public void postExecute(BlockEnvironment env) throws Exception {
        URL url = new URL("http://sinapad-01.sinapad.lncc.br:8080/sinapad-rest/rest/authentication/logout");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        String parameters = "uuid=" + uuid;
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parameters);
        wr.flush();
        wr.close();
}


private boolean uploadFile(File file) throws IOException, JSONException {

        System.out.println("cheguei no UPLOAD");
        /*
         uuid - Universally unique identifier of the user.
         project - A project owned by the user.
         parents - (optional) The parents directory. Null if there is no parents. Parents are separated using the '/' character.
         file - The file.
         content - The file content.
         override - True if the file already exists and want to override.uuid - Universally unique identifier of the user.
         project - A project owned by the user.
         parents - (optional) The parents directory. Null if there is no parents. Parents are separated using the '/' character.
         is - The file.
         content - The file content. optional
         override - True if the file already exists and want to override. - optional
         */
        //File file = new File("/Users/operador/Documents/cs.txt");
        //variaveis para montagem do POST
        String attachmentName = "file"; // <- em vez de 'is', use file como nome do parâmetro.
        String attachmentFileName = file.getName();
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String project = "eSC";

        byte[] arr = Files.readAllBytes(file.toPath());

        //montagem do POST
        URL urlUpload = new URL("http://sinapad-01.sinapad.lncc.br:8080/sinapad-rest/rest/file/upload");
        HttpURLConnection uploadConnection = (HttpURLConnection) urlUpload.openConnection();
        uploadConnection.setRequestMethod("POST");
        uploadConnection.setDoOutput(true);
        uploadConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        uploadConnection.setRequestProperty("Accept", "application/json");

        DataOutputStream wrUpload = new DataOutputStream(uploadConnection.getOutputStream());
        wrUpload.writeBytes(twoHyphens + boundary + crlf);
        wrUpload.writeBytes("Content-Disposition: form-data; name=\"uuid\"" + crlf);
        wrUpload.writeBytes("Content-Type: text/plain;charset=UTF-8" + crlf + "Content-Length: " + uuid.length() + crlf + crlf);
        wrUpload.writeBytes(uuid + crlf);

        //um blocodesse para cada variavel    
        //project
        wrUpload.writeBytes(twoHyphens + boundary + crlf);
        wrUpload.writeBytes("Content-Disposition: form-data; name=\"project\"" + crlf);
        wrUpload.writeBytes("Content-Type: text/plain;charset=UTF-8" + crlf + "Content-Length: " + project.length() + crlf + crlf);
        wrUpload.writeBytes(project + crlf);
        
        //parents
        wrUpload.writeBytes(twoHyphens + boundary + crlf);
        wrUpload.writeBytes("Content-Disposition: form-data; name=\"parents\"" + crlf);
        wrUpload.writeBytes("Content-Type: text/plain;charset=UTF-8" + crlf + "Content-Length: " + this.submissionDirectory.length() + crlf + crlf);
        wrUpload.writeBytes(this.submissionDirectory + crlf);
        

        //um bloco dese para cada tipo file
        wrUpload.writeBytes(twoHyphens + boundary + crlf);
        //wrUpload.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
        wrUpload.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
        wrUpload.writeBytes(crlf);
        wrUpload.write(arr);
        wrUpload.writeBytes(crlf);
        wrUpload.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
        wrUpload.flush();
        wrUpload.close();

        int codeUpload = uploadConnection.getResponseCode();
        System.out.println("CODIGO UPLOAD" + codeUpload);

        String responseUpload = "";
        String lineUpload = null;

        // 200 is http success code
        if (codeUpload == 200) {
            try (BufferedReader inUpload = new BufferedReader(new InputStreamReader(uploadConnection.getInputStream()))) {

                while ((lineUpload = inUpload.readLine()) != null) {
                    responseUpload += lineUpload;
                }
            }
            JSONObject jsonUpload = new JSONObject(responseUpload);
            System.out.println("code response" + jsonUpload.getInt("code"));
            if (jsonUpload.getInt("code") == 200) {

                System.out.println("SUCCESS!! ACABOU UPLoad");
                return true;
            }
        }

        return false;
    }

private void downloadFile() throws MalformedURLException, IOException, JSONException {

        /*
         uuid - Universally unique identifier of the user.
         project - A project owned by the user.
         parents  - (optional) The parents directory. Null if there is no parents. Parents are separated using the '/' character.
         file - The file name.
         Returns:
         A streaming for download the file and a result containing the code of the result. 
         OK - if success;        
         */
        URL url = new URL("http://sinapad-01.sinapad.lncc.br:8080/sinapad-rest/rest/file/download");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        StringBuilder parameters = new StringBuilder();
        parameters.append("uuid=");
        parameters.append(this.uuid);
        parameters.append("&");
        parameters.append("project=eSC&");
        parameters.append("parents=");
        parameters.append(this.submissionDirectory);
        parameters.append("&");
        parameters.append("file=");
        parameters.append(this.outputFileName);
        
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parameters.toString());
        wr.flush();
        wr.close();

        int submissionCode = connection.getResponseCode();
        System.out.println("submission code " + submissionCode);

        // 200 is http success code
        if (submissionCode == 200) {
            System.out.println("submisssion OK");

            /*BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             String response = "";
             String line = null;
            
             while ((line = in.readLine()) != null) {
             response += line;
             }
             in.close();*/
            InputStream input = connection.getInputStream();

            saida = new File(this.outputFileName);

            byte[] buffer = new byte[4096];
            int n = - 1;

            OutputStream output = new FileOutputStream(saida);

            while ((n = input.read(buffer)) != -1) {
                if (n > 0) {
                    output.write(buffer, 0, n);
                }
            }
            output.close();
        }

    }



}
