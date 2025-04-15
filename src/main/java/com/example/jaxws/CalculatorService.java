package com.example.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * A simple calculator service interface for JAX-WS demonstration
 */
@WebService(name = "CalculatorService", targetNamespace = "http://jaxws.example.com")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public interface CalculatorService {
    
    /**
     * Adds two numbers together
     * 
     * @param a first number
     * @param b second number
     * @return sum of a and b
     */
    @WebMethod
    int add(@WebParam(name = "a") int a, @WebParam(name = "b") int b);
    
    /**
     * Subtracts second number from first number
     * 
     * @param a first number
     * @param b second number
     * @return a minus b
     */
    @WebMethod
    int subtract(@WebParam(name = "a") int a, @WebParam(name = "b") int b);
    
    /**
     * Multiplies two numbers
     * 
     * @param a first number
     * @param b second number
     * @return product of a and b
     */
    @WebMethod
    int multiply(@WebParam(name = "a") int a, @WebParam(name = "b") int b);
    
    /**
     * Divides first number by second number
     * 
     * @param a first number (dividend)
     * @param b second number (divisor)
     * @return a divided by b
     * @throws ArithmeticException if b is zero
     */
    @WebMethod
    int divide(@WebParam(name = "a") int a, @WebParam(name = "b") int b) throws ArithmeticException;
}
