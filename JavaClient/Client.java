import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import java.net.http.HttpResponse.BodyHandlers;

import org.w3c.dom.Node;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */

public class Client {
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static void main(String... args) throws Exception {
        String server = args[0];
        int port = Integer.parseInt(args[1]);

        HttpClient httpClient = HttpClient.newHttpClient();
        String userAgent = "GroupName"; // confused as to what this is supposed to be

        Object subtractResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "subtract", 12, 6);
        System.out.println("subtract(12, 6) = " + subtractResult);

        Object multiplyResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "multiply", 3, 4);
        System.out.println("multiply(3, 4) = " + multiplyResult);

        Object divideResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "divide", 10, 2);
        System.out.println("divide(10, 2) = " + divideResult);

        Object moduloResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "modulo", 10, 5);
        System.out.println("modulo(10, 5) = " + moduloResult);

        Object addZeroResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "add", 0);
        System.out.println("add(0) = " + addZeroResult);

        Object addResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "add", 1, 2, 3, 4, 5);
        System.out.println("add(1, 2, 3, 4, 5) = " + addResult);

        Object multiplyMultipleResult = makeXmlRpcRequest(httpClient, server, port, userAgent, "multiply", 1, 2, 3, 4, 5);
        System.out.println("multiply(1, 2, 3, 4, 5) = " + multiplyMultipleResult);
    }

    private static Object makeXmlRpcRequest(HttpClient httpClient, String server, int port, String userAgent, String methodName, Object... params) throws Exception {
        String xmlRequest = buildXmlRpcRequest(methodName, params);
        // System.out.println("Request XML");
        // checkingRequest(xmlRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://" + server + ":" + port + "/RPC"))
                .header("Content-Type", "text/xml")
                .header("User-Agent", userAgent)
                .POST(HttpRequest.BodyPublishers.ofString(xmlRequest))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        String responseXml=response.body();
        System.out.println("Response XML");
        checkingRequest(responseXml);

        Object result = parseXmlRpcResponse(response.body());
        return result;
    }

    private static String buildXmlRpcRequest(String methodName, Object... params) {
        // Build the XML-RPC request payload
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\"?>");
        xmlBuilder.append("<methodCall>");
        xmlBuilder.append("<methodName>").append(methodName).append("</methodName>");
        if (params.length > 0) {
            xmlBuilder.append("<params>");
            for (Object param : params) {
                xmlBuilder.append("<param>");
                xmlBuilder.append("<value>").append(getXmlRpcValue(param)).append("</value>");
                xmlBuilder.append("</param>");
            }
            xmlBuilder.append("</params>");
        }
        xmlBuilder.append("</methodCall>");

        return xmlBuilder.toString();
    }

    private static String getXmlRpcValue(Object value) {
        // if (value instanceof Integer) {
            return "<i4>" + value + "</i4>";
        // } else {
            // return "<string>" + value + "</string>";
        // }
    }

    private static Object parseXmlRpcResponse(String xmlResponse) throws Exception {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(xmlResponse));
            String line;
            String faultCode="";

            while ((line = reader.readLine()) != null) {
                if (line.contains("<i4>")) {
                    int start = line.indexOf("<i4>") + 4;
                    int end = line.indexOf("</i4>");
                    String value = line.substring(start, end);
                    int intValue = Integer.parseInt(value);
                    return intValue;
                }
                if (line.contains("faultCode")) {
                    int start = line.indexOf("<int>") + 5;
                    int end = line.indexOf("</int>");
                    String value = line.substring(start, end);
                    faultCode= "Fault Code: " + Integer.parseInt(value);
                }
                if (line.contains("faultString")) {
                    int start = line.indexOf("<string>") + 8;
                    int end = line.indexOf("</string>");
                    String value = line.substring(start, end);
                    return faultCode + " FaultString: " + value;
                }
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Printing the XML so i can check format
    private static void checkingRequest(String xmlResponse) throws Exception {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(xmlResponse));
            String line;
            // Read each line of the XML request
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
