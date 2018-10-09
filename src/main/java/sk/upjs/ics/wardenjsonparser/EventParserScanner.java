package sk.upjs.ics.wardenjsonparser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.Iterator;
import java.util.UUID;

public class EventParserScanner {
//a
    //maximum size of body/2
    private static int maxSize = 1000000;

    StringBuilder eventSB = new StringBuilder();
    StringBuilder sourceOrTargetSB = new StringBuilder();
    StringBuilder attachSB = new StringBuilder();
    StringBuilder nodeSB = new StringBuilder();
    StringBuilder arraySB = new StringBuilder();

    private static String[] jsonObjectsOfEvent = {"Source", "Target", "Attach", "Node"};

    private static String[] attributeArrayNamesOfEvent = {"AltNames", "CorrelID", "AggrID", "PredID", "RelID", "Ref", "Category"};
    private static String[] attributeNamesOfEvent = {"ID", "Format", "CreateTime", "DetectTime", "EventTime",
        "CeaseTime", "WinStartTime", "WinEndTime", "ConnCount",
        "FlowCount", "PacketCount", "ByteCount", "Confidence", "Description", "Note"};

    private static String[] attributeNamesOfSourceOrTarget = {"Note", "Spoofed", "Imprecise", "Anonymised"};
    private static String[] attributeArrayNamesOfSourceOrTarget = {"Type", "HostName", "IP4", "MAC", "IP6", "Port", "Proto", "URL",
        "Email", "AttachHand", "ASN", "Router", "Netname", "Ref"};

    private static String[] attributeNamesOfAttach = {"Handle", "Size", "Note", "ContentType", "ContentCharset",
        "ContentEncoding", "Content"};
    private static String[] attributeArrayNamesOfAttach = {"FileName", "Type", "Hash", "Ref", "ContentID", "ExternalURI"};

    private static String[] attributeNamesOfNode = {"Name", "AggrWin", "Note"};
    private static String[] attributeArrayNamesOfNode = {"Type", "SW"};

    public EventParserScanner() {
        eventSB = new StringBuilder();
        sourceOrTargetSB = new StringBuilder();
        attachSB = new StringBuilder();
        nodeSB = new StringBuilder();
        arraySB = new StringBuilder();
    }

    public void parseEvent(JSONObject event) {
        if (eventSB.length() > 0) {
            eventSB.append(",\n");
        }
        eventSB.append("{ \"ESP_OPS\":\"i\"");
        Object IDObject = event.get("ID");
        String ID = IDObject.toString();

        //attributes, which are not arrays in the event
        for (String attirbuteName : attributeNamesOfEvent) {
            readWriteAttributeObject(event, "eventSB", attirbuteName);
        }

        eventSB.append("}");

        for (String attributeArrayName : attributeArrayNamesOfEvent) {
            Object arrayO = event.get(attributeArrayName);
            if (arrayO != null) {
                JSONArray array = (JSONArray) arrayO;
                readWriteArrayObject(array, ID, "Event", attributeArrayName);
            }
        }

        for (String jsonObjectString : jsonObjectsOfEvent) {
            JSONArray jsonA = (JSONArray) event.get(jsonObjectString);
            if (jsonA != null) {
                for (Object object : jsonA) {
                    JSONObject jsonO = (JSONObject) object;
                    parseObject(jsonO, jsonObjectString, ID);
                }
            }
        }
    }

