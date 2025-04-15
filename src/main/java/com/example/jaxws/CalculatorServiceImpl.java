package com.example.jaxws;

import javax.jws.WebService;

/**
 * Implementation of the CalculatorService interface for JAX-WS demonstration
 */
@WebService(
    serviceName = "CalculatorService", 
    portName = "CalculatorPort",
    endpointInterface = "com.example.jaxws.CalculatorService", 
    targetNamespace = "http://jaxws.example.com"
)
public class CalculatorServiceImpl implements CalculatorService {
    
    @Override
    public int add(int a, int b) {
        return a + b;
    }
    
    @Override
    public int subtract(int a, int b) {
        return a - b;
    }
    
    @Override
    public int multiply(int a, int b) {
        return a * b;
    }
    
    @Override
    public int divide(int a, int b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }
}
