package com.soulware.platform.docexcelparser;

import com.soulware.platform.docexcelparser.test.ExcelParserTester;
import jakarta.inject.Inject;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Servlet para testing del parser de Excel con base64 personalizado
 */
@WebServlet(name = "parserTestServlet", value = "/parser-test")
public class ParserTestServlet extends HttpServlet {
    
    @Inject
    private ExcelParserTester parserTester;

    public void init() {
        System.out.println("=== PARSER TEST SERVLET INICIALIZADO ===");
        System.out.println("Versión: 1.0 - Testing del Parser Excel");
        System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("=====================================");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>🧪 Parser Test - DocExcelParser</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; border-bottom: 2px solid #28a745; padding-bottom: 10px; }");
        out.println(".form-section { background-color: #e8f5e8; border: 1px solid #28a745; border-radius: 4px; padding: 20px; margin: 20px 0; }");
        out.println(".result-section { background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 4px; padding: 20px; margin: 20px 0; }");
        out.println("textarea { width: 100%; height: 200px; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-family: monospace; font-size: 12px; }");
        out.println("button { background-color: #28a745; color: white; border: none; padding: 12px 24px; border-radius: 4px; cursor: pointer; margin: 5px; font-size: 14px; }");
        out.println("button:hover { background-color: #218838; }");
        out.println(".quick-test-btn { background-color: #007bff; }");
        out.println(".quick-test-btn:hover { background-color: #0056b3; }");
        out.println(".result-box { background-color: white; border: 1px solid #ddd; border-radius: 4px; padding: 15px; margin: 10px 0; }");
        out.println(".success { border-left: 4px solid #28a745; }");
        out.println(".error { border-left: 4px solid #dc3545; }");
        out.println(".info { border-left: 4px solid #17a2b8; }");
        out.println("pre { background-color: #f8f9fa; padding: 10px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; }");
        out.println(".field-analysis { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0; }");
        out.println(".field-group { background-color: white; border: 1px solid #ddd; border-radius: 4px; padding: 15px; }");
        out.println(".field-item { margin: 5px 0; padding: 5px; border-radius: 3px; }");
        out.println(".completed { background-color: #d4edda; color: #155724; }");
        out.println(".incomplete { background-color: #f8d7da; color: #721c24; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>🧪 Parser Test - DocExcelParser</h1>");
        
        // Información de la aplicación
        out.println("<div class='info-box'>");
        out.println("<h3>📋 Información del Test</h3>");
        out.println("<p><strong>Versión:</strong> 1.0 - Testing del Parser Excel</p>");
        out.println("<p><strong>Última actualización:</strong> " + 
                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>");
        out.println("<p><strong>Funcionalidad:</strong> Permite testear el parser con cualquier base64 de Excel</p>");
        out.println("</div>");
        
        // Formulario para testing
        out.println("<div class='form-section'>");
        out.println("<h3>🔬 Testing del Parser</h3>");
        out.println("<form method='post' action='parser-test'>");
        out.println("<label for='base64Input'><strong>Base64 del archivo Excel:</strong></label><br/>");
        out.println("<textarea id='base64Input' name='base64Content' placeholder='Pega aquí el base64 de tu archivo Excel...'></textarea><br/>");
        out.println("<button type='submit' name='action' value='test'>🧪 Testear Parser</button>");
        out.println("<button type='submit' name='action' value='quickTest' class='quick-test-btn'>⚡ Test Rápido (Ejemplo)</button>");
        out.println("</form>");
        out.println("</div>");
        
        // Mostrar resultados si hay una acción POST
        String action = request.getParameter("action");
        if (action != null) {
            out.println("<div class='result-section'>");
            out.println("<h3>📊 Resultados del Test</h3>");
            
            try {
                Map<String, Object> testResult = null;
                
                if ("quickTest".equals(action)) {
                    out.println("<div class='info-box'>");
                    out.println("<p><strong>⚡ Ejecutando test rápido con datos de ejemplo...</strong></p>");
                    out.println("</div>");
                    testResult = parserTester.quickTest();
                } else {
                    String base64Content = request.getParameter("base64Content");
                    if (base64Content == null || base64Content.trim().isEmpty()) {
                        out.println("<div class='result-box error'>");
                        out.println("<h4>❌ Error</h4>");
                        out.println("<p>No se proporcionó contenido base64 para testear.</p>");
                        out.println("</div>");
                    } else {
                        out.println("<div class='info-box'>");
                        out.println("<p><strong>🔬 Ejecutando test con base64 personalizado...</strong></p>");
                        out.println("<p><strong>Longitud del base64:</strong> " + base64Content.length() + " caracteres</p>");
                        out.println("</div>");
                        testResult = parserTester.testParserWithBase64(base64Content);
                    }
                }
                
                if (testResult != null) {
                    displayTestResults(out, testResult);
                }
                
            } catch (Exception e) {
                out.println("<div class='result-box error'>");
                out.println("<h4>❌ Error durante el test</h4>");
                out.println("<p>" + e.getMessage() + "</p>");
                out.println("<pre>" + getStackTrace(e) + "</pre>");
                out.println("</div>");
            }
            
            out.println("</div>");
        }
        
        // Instrucciones de uso
        out.println("<div class='form-section'>");
        out.println("<h3>📖 Instrucciones de Uso</h3>");
        out.println("<ol>");
        out.println("<li><strong>Test Rápido:</strong> Usa el botón 'Test Rápido' para probar con datos de ejemplo</li>");
        out.println("<li><strong>Test Personalizado:</strong> Pega el base64 de tu archivo Excel en el área de texto y haz clic en 'Testear Parser'</li>");
        out.println("<li><strong>Interpretación de Resultados:</strong> Revisa la validación, análisis de datos y recomendaciones</li>");
        out.println("<li><strong>Campos Analizados:</strong> El sistema analiza campos obligatorios y opcionales del formulario médico</li>");
        out.println("</ol>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Redirigir a GET para mostrar resultados
        doGet(request, response);
    }
    
    /**
     * Muestra los resultados del test de forma organizada
     */
    private void displayTestResults(PrintWriter out, Map<String, Object> testResult) {
        boolean success = (Boolean) testResult.get("success");
        
        if (success) {
            out.println("<div class='result-box success'>");
            out.println("<h4>✅ Test Completado Exitosamente</h4>");
            out.println("<p><strong>Timestamp:</strong> " + testResult.get("timestamp") + "</p>");
            out.println("</div>");
            
            // Mostrar resumen
            String summary = (String) testResult.get("summary");
            if (summary != null) {
                out.println("<div class='result-box info'>");
                out.println("<h4>📋 Resumen del Test</h4>");
                out.println("<pre>" + summary + "</pre>");
                out.println("</div>");
            }
            
            // Mostrar análisis detallado
            @SuppressWarnings("unchecked")
            Map<String, Object> analysis = (Map<String, Object>) testResult.get("analysis");
            if (analysis != null) {
                out.println("<div class='result-box info'>");
                out.println("<h4>📊 Análisis Detallado de Campos</h4>");
                
                out.println("<div class='field-analysis'>");
                
                // Campos obligatorios
                @SuppressWarnings("unchecked")
                Map<String, Object> requiredFields = (Map<String, Object>) analysis.get("requiredFields");
                if (requiredFields != null) {
                    out.println("<div class='field-group'>");
                    out.println("<h5>🔴 Campos Obligatorios</h5>");
                    for (Map.Entry<String, Object> entry : requiredFields.entrySet()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fieldData = (Map<String, Object>) entry.getValue();
                        boolean completed = (Boolean) fieldData.get("completed");
                        String value = (String) fieldData.get("value");
                        
                        out.println("<div class='field-item " + (completed ? "completed" : "incomplete") + "'>");
                        out.println("<strong>" + entry.getKey() + ":</strong> " + value);
                        out.println("</div>");
                    }
                    out.println("</div>");
                }
                
                // Campos opcionales
                @SuppressWarnings("unchecked")
                Map<String, Object> optionalFields = (Map<String, Object>) analysis.get("optionalFields");
                if (optionalFields != null) {
                    out.println("<div class='field-group'>");
                    out.println("<h5>🟡 Campos Opcionales</h5>");
                    for (Map.Entry<String, Object> entry : optionalFields.entrySet()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fieldData = (Map<String, Object>) entry.getValue();
                        boolean completed = (Boolean) fieldData.get("completed");
                        String value = (String) fieldData.get("value");
                        
                        out.println("<div class='field-item " + (completed ? "completed" : "incomplete") + "'>");
                        out.println("<strong>" + entry.getKey() + ":</strong> " + value);
                        out.println("</div>");
                    }
                    out.println("</div>");
                }
                
                out.println("</div>");
                out.println("</div>");
            }
            
            // Mostrar datos del paciente
            Object parserResult = testResult.get("parserResult");
            if (parserResult != null) {
                out.println("<div class='result-box info'>");
                out.println("<h4>👤 Datos del Paciente Extraídos</h4>");
                out.println("<pre>" + parserResult.toString() + "</pre>");
                out.println("</div>");
            }
            
        } else {
            out.println("<div class='result-box error'>");
            out.println("<h4>❌ Test Falló</h4>");
            out.println("<p><strong>Error:</strong> " + testResult.get("error") + "</p>");
            out.println("<p><strong>Timestamp:</strong> " + testResult.get("timestamp") + "</p>");
            out.println("</div>");
        }
    }
    
    /**
     * Obtiene el stack trace de una excepción
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public void destroy() {
    }
}
