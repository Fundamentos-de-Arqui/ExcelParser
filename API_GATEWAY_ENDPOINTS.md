# 🚀 API Gateway - Endpoints y Configuración

## 📡 **Endpoint Principal**

### **POST /api/excel/process**
**URL:** `http://localhost:3000/api/excel/process`

**Descripción:** Procesa archivos Excel codificados en base64 y los envía a la cola de procesamiento.

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "excelBase64": "tu_archivo_excel_en_base64_aqui"
}
```

**Respuesta Exitosa (202 Accepted):**
```json
{
    "status": "accepted",
    "message": "Excel file successfully sent to processing queue",
    "destination": "/queue/excel-input-queue",
    "processingId": "excel-1761116510288",
    "timestamp": "2025-10-22T07:01:50.287Z",
    "brokerStatus": "connected"
}
```

**Respuesta de Error (400 Bad Request):**
```json
{
    "status": "error",
    "message": "Missing required field: excelBase64",
    "details": "The request body must contain an excelBase64 field with the Excel file encoded in base64"
}
```

---

## 🔧 **Configuración Técnica**

### **API Gateway**
- **Puerto:** 3000
- **Límite JSON:** 50MB
- **STOMP Configuración:**
  - `splitLargeFrames: true`
  - `maxWebSocketChunkSize: 64 * 1024` (64KB chunks)
  - `maxWebSocketFrameSize: 50MB`
  - `maxWebSocketMessageSize: 50MB`

### **ActiveMQ**
- **Puerto STOMP:** 61613
- **Puerto WebSocket:** 61614
- **Puerto Consola Web:** 8161
- **Cola de Destino:** `excel-input-queue`
- **Límites Configurados:**
  - `maxFrameSize: 500MB`
  - `maxMessageSize: 500MB`
  - `maxCommandLength: 500MB`

---

## 🧪 **Ejemplos de Uso**

### **Con cURL:**
```bash
curl -X POST http://localhost:3000/api/excel/process \
  -H "Content-Type: application/json" \
  -d '{"excelBase64": "UEsDBBQABgAIAAAAIQBH+AyKrgEAAD4IAAATAAgCW0NvbnRlbn..."}'
```

### **Con PowerShell:**
```powershell
$body = @{excelBase64="tu_base64_aqui"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:3000/api/excel/process" -Method POST -ContentType "application/json" -Body $body
```

### **Con Postman:**
1. **Method:** POST
2. **URL:** `http://localhost:3000/api/excel/process`
3. **Headers:** `Content-Type: application/json`
4. **Body (raw JSON):**
   ```json
   {
       "excelBase64": "tu_archivo_excel_en_base64"
   }
   ```

---

## 🔍 **Monitoreo y Debugging**

### **Consola Web de ActiveMQ:**
- **URL:** `http://localhost:8161/admin`
- **Usuario:** admin
- **Contraseña:** admin
- **Verificar cola:** `excel-input-queue`

### **Logs del API Gateway:**
Los logs muestran:
- ✅ Validación de entrada
- ✅ Tamaño del mensaje
- ✅ Estado de conexión al broker
- ✅ Confirmación de envío

### **Logs de ActiveMQ:**
Buscar errores como:
- ❌ `The maximum command length was exceeded`
- ❌ `Transport Connection failed`

---

## 🚨 **Solución de Problemas**

### **Problema: "Maximum command length exceeded"**
**Solución:** Configurar `maxCommandLength=524288000` en ActiveMQ

### **Problema: Mensajes grandes no llegan**
**Solución:** Usar `splitLargeFrames: true` en el cliente STOMP

### **Problema: "Missing excelBase64 field"**
**Solución:** Asegurar que el campo se llame exactamente `excelBase64`

---

## 📋 **Estado Actual**

✅ **API Gateway:** Funcionando en puerto 3000
✅ **ActiveMQ:** Configurado con límites de 500MB
✅ **STOMP:** Configurado con división de frames
✅ **Endpoint:** `/api/excel/process` operativo
✅ **Campo:** `excelBase64` implementado
✅ **Mensajes grandes:** Soportados hasta 500MB

---

## 🔗 **Enlaces Útiles**

- **API Gateway:** `http://localhost:3000/api/excel/process`
- **Consola ActiveMQ:** `http://localhost:8161/admin`
- **Health Check:** `http://localhost:3000/api/health`
