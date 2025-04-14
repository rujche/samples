package com.example.jaxrpc;

/**
 * A simple calculator service interface for JAX-RPC demonstration
 */
public interface CalculatorService {
    
    /**
     * Adds two numbers together
     * 
     * @param a first number
     * @param b second number
     * @return sum of a and b
     */
    int add(int a, int b);
    
    /**
     * Subtracts second number from first number
     * 
     * @param a first number
     * @param b second number
     * @return a minus b
     */
    int subtract(int a, int b);
    
    /**
     * Multiplies two numbers
     * 
     * @param a first number
     * @param b second number
     * @return product of a and b
     */
    int multiply(int a, int b);
    
    /**
     * Divides first number by second number
     * 
     * @param a first number (dividend)
     * @param b second number (divisor)
     * @return a divided by b
     * @throws ArithmeticException if b is zero
     */
    int divide(int a, int b) throws ArithmeticException;
}
