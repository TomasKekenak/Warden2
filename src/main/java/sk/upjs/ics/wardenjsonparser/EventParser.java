package sk.upjs.ics.wardenjsonparser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Iterator;
import java.util.Scanner;


public class EventParser {

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader("dataset.txt")) {

            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray eventList = (JSONArray) obj;
            System.out.println(eventList);

            //Iterate over event array
            eventList.forEach(event -> parseEventObject((JSONObject) event));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void parseEventObject(JSONObject event) {

        JSONArray categoryArray = (JSONArray) event.get("Category");
        System.out.println(categoryArray);

        Iterator<String> iterator = categoryArray.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        //get the detect time of the event
        String detectTime = (String) event.get("DetectTime");
        System.out.println(detectTime);

    }

}
