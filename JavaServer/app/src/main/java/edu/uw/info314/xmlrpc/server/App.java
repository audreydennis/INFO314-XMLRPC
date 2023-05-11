package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;
import java.io.*;

import static spark.Spark.*;


class Call {
    public String name;
    public List < Object > args = new ArrayList < Object > ();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args) {
        LOG.info("Starting up on port 8080");

        port(8080);

        post("/RPC", (request, response) -> {

            if (!request.uri().equals("/RPC")) {
                response.status(404);
                return "Not Found";
            }

            String host = request.host();
            response.header("Host", host);

            String xmlRequest = request.body();

            Calc calc = new Calc(); 
            Call call = new Call(); 

            String methodName = getMethodName(xmlRequest);
            LOG.info("methodName: " + methodName);
            call.name = methodName; 

            List < Object > i4Values = geti4(xmlRequest);
            if (i4Values== null) {
                response.type("text/xml");
                return buildXmlFaultResponse(3, "illegal argument type");
            }else{
            System.out.println("i4Values: " + i4Values);
            call.args = i4Values; 
            int result = 11;

            if ("add".equals(call.name)) {
                result = calc.add(convertArgsToIntArray(call.args));
            } else if ("subtract".equals(call.name)) {
                int lhs = (int) call.args.get(0);
                int rhs = (int) call.args.get(1);
                result = calc.subtract(lhs, rhs);
            } else if ("multiply".equals(call.name)) {
                result = calc.multiply(convertArgsToIntArray(call.args));
            } else if ("divide".equals(call.name)) {
                int lhs = (int) call.args.get(0);
                int rhs = (int) call.args.get(1);
                if(rhs==0){
                    response.type("text/xml");
                    return buildXmlFaultResponse(1, "divide by zero");
                }else{
                    result = calc.divide(lhs, rhs);
                }
                
            } else if ("modulo".equals(call.name)) {
                int lhs = (int) call.args.get(0);
                int rhs = (int) call.args.get(1);
                if(rhs==0){
                    response.type("text/xml");
                    return buildXmlFaultResponse(1, "divide by zero");
                }else{
                    result = calc.modulo(lhs, rhs);
                }
            } else {
                result = 0;
            }

            String xmlResponse = buildXmlResponse(methodName, result); //testing to get response

            response.type("text/xml");
            return xmlResponse;
            }
        });

        put("/RPC", (request, response) -> {
            response.status(405);
            return "Method Not Allowed";
        });

        get("/RPC", (request, response) -> {
            response.status(405);
            return "Method Not Allowed";
        });

        delete("/RPC", (request, response) -> {
            response.status(405);
            return "Method Not Allowed";
        });

    }

    public static List < Object > geti4(String xmlRequest) {
        List < Object > i4Values = new ArrayList < > ();

        int startIndex = 0;
        int endIndex;

        while ((startIndex = xmlRequest.indexOf("<i4>", startIndex)) != -1) {
            startIndex += 4; 
            endIndex = xmlRequest.indexOf("</i4>", startIndex);
            
            if (endIndex != -1) {
                String value = xmlRequest.substring(startIndex, endIndex);

                try {
                    int intValue = Integer.parseInt(value);
                    i4Values.add(intValue);
                } catch (NumberFormatException e) {
                    return null; 
                }

                startIndex = endIndex + 5; 
            } else {
                break; 
            }
        }
        return i4Values;
    }

    public static String getMethodName(String xmlRequest) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(xmlRequest));
            String line;

            // Read each line of the XML request until methodName
            while ((line = reader.readLine()) != null) {
                if (line.contains("<methodName>")) {
                    int start = line.indexOf("<methodName>") + 12;
                    int end = line.indexOf("</methodName>");
                    String value = line.substring(start, end);
                    return value;
                }
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //build same XML as Client
    private static String buildXmlResponse(String methodName, int result) {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\"?>");
        xmlBuilder.append("<methodResponse>");
        xmlBuilder.append("<params>");
        xmlBuilder.append("<param>");
        xmlBuilder.append("<value>").append(getXmlRpcValue(result)).append("</value>");
        xmlBuilder.append("</param>");
        xmlBuilder.append("</params>");
        xmlBuilder.append("</methodResponse>");

        return xmlBuilder.toString();
    }

    private static String buildXmlFaultResponse(int faultCode, String faultString) {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\"?>");
        xmlBuilder.append("<methodResponse>");
        xmlBuilder.append("<fault>");
        xmlBuilder.append("<value>");
        xmlBuilder.append("<struct>");
        xmlBuilder.append("<member>");
        xmlBuilder.append("<name>faultCode</name>");
        xmlBuilder.append("<value><int>").append(faultCode).append("</int></value>");
        xmlBuilder.append("</member>");
        xmlBuilder.append("<member>");
        xmlBuilder.append("<name>faultString</name>");
        xmlBuilder.append("<value><string>").append(faultString).append("</string></value>");
        xmlBuilder.append("</member>");
        xmlBuilder.append("</struct>");
        xmlBuilder.append("</value>");
        xmlBuilder.append("</fault>");
        xmlBuilder.append("</methodResponse>");

        return xmlBuilder.toString();
    }

    private static String getXmlRpcValue(int value) {
        return "<i4>" + value + "</i4>";
    }

    private static int[] convertArgsToIntArray(List < Object > args) {
        int[] intArray = new int[args.size()];
        for (int i = 0; i < args.size(); i++) {
            intArray[i] = (int) args.get(i);
        }
        return intArray;
    }
}