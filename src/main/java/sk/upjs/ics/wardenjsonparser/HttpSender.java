package sk.upjs.ics.wardenjsonparser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author tomas
 */
public class HttpSender implements Runnable {

    //private static String token;// = "SWS-Token \"sws-token\"=\"OG/OpLD8xgKz8lF0g5h6rLTSfqkIfpaGVs+/iOFVBj3tClGuu4xDhCJTvxadJpV3mB7U0FCiwTJRCxujaanm6Q==\"";
    private static String newstreamUrl = "http://tc371hdbexpress:9093/1/workspaces/default/projects/warden_streaming/streams/NEWSTREAM";

    private static String eventUrl = "http://tc371hdbexpress:9093/1/workspaces/default/projects/warden_streaming/streams/EVENT_STREAM";
    private static String sourceOrTargetUrl = "http://tc371hdbexpress:9093/1/workspaces/default/projects/warden_streaming/streams/SOURCE_OR_TARGET_STREAM";
    private static String attachUrl = "http://tc371hdbexpress:9093/1/workspaces/default/projects/warden_streaming/streams/ATTACH_STREAM";
    private static String nodeUrl = "http://tc371hdbexpress:9093/1/workspaces/default/projects/warden_streaming/streams/NODE_STREAM";
    private static String arrayUrl = "http://tc371hdbexpress:9093/1/workspaces/default/projects/warden_streaming/streams/ARRAY_STREAM";

    private String eventString;
    private String sourceOrTargetString;
    private String attachString;
    private String nodeString;
    private String arrayString;
    String eventToken;
    String sourceOrTargetToken;
    String attachToken;
    String nodeToken;
    String arrayToken;
    int packetNumber;

    HttpClient httpClient = HttpClients.createDefault();

    public HttpSender(String eventString, String sourceOrTargetString, String attachString, String nodeString, String arrayString,
            String eventToken, String sourceOrTargetToken, String attachToken, String nodeToken, String arrayToken, int packetNumber) {
        this.eventString = eventString;
        this.sourceOrTargetString = sourceOrTargetString;
        this.attachString = attachString;
        this.nodeString = nodeString;
        this.arrayString = arrayString;
        this.eventToken = eventToken;
        this.sourceOrTargetToken = sourceOrTargetToken;
        this.attachToken = attachToken;
        this.nodeToken = nodeToken;
        this.arrayToken = arrayToken;
        this.packetNumber = packetNumber;
    }

    public HttpSender() {

    }

    String getToken(String streamName) {
        String authorizationUrl = "http://tc371hdbexpress:9093/1/authorization/";
        HttpPost httpPost = new HttpPost(authorizationUrl);

        String tokenBody = "[\n"
                + "{ \"privilege\":\"write\", \"resourceType\":\"stream\", \"resource\":\"default/warden_streaming/" + streamName + "\"}\n"
                + "]";
        StringEntity sentity = new StringEntity(tokenBody, "UTF-8");
        httpPost.setEntity(sentity);
        httpPost.setHeader("authorization", "Basic U1lTVEVNOlRDLmFkbWluLjEyMzQ=");

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String string = EntityUtils.toString(entity);
            string = string.substring(15, string.length() - 3);

            if (streamName.equals("EVENT_STREAM")) {
                eventToken = "SWS-Token \"sws-token\"=\"" + string + "\"";
                return eventToken;
            }
            if (streamName.equals("SOURCE_OR_TARGET_STREAM")) {
                sourceOrTargetToken = "SWS-Token \"sws-token\"=\"" + string + "\"";
                return sourceOrTargetToken;
            }
            if (streamName.equals("ATTACH_STREAM")) {
                attachToken = "SWS-Token \"sws-token\"=\"" + string + "\"";
                return attachToken;
            }
            if (streamName.equals("NODE_STREAM")) {
                nodeToken = "SWS-Token \"sws-token\"=\"" + string + "\"";
                return nodeToken;
            }
            if (streamName.equals("ARRAY_STREAM")) {
                arrayToken = "SWS-Token \"sws-token\"=\"" + string + "\"";
                return arrayToken;
            }

        } catch (IOException ex) {
            Logger.getLogger(HttpSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void sendAll() {
        boolean eventIsOk = true;
        if (eventString.length() > 0) {
            eventIsOk = sendString(eventString, eventUrl, eventToken);
        }
        if (eventIsOk) {
            boolean sourceOrTargetIsOk = true;
            if (sourceOrTargetString.length() > 0) {
                sourceOrTargetIsOk = sendString(sourceOrTargetString, sourceOrTargetUrl, sourceOrTargetToken);
            }
            boolean attachIsOk = true;
            if (attachString.length() > 0) {
                attachIsOk = sendString(attachString, attachUrl, attachToken);
            }
            boolean nodeIsOk = true;
            if (nodeString.length() > 0) {
                nodeIsOk = sendString(nodeString, nodeUrl, nodeToken);
            }

            if (sourceOrTargetIsOk && attachIsOk && nodeIsOk) {
                if (arrayString.length() > 0) {
                    sendString(arrayString, arrayUrl, arrayToken);
                }
            }
        }
    }

    public boolean sendString(String string, String url, String token) {
        HttpPost httppost = new HttpPost(url);
        httppost.setProtocolVersion(HttpVersion.HTTP_1_0);

        httppost.setHeader("authorization", token);

        StringEntity entity = null;
        try {
            string = "[\n" + string + "\n]}";
            //System.out.println(string);
            entity = new StringEntity(string);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HttpSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        httppost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httppost);
            //System.out.println(packetNumber);
            //System.out.println(response.getStatusLine().getStatusCode());
            //vypis(response);
           
            if (response.getStatusLine().getStatusCode() == 200) {
                //System.out.println("podarilo sa " + url);
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("nepodarilo sa " + url);
       // System.out.println("chuncked " + entity.isChunked());
       // System.out.println(eventString);
        return false;
    }

    private void vypis(HttpResponse response) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder total = new StringBuilder();
            String line = null;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            r.close();
            System.out.println(total.toString());
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        //HttpSender httpSender = new HttpSender();
        String data = "[\n"
                + "	[ \"insert\" , \"Recon.Scanning34\" , \"2017-12-13T11:14:30Z\" , 23 , \"195.113.253.93\", \"tcp\" ] \n]";
        //httpSender.getToken();
        //Boolean b = httpSender.sendString(data, newstreamUrl, token);
        //System.out.println(b);

    }

    @Override
    public void run() {
        sendAll();
    }

}
