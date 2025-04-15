package com.example.jaxws.client;

import com.example.jaxws.CalculatorService;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * Client application demonstrating JAX-WS service consumption
 */
public class CalculatorClient {

    private static final String ENDPOINT_URL = "http://localhost:8080/jaxws-sample/services/CalculatorService?wsdl";
    private static final String NAMESPACE = "http://jaxws.example.com";
    private static final String SERVICE_NAME = "CalculatorService";

    public static void main(String[] args) {
        try {
            URL url = new URL(ENDPOINT_URL);
            QName qname = new QName(NAMESPACE, SERVICE_NAME);
            
            // Create a service and get a port
            Service service = Service.create(url, qname);
            CalculatorService calculator = service.getPort(CalculatorService.class);
            
            // Test addition
            int addResult = calculator.add(10, 20);
            System.out.println("Addition result: 10 + 20 = " + addResult);
            
            // Test subtraction
            int subtractResult = calculator.subtract(30, 15);
            System.out.println("Subtraction result: 30 - 15 = " + subtractResult);
            
            // Test multiplication
            int multiplyResult = calculator.multiply(8, 5);
            System.out.println("Multiplication result: 8 * 5 = " + multiplyResult);
            
            // Test division
            int divideResult = calculator.divide(100, 20);
            System.out.println("Division result: 100 / 20 = " + divideResult);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
