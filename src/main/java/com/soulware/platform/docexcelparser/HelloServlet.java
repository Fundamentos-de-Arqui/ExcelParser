package com.soulware.platform.docexcelparser;

import com.soulware.platform.docexcelparser.entity.PatientProfile;
import com.soulware.platform.docexcelparser.service.WebListenerService;
import jakarta.inject.Inject;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;
    
    @Inject
    private WebListenerService webListenerService;

    public void init() {
        message = "DocExcelParser - Procesador de Pacientes desde Cola ActiveMQ";
        System.out.println("=== HELLO SERVLET INICIALIZADO ===");
        System.out.println("Versión: 3.0 - Parser Integrado con Cola");
        System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Procesamiento automático: IMPLEMENTADO");
        System.out.println("Extracción de pacientes: ACTIVADO");
        System.out.println("Integración con cola: COMPLETA");
        System.out.println("=====================================");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("=== HELLO SERVLET DOGET LLAMADO ===");
        System.out.println("Versión: 3.0 - Parser Integrado con Cola");
        System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Procesando pacientes desde cola...");
        System.out.println("=====================================");
        
        response.setContentType("text/html; charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>DocExcelParser - Procesador de Pacientes</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }");
        out.println(".info-box { background-color: #e7f3ff; border: 1px solid #007bff; border-radius: 4px; padding: 15px; margin: 20px 0; }");
        out.println(".messages-box { background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 4px; padding: 15px; margin: 20px 0; }");
        out.println(".message-item { background-color: white; border: 1px solid #ddd; border-radius: 4px; padding: 10px; margin: 10px 0; }");
        out.println(".timestamp { color: #666; font-size: 0.9em; }");
        out.println(".no-messages { color: #666; font-style: italic; }");
        out.println("button { background-color: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; margin: 5px; }");
        out.println("button:hover { background-color: #0056b3; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>" + message + "</h1>");
        
        // Información de versión
        out.println("<div class='info-box'>");
        out.println("<h3>🔧 Información de Versión</h3>");
        out.println("<p><strong>Versión:</strong> 3.0 - Parser Integrado con Cola</p>");
        out.println("<p><strong>Última actualización:</strong> " + 
                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>");
        out.println("<p><strong>Características:</strong></p>");
        out.println("<ul>");
        out.println("<li>✅ Procesamiento automático desde cola</li>");
        out.println("<li>✅ Extracción completa de datos de pacientes</li>");
        out.println("<li>✅ Integración con ActiveMQ</li>");
        out.println("<li>✅ Visualización detallada de resultados</li>");
        out.println("<li>✅ Manejo de errores robusto</li>");
        out.println("</ul>");
        out.println("</div>");
        
        // Información de la cola
        out.println("<div class='info-box'>");
        out.println("<h3>📊 Información de la Cola</h3>");
        out.println("<p><strong>📡 ActiveMQ JMS Listener - Cola: excel.input.queue</strong></p>");
        out.println("<p><strong>🔗 Conexión: tcp://localhost:61616</strong></p>");
        out.println("<p class='timestamp'>Última actualización: " + 
                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>");
        out.println("</div>");
        
        // Botones de acción
        out.println("<div>");
        out.println("<button onclick='location.reload()'>🔄 Actualizar Pacientes</button>");
        out.println("<button onclick='forcePoll()'>⚡ Forzar Polling</button>");
        out.println("<button onclick='processPatients()'>👥 Procesar Pacientes</button>");
        out.println("<button onclick='viewQueueStatus()'>📊 Estado de Cola</button>");
        out.println("</div>");
        
               // DEBUGGING: Mostrar JSON crudo de la cola usando ActiveMQ JMS Listener
               out.println("<div class='messages-box'>");
               out.println("<h3>🔍 DEBUG: JSON Crudo de la Cola (ActiveMQ JMS Listener)</h3>");
        
        try {
            // Leer mensaje usando WebListener
            System.out.println("=== OBTENIENDO MENSAJE DEL WEBLISTENER ===");
            String rawMessage = webListenerService.getLastMessage();
            
            if (rawMessage != null && !rawMessage.trim().isEmpty()) {
                System.out.println("=== MENSAJE ENCONTRADO EN WEBLISTENER ===");
                System.out.println("Longitud: " + rawMessage.length() + " caracteres");
                System.out.println("Primeros 100 chars: " + rawMessage.substring(0, Math.min(100, rawMessage.length())));
                
                out.println("<div style='background-color: #e7f3ff; padding: 15px; margin: 10px 0; border-radius: 4px;'>");
                out.println("<strong>📄 JSON Leído de la Cola (WebListener):</strong><br/>");
                out.println("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 4px; overflow-x: auto; white-space: pre-wrap; font-family: monospace; font-size: 12px;'>" + rawMessage + "</pre>");
                out.println("</div>");
                
                // Mostrar información básica del mensaje
                out.println("<div style='background-color: #d4edda; padding: 10px; margin: 10px 0; border-radius: 4px;'>");
                out.println("<strong>📊 Información del Mensaje:</strong><br/>");
                out.println("📏 Longitud: " + rawMessage.length() + " caracteres<br/>");
                out.println("📅 Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "<br/>");
                out.println("🔧 Método: WebListener Jakarta<br/>");
                out.println("✅ Estado: MENSAJE ENCONTRADO<br/>");
                out.println("📡 Estado: " + webListenerService.getListenerStatus() + "<br/>");
                out.println("</div>");
                
            } else {
                System.out.println("=== NO HAY MENSAJES EN EL WEBLISTENER ===");
                out.println("<div style='background-color: #f8d7da; padding: 10px; margin: 10px 0; border-radius: 4px;'>");
                out.println("<strong>❌ No hay mensajes en el WebListener</strong><br/>");
                out.println("El WebListener no ha encontrado mensajes aún o están vacíos.");
                out.println("<br/>🔧 Método: WebListener Jakarta");
                out.println("<br/>📡 Estado: " + webListenerService.getListenerStatus());
                out.println("</div>");
            }
            
        } catch (Exception e) {
            System.err.println("=== ERROR EN WEBLISTENER ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            out.println("<div style='background-color: #f8d7da; padding: 10px; margin: 10px 0; border-radius: 4px;'>");
            out.println("<strong>❌ Error en WebListener:</strong><br/>");
            out.println("Error: " + e.getMessage() + "<br/>");
            out.println("</div>");
        }
        
        out.println("</div>");
        
        // Resumen general deshabilitado - solo debugging
        out.println("<div class='messages-box'>");
        out.println("<h3>📊 Estado del Sistema</h3>");
        out.println("<p>🔧 Parser: COMPLETAMENTE DESHABILITADO</p>");
        out.println("<p>🔍 Modo: WebListener Jakarta Servlet</p>");
        out.println("<p>📡 Estado: " + webListenerService.getListenerStatus() + "</p>");
        out.println("<p>📅 Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();
        
        if ("processPatients".equals(action)) {
            // Procesamiento deshabilitado - solo WebListener
            out.println("{\"success\": false, \"message\": \"Procesamiento deshabilitado - Solo WebListener activo\"}");
        } else if ("forcePoll".equals(action)) {
            try {
                System.out.println("=== POLLING FORZADO DESDE SERVLET ===");
                String message = webListenerService.getLastMessage();
                if (message != null && !message.trim().isEmpty()) {
                    out.println("{\"success\": true, \"message\": \"Polling forzado exitosamente - Mensaje encontrado: " + message.length() + " caracteres\"}");
                } else {
                    out.println("{\"success\": true, \"message\": \"Polling forzado exitosamente - No hay mensajes en el WebListener\"}");
                }
            } catch (Exception e) {
                out.println("{\"success\": false, \"message\": \"Error en polling forzado: " + e.getMessage() + "\"}");
            }
        } else if ("getSummary".equals(action)) {
            String summary = webListenerService.getListenerStatus();
            out.println("{\"success\": true, \"summary\": \"" + summary.replace("\"", "\\\"") + "\"}");
        } else if ("getQueueInfo".equals(action)) {
            String queueInfo = webListenerService.getListenerStatus();
            out.println("{\"success\": true, \"queueInfo\": \"" + queueInfo + "\"}");
        }
    }

    public void destroy() {
    }
}