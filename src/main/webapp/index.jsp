<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JAX-WS Calculator Service</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            line-height: 1.6;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
        }
        h1 {
            color: #333366;
        }
        .info {
            background-color: #f0f0f0;
            padding: 15px;
            border-radius: 5px;
        }
        .code {
            background-color: #eee;
            padding: 10px;
            border-left: 3px solid #333366;
            font-family: monospace;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>JAX-WS Calculator Web Service</h1>
        
        <div class="info">
            <p>This is a sample JAX-WS web service implementation.</p>
            <p>The calculator service provides basic arithmetic operations:</p>
            <ul>
                <li>Addition</li>
                <li>Subtraction</li>
                <li>Multiplication</li>
                <li>Division</li>
            </ul>
        </div>
        
        <h2>Service Information</h2>
        <p>The WSDL for this service is available at:</p>
        <div class="code">
            <a href="services/CalculatorService?wsdl">services/CalculatorService?wsdl</a>
        </div>
        
        <h2>Testing the Service</h2>
        <p>You can use the client application provided in this project to test the service.</p>
        <p>See the README.md file for detailed instructions on building and running the client.</p>
        
        <h2>About JAX-WS</h2>
        <p>JAX-WS (Java API for XML Web Services) is the modern successor to JAX-RPC, providing a simpler 
           programming model, support for SOAP 1.2, and better integration with JAXB for XML data binding.</p>
    </div>
</body>
</html>
