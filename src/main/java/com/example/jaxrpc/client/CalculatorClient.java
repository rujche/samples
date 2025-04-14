package com.example.jaxrpc.client;

import com.example.jaxrpc.CalculatorService;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * Client application demonstrating JAX-RPC service consumption
 */
public class CalculatorClient {

    private static final String ENDPOINT_URL = "http://localhost:8080/jaxrpc-sample/services/CalculatorService";
    private static final String NAMESPACE = "http://jaxrpc.example.com";

    public static void main(String[] args) {
        try {
            // Create a service and call objects
            Service service = new Service();
            Call call = (Call) service.createCall();
            
            // Set the endpoint and other properties
            call.setTargetEndpointAddress(ENDPOINT_URL);
            
            // Test addition
            testAddition(call);
            
            // Test subtraction
            testSubtraction(call);
            
            // Test multiplication
            testMultiplication(call);
            
            // Test division
            testDivision(call);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
      private static void testAddition(Call call) throws Exception {
        call.removeAllParameters(); // Clear any previous parameters
        call.setOperationName(new QName(NAMESPACE, "add"));
        call.addParameter("a", XMLType.XSD_INT, ParameterMode.IN);
        call.addParameter("b", XMLType.XSD_INT, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_INT);
        
        Integer result = (Integer) call.invoke(new Object[]{10, 20});
        System.out.println("Addition result: 10 + 20 = " + result);
    }
      private static void testSubtraction(Call call) throws Exception {
        call.removeAllParameters(); // Clear any previous parameters
        call.setOperationName(new QName(NAMESPACE, "subtract"));
        call.addParameter("a", XMLType.XSD_INT, ParameterMode.IN);
        call.addParameter("b", XMLType.XSD_INT, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_INT);
        
        Integer result = (Integer) call.invoke(new Object[]{30, 15});
        System.out.println("Subtraction result: 30 - 15 = " + result);
    }
      private static void testMultiplication(Call call) throws Exception {
        call.removeAllParameters(); // Clear any previous parameters
        call.setOperationName(new QName(NAMESPACE, "multiply"));
        call.addParameter("a", XMLType.XSD_INT, ParameterMode.IN);
        call.addParameter("b", XMLType.XSD_INT, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_INT);
        
        Integer result = (Integer) call.invoke(new Object[]{8, 5});
        System.out.println("Multiplication result: 8 * 5 = " + result);
    }
      private static void testDivision(Call call) throws Exception {
        call.removeAllParameters(); // Clear any previous parameters
        call.setOperationName(new QName(NAMESPACE, "divide"));
        call.addParameter("a", XMLType.XSD_INT, ParameterMode.IN);
        call.addParameter("b", XMLType.XSD_INT, ParameterMode.IN);
        call.setReturnType(XMLType.XSD_INT);
        
        Integer result = (Integer) call.invoke(new Object[]{100, 20});
        System.out.println("Division result: 100 / 20 = " + result);
    }
}
