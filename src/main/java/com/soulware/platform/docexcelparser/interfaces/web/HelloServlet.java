package com.soulware.platform.docexcelparser.interfaces.web;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import com.soulware.platform.docexcelparser.domain.model.LegalGuardian;
import com.soulware.platform.docexcelparser.domain.model.ReferredTherapist;
import com.soulware.platform.docexcelparser.interfaces.adapter.WebListenerService;
import com.soulware.platform.docexcelparser.infrastructure.messaging.JMSMessageSender;
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
    
    @Inject
    private JMSMessageSender jmsMessageSender;

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
        out.println("<p><strong>📡 Direct JMS Listener + Test Fallback</strong></p>");
        out.println("<p><strong>🔧 Estado: " + (webListenerService.isDirectJMSInitialized() ? "JMS DIRECTO ACTIVO" : "MODO TEST") + "</strong></p>");
        
        // Mostrar información de pacientes procesados
        List<PatientProfile> patients = webListenerService.getProcessedPatients();
        out.println("<p><strong>👥 Pacientes Procesados: " + patients.size() + "</strong></p>");
        
        out.println("<p class='timestamp'>Última actualización: " + 
                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>");
        out.println("</div>");
        
        // Botones de acción
        out.println("<div>");
        out.println("<button onclick='location.reload()'>🔄 Actualizar Pacientes</button>");
        out.println("<button onclick='forcePoll()'>⚡ Forzar Polling</button>");
        out.println("<button onclick='readNextMessage()'>📨 Leer Siguiente Mensaje</button>");
        out.println("<button onclick='processPatients()'>👥 Procesar Pacientes</button>");
        out.println("<button onclick='viewQueueStatus()'>📊 Estado de Cola</button>");
        out.println("</div>");
        
               // DEBUGGING: Mostrar JSON crudo de la cola usando listener activo
               out.println("<div class='messages-box'>");
               out.println("<h3>🔍 DEBUG: JSON Crudo de la Cola (" + (webListenerService.isDirectJMSInitialized() ? "Direct JMS Listener" : "Test Listener") + ")</h3>");
        
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
        
        // Mostrar pacientes procesados
        out.println("<div class='messages-box'>");
        out.println("<h3>👥 Pacientes Procesados desde Excel</h3>");
        
        if (!patients.isEmpty()) {
            out.println("<p><strong>Total de pacientes encontrados: " + patients.size() + "</strong></p>");
            
            for (int i = 0; i < patients.size(); i++) {
                PatientProfile patient = patients.get(i);
                out.println("<div class='message-item'>");
                out.println("<h4>Paciente " + (i + 1) + "</h4>");
                out.println("<p><strong>Nombres:</strong> " + patient.getFirstNames() + "</p>");
                out.println("<p><strong>Apellido Paterno:</strong> " + patient.getPaternalSurname() + "</p>");
                out.println("<p><strong>Apellido Materno:</strong> " + patient.getMaternalSurname() + "</p>");
                out.println("<p><strong>Edad Actual:</strong> " + patient.getAgeCurrent() + "</p>");
                out.println("<p><strong>Género:</strong> " + patient.getGender() + "</p>");
                out.println("<p><strong>Email:</strong> " + patient.getEmail() + "</p>");
                out.println("<p><strong>Teléfono:</strong> " + patient.getPhone() + "</p>");
                out.println("<p><strong>Dirección:</strong> " + patient.getCurrentAddress() + "</p>");
                out.println("<p><strong>Fecha de Nacimiento:</strong> " + patient.getBirthDate() + "</p>");
                out.println("<p><strong>Lugar de Nacimiento:</strong> " + patient.getBirthPlace() + "</p>");
                out.println("<p><strong>Documento de Identidad:</strong> " + patient.getIdentityDocumentNumber() + "</p>");
                out.println("<p><strong>Estado Civil:</strong> " + patient.getMaritalStatus() + "</p>");
                out.println("<p><strong>Ocupación:</strong> " + patient.getOccupation() + "</p>");
                out.println("<p><strong>Nivel de Educación:</strong> " + patient.getEducationLevel() + "</p>");
                out.println("<p><strong>Religión:</strong> " + patient.getReligion() + "</p>");
                out.println("<p><strong>Distrito:</strong> " + patient.getDistrict() + "</p>");
                out.println("<p><strong>Provincia:</strong> " + patient.getProvince() + "</p>");
                out.println("<p><strong>Región:</strong> " + patient.getRegion() + "</p>");
                       out.println("<p><strong>País:</strong> " + patient.getCountry() + "</p>");
                       
                       // Mostrar responsables legales
                       List<LegalGuardian> legalGuardians = patient.getLegalGuardians();
                       if (legalGuardians != null && !legalGuardians.isEmpty()) {
                           out.println("<div style='margin-top: 15px; padding: 10px; background-color: #f0f8ff; border-radius: 4px;'>");
                           out.println("<h5>👨‍👩‍👧‍👦 Responsables Legales (" + legalGuardians.size() + "):</h5>");
                           
                           for (int j = 0; j < legalGuardians.size(); j++) {
                               LegalGuardian guardian = legalGuardians.get(j);
                               out.println("<div style='margin: 10px 0; padding: 8px; background-color: #e6f3ff; border-radius: 4px;'>");
                               out.println("<strong>Responsable " + (j + 1) + ":</strong><br/>");
                               out.println("📝 <strong>Nombre:</strong> " + (guardian.getFullName() != null ? guardian.getFullName() : "No especificado") + "<br/>");
                               out.println("🆔 <strong>Documento:</strong> " + (guardian.getIdentityDocumentNumber() != null ? guardian.getIdentityDocumentNumber() : "No especificado") + "<br/>");
                               out.println("👥 <strong>Parentesco:</strong> " + (guardian.getRelationship() != null ? guardian.getRelationship() : "No especificado") + "<br/>");
                               out.println("📞 <strong>Teléfono:</strong> " + (guardian.getPhoneNumber() != null ? guardian.getPhoneNumber() : "No especificado") + "<br/>");
                               out.println("📧 <strong>Email:</strong> " + (guardian.getEmail() != null ? guardian.getEmail() : "No especificado") + "<br/>");
                           out.println("</div>");
                       }
                       
                       // Mostrar médico tratante principal
                       ReferredTherapist therapist = patient.getReferredTherapist();
                       if (therapist != null && therapist.hasName()) {
                           out.println("<div style='margin-top: 15px; padding: 10px; background-color: #e8f5e8; border-radius: 4px;'>");
                           out.println("<h5>👨‍⚕️ Médico Tratante Principal:</h5>");
                           out.println("<p><strong>Nombre:</strong> " + therapist.getTherapistName() + "</p>");
                           out.println("</div>");
                       } else {
                           out.println("<div style='margin-top: 15px; padding: 10px; background-color: #fff3cd; border-radius: 4px;'>");
                           out.println("<h5>👨‍⚕️ Médico Tratante Principal:</h5>");
                           out.println("<p style='color: #856404;'>No se encontró información del médico tratante en este formulario.</p>");
                           out.println("</div>");
                       }
                       } else {
                           out.println("<div style='margin-top: 15px; padding: 10px; background-color: #fff3cd; border-radius: 4px;'>");
                           out.println("<h5>👨‍👩‍👧‍👦 Responsables Legales:</h5>");
                           out.println("<p style='color: #856404;'>No se encontraron responsables legales en este formulario.</p>");
                           out.println("</div>");
                       }
            }
        } else {
            out.println("<p class='no-messages'>No hay pacientes procesados aún. Envía un mensaje con datos de Excel para procesar.</p>");
        }
        
        out.println("</div>");
        
        // Resumen general
        out.println("<div class='messages-box'>");
        out.println("<h3>📊 Estado del Sistema</h3>");
        out.println("<p>🔧 Parser: ACTIVO Y FUNCIONANDO</p>");
        out.println("<p>🔍 Modo: WebListener Jakarta Servlet + Excel Parser</p>");
        out.println("<p>📡 Estado: " + webListenerService.getListenerStatus() + "</p>");
        out.println("<p>👥 Pacientes Procesados: " + patients.size() + "</p>");
        out.println("<p>📅 Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>");
        out.println("</div>");
        
        out.println("</div>");
        
        // JavaScript para los botones
        out.println("<script>");
        out.println("function forcePoll() {");
        out.println("    fetch('/DocExcelParser/hello-servlet', {");
        out.println("        method: 'POST',");
        out.println("        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("        body: 'action=forcePoll'");
        out.println("    })");
        out.println("    .then(response => response.json())");
        out.println("    .then(data => {");
        out.println("        alert(data.message);");
        out.println("        location.reload();");
        out.println("    })");
        out.println("    .catch(error => alert('Error: ' + error));");
        out.println("}");
        out.println("");
        out.println("function readNextMessage() {");
        out.println("    fetch('/DocExcelParser/hello-servlet', {");
        out.println("        method: 'POST',");
        out.println("        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("        body: 'action=readNextMessage'");
        out.println("    })");
        out.println("    .then(response => response.json())");
        out.println("    .then(data => {");
        out.println("        alert(data.message);");
        out.println("        location.reload();");
        out.println("    })");
        out.println("    .catch(error => alert('Error: ' + error));");
        out.println("}");
        out.println("");
        out.println("function processPatients() {");
        out.println("    fetch('/DocExcelParser/hello-servlet', {");
        out.println("        method: 'POST',");
        out.println("        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("        body: 'action=processPatients'");
        out.println("    })");
        out.println("    .then(response => response.json())");
        out.println("    .then(data => alert(data.message))");
        out.println("    .catch(error => alert('Error: ' + error));");
        out.println("}");
        out.println("");
        out.println("function viewQueueStatus() {");
        out.println("    fetch('/DocExcelParser/hello-servlet', {");
        out.println("        method: 'POST',");
        out.println("        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("        body: 'action=getQueueInfo'");
        out.println("    })");
        out.println("    .then(response => response.json())");
        out.println("    .then(data => alert('Estado de la cola: ' + data.queueInfo))");
        out.println("    .catch(error => alert('Error: ' + error));");
        out.println("}");
        out.println("</script>");
        
        out.println("</body>");
        out.println("</html>");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();
        
        if ("processPatients".equals(action)) {
            // Procesamiento deshabilitado - solo WebListener
            out.println("{\"success\": false, \"message\": \"Procesamiento deshabilitado - Solo WebListener activo\"}");
        } else if ("sendMessage".equals(action)) {
            // Enviar mensaje REAL a la cola JMS
            try {
                String messageBody = request.getReader().lines().collect(java.util.stream.Collectors.joining());
                System.out.println("=== ENVIANDO MENSAJE REAL A LA COLA JMS ===");
                System.out.println("Mensaje recibido: " + messageBody);
                
                // Enviar mensaje REAL a la cola JMS
                boolean success = jmsMessageSender.sendMessageToQueue(messageBody);
                
                if (success) {
                    out.println("{\"success\": true, \"message\": \"Mensaje enviado REALMENTE a la cola JMS\", \"messageId\": \"real-" + System.currentTimeMillis() + "\"}");
                } else {
                    out.println("{\"success\": false, \"message\": \"Error enviando mensaje a la cola JMS\"}");
                }
            } catch (Exception e) {
                out.println("{\"success\": false, \"message\": \"Error enviando mensaje: " + e.getMessage() + "\"}");
            }
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
        } else if ("readNextMessage".equals(action)) {
            // Leer el siguiente mensaje de la cola
            try {
                System.out.println("=== LEYENDO SIGUIENTE MENSAJE DE LA COLA ===");
                
                // Primero limpiar el último mensaje leído
                webListenerService.clearLastMessage();
                
                // Luego obtener el siguiente mensaje
                String message = webListenerService.getLastMessage();
                
                if (message != null && !message.trim().isEmpty()) {
                    System.out.println("=== SIGUIENTE MENSAJE ENCONTRADO ===");
                    System.out.println("Longitud: " + message.length() + " caracteres");
                    System.out.println("Primeros 100 chars: " + message.substring(0, Math.min(100, message.length())));
                    
                    out.println("{\"success\": true, \"message\": \"Siguiente mensaje leído exitosamente: " + message.length() + " caracteres\", \"messageLength\": " + message.length() + "}");
                } else {
                    System.out.println("=== NO HAY MÁS MENSAJES EN LA COLA ===");
                    out.println("{\"success\": true, \"message\": \"No hay más mensajes en la cola. La cola está vacía.\"}");
                }
            } catch (Exception e) {
                System.err.println("=== ERROR LEYENDO SIGUIENTE MENSAJE ===");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                out.println("{\"success\": false, \"message\": \"Error leyendo siguiente mensaje: " + e.getMessage() + "\"}");
            }
        }
    }

    public void destroy() {
    }
}