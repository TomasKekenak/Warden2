package sk.upjs.ics.wardenjsonparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author tomas
 */
public class Starter {

    static int c = 0;
    static int i = 0;
    static int packetNumber = 0;
    static int maxNumberOfThreads = 10;
    static ExecutorService executorService;

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        executorService = Executors.newFixedThreadPool(maxNumberOfThreads);

        String datasetName = "/Users/tomas/NetBeansProjects/dataset.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetName))) {
            String line;
            EventParserScanner eps = new EventParserScanner();
            HttpSender hs = new HttpSender();
            String eventToken = hs.getToken("EVENT_STREAM");
            String sourceOrTargetToken = hs.getToken("SOURCE_OR_TARGET_STREAM");
            String attachToken = hs.getToken("ATTACH_STREAM");
            String nodeToken = hs.getToken("NODE_STREAM");
            String arrayToken = hs.getToken("ARRAY_STREAM");

            while ((line = reader.readLine()) != null) {
                //parse one event
                Object obj = jsonParser.parse(line);
                JSONObject event = (JSONObject) obj;
                eps.parseEvent(event);
                if (c % 1000 == 0) {
                    System.out.println("posielam event c" + c);
                }
                c++;
                long freeMemory = Runtime.getRuntime().freeMemory();
                while (Runtime.getRuntime().freeMemory() < 80000000) {
                    //System.out.println(freeMemory);
                    TimeUnit.SECONDS.sleep(1);
                }

                if (eps.packetIsFull()) {
                    System.out.println("posielam packet c" + i);
                    i++;
                    Starter.sendAll(eps, eventToken, sourceOrTargetToken, attachToken, nodeToken, arrayToken);
                }
                if (c > 100000) {
                    return;
                }
            }
            System.out.println("ahoj");
            System.out.println(eps.eventSB.toString());
            Starter.sendAll(eps, eventToken, sourceOrTargetToken, attachToken, nodeToken, arrayToken);

            /*System.out.println(eps.eventSB.toString());
            System.out.println(eps.arraySB.toString());
            System.out.println(eps.sourceOrTargetSB.toString());
            System.out.println(eps.nodeSB.toString());
            System.out.println(eps.attachSB.toString());*/
        } catch (FileNotFoundException ex) {
            System.out.println("nenasiel sa subor");
        } catch (IOException ex) {
            Logger.getLogger(EventParserScanner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(EventParserScanner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
        }
        executorService.shutdown();
    }

    private static void sendAll(EventParserScanner eps, String eventToken, String sourceOrTargetToken, String attachToken, String nodeToken, String arrayToken) {
        String eventSBString = eps.eventSB.toString();
        String sourceOrTargetSBString = eps.sourceOrTargetSB.toString();
        String attachSBString = eps.attachSB.toString();
        String nodeSBString = eps.nodeSB.toString();
        String arraySBString = eps.arraySB.toString();
        eps.eventSB.setLength(0);
        eps.sourceOrTargetSB.setLength(0);
        eps.attachSB.setLength(0);
        eps.nodeSB.setLength(0);
        eps.arraySB.setLength(0);

        Runnable r = new HttpSender(eventSBString, sourceOrTargetSBString, attachSBString, nodeSBString, arraySBString, eventToken,
                sourceOrTargetToken, attachToken, nodeToken, arrayToken, ++packetNumber);
        executorService.submit(r);

    }

}
