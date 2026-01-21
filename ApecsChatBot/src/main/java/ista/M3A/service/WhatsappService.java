package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsappService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.api.token}")
    private String token;

    @Value("${whatsapp.phone.id}")
    private String phoneId;

    private final RestTemplate restTemplate = new RestTemplate();

    // ESTE ES EL MÉTODO QUE RECIBE EL WEBHOOK
    public void procesarMensaje(String from, String msgBody) {
        System.out.println("📨 MENSAJE RECIBIDO DE: " + from);
        System.out.println("💬 TEXTO: " + msgBody);
        
        // FORZAMOS LA RESPUESTA SIEMPRE (Para probar conexión)
        enviarMensajePrueba(from);
    }

    private void enviarMensajePrueba(String numeroDestino) {
        String url = apiUrl + phoneId + "/messages";

        // 1. CONSTRUCCIÓN DEL JSON IGUALITO A POSTMAN
        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", numeroDestino);
        payload.put("type", "text");
        
        Map<String, String> textObj = new HashMap<>();
        textObj.put("body", "🤖 ¡Hola! Soy Java. Si lees esto, ¡FUNCIONÓ!");
        payload.put("text", textObj);

        // 2. DEBUG DE CREDENCIALES (Para ver si Java lee bien las variables)
        System.out.println("------------------------------------------------");
        System.out.println("🚀 INTENTANDO ENVIAR A META...");
        System.out.println("📍 URL: " + url);
        System.out.println("🔑 Token usado (primeros 10): " + (token != null && token.length() > 10 ? token.substring(0, 10) + "..." : "NULL O VACÍO"));
        System.out.println("📱 ID Teléfono: " + phoneId);
        System.out.println("------------------------------------------------");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            // 3. EL DISPARO
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // SI LLEGAMOS AQUÍ, ES VICTORIA
            System.out.println("✅ ¡ÉXITO! Meta respondió: " + response.getBody());

        } catch (HttpClientErrorException e) {
            // 4. AQUÍ ESTÁ LA VERDAD (Si falla, Meta nos dirá por qué)
            System.err.println("❌ ERROR CRÍTICO DE META (Leer atentamente):");
            System.err.println("👉 CÓDIGO: " + e.getStatusCode());
            System.err.println("👉 RAZÓN EXACTA: " + e.getResponseBodyAsString()); // <--- ESTO ES ORO
        } catch (Exception e) {
            System.err.println("❌ ERROR DE JAVA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


