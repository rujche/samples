package com.example.jaxrpc;

/**
 * Implementation of the CalculatorService interface for JAX-RPC demonstration
 */
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