    private void parseObject(JSONObject object, String type, String eventID) {
        String[] attributes = null;
        String[] attributeArrayNames = null;
        StringBuilder builder = new StringBuilder();
        String builderName = "";
        int sourceOrTarget = 0;

        if (type.equals("Source") || type.equals("Target")) {
            attributes = attributeNamesOfSourceOrTarget;
            attributeArrayNames = attributeArrayNamesOfSourceOrTarget;
            builder = sourceOrTargetSB;
            builderName = "sourceOrTargetSB";
            if (type.equals("Source")) {
                sourceOrTarget = 1;
            } else {
                sourceOrTarget = 2;
            }
        }
        if (type.equals("Attach")) {
            attributes = attributeNamesOfAttach;
            attributeArrayNames = attributeArrayNamesOfAttach;
            builder = attachSB;
            builderName = "attachSB";
        }
        if (type.equals("Node")) {
            attributes = attributeNamesOfNode;
            attributeArrayNames = attributeArrayNamesOfNode;
            builder = nodeSB;
            builderName = "nodeSB";
        }

        if (builder.length() > 0) {
            builder.append(",\n");
        }

        builder.append("{ \"ESP_OPS\":\"i\"");
        builder.append(", \"EVENT_ID_PK\": \"");
        builder.append(eventID);
        builder.append("\"");

        String ID = UUID.randomUUID().toString();
        builder.append(", \"ID_TEMPORARY\": \"");
        builder.append(ID);
        builder.append("\"");

        if (sourceOrTarget == 1) {
            builder.append(", \"SOURCE_OR_TARGET\": \"");
            builder.append("source");
            builder.append("\"");
        }
        if (sourceOrTarget == 2) {
            builder.append(", \"SOURCE_OR_TARGET\": \"");
            builder.append("target");
            builder.append("\"");
        }

        for (String attributeName : attributes) {
            readWriteAttributeObject(object, builderName, attributeName);
        }

        builder.append("}");

        for (String attributeArrayName : attributeArrayNames) {
            Object arrayO = object.get(attributeArrayName);
            if (arrayO != null) {
                JSONArray array = (JSONArray) arrayO;
                readWriteArrayObject(array, ID, type, attributeArrayName);
            }
        }

    }

    private void readWriteArrayObject(JSONArray array, String ID, String type, String tableName) {
        Iterator<Object> iterator = array.iterator();
        while (iterator.hasNext()) {
            if (arraySB.length() > 0) {
                arraySB.append(",\n");
            }
            String string = iterator.next().toString();
            boolean isInteger = isNumberOrBoolean(string);

            arraySB.append("[ \"insert\", \"");
            arraySB.append(ID);
            arraySB.append("\", \"");
            arraySB.append(type);
            arraySB.append("\", \"");
            arraySB.append(tableName);
            arraySB.append("\", \"");
            arraySB.append(string);
            arraySB.append("\" ]");
        }
    }

    private String readWriteAttributeObject(JSONObject jSONObject, String SBname, String attributeName) {
        Object attributeValueObject = jSONObject.get(attributeName);
        if (attributeValueObject == null) {
            return null;
        }

        String attributeValue = attributeValueObject.toString();

        //data su cudne 
        boolean isInteger = isNumberOrBoolean(attributeValue);
        if (attributeName.contains("Time")) {
            attributeValue = attributeValue.substring(0, 10) + "T" + attributeValue.substring(11, 19) + "Z";
        }
        if (attributeValue.contains("\"")) {
            attributeValue = "m";
        }

        StringBuilder sb = null;
        if (SBname.equals("eventSB")) {
            sb = eventSB;
        }
        if (SBname.equals("sourceOrTargetSB")) {
            sb = sourceOrTargetSB;
        }
        if (SBname.equals("attachSB")) {
            sb = attachSB;
        }
        if (SBname.equals("nodeSB")) {
            sb = nodeSB;
        }
        sb.append(", \"");
        sb.append(attributeName);
        sb.append("\": ");
        if (!isInteger) {
            sb.append("\"");
        }
        sb.append(attributeValue);
        if (!isInteger) {
            sb.append("\"");
        }
        return attributeValue;
    }

    boolean packetIsFull() {
        if (eventSB.length() > maxSize
                || sourceOrTargetSB.length() > maxSize
                || attachSB.length() > maxSize
                || nodeSB.length() > maxSize
                || arraySB.length() > maxSize) {
            return true;
        }
        return false;
    }

    public static boolean isNumberOrBoolean(String str) {
        int length = str.length();
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return false;
        }
        if (str.contains("true") || str.contains("false")) {
            return true;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if ((c < '0' || c > '9') && c != '.') {
                return false;
            }
        }

        return true;
    }
    
    private String messageType = "transaction";
    public void initializeStringBuilders(){
        eventSB.setLength(0);
        sourceOrTargetSB.setLength(0);
        attachSB.setLength(0);
        nodeSB.setLength(0);
        arraySB.setLength(0);
        
        eventSB.append("{\"messageBlockType\" : \"");
        eventSB.append(messageType);
        eventSB.append( "\",\n \"data\" :");

    }

}
