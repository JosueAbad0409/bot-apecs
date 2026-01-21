package ista.M3A.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ista.M3A.service.WhatsappService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
	
	@Value("${whatsapp.verifyToken}")
    private String verifyToken;

    private final WhatsappService whatsappService;
    private final ObjectMapper objectMapper;

    public WebhookController(WhatsappService whatsappService) {
        this.whatsappService = whatsappService;
        this.objectMapper = new ObjectMapper();
    }

    // 1. VERIFICACIÓN (GET)
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 2. RECEPCIÓN DE MENSAJES (POST)
        @PostMapping
public ResponseEntity<String> receiveMessage(@RequestBody String body) {
    System.out.println("🔔 WEBHOOK RECIBIDO!");
    System.out.println("📦 Body completo: " + body);
    
    try {
        JsonNode jsonNode = objectMapper.readTree(body);

        if (isValidMessage(jsonNode)) {
            System.out.println("✅ Mensaje válido detectado");
            
            JsonNode messageNode = jsonNode.get("entry").get(0)
                .get("changes").get(0).get("value").get("messages").get(0);
            
            String from = messageNode.get("from").asText();
            String type = messageNode.get("type").asText();
            String msgBody = extraerContenidoMensaje(messageNode, type);

            System.out.println("👤 De: " + from);
            System.out.println("📝 Tipo: " + type);
            System.out.println("💬 Contenido: " + msgBody);

            if (msgBody != null) {
                whatsappService.procesarMensaje(from, msgBody);
            }
        } else {
            System.out.println("⚠️ Mensaje NO válido - posiblemente status update");
        }
        
        return ResponseEntity.ok("EVENT_RECEIVED");

    } catch (Exception e) {
        System.err.println("❌ ERROR procesando webhook:");
        e.printStackTrace();
        return ResponseEntity.ok("ERROR");
    }
}

    // Métodos auxiliares privados para limpiar el código principal
    private boolean isValidMessage(JsonNode root) {
        return root.has("entry") && 
               root.get("entry").get(0).has("changes") &&
               root.get("entry").get(0).get("changes").get(0).get("value").has("messages");
    }

    private String extraerContenidoMensaje(JsonNode messageNode, String type) {
        if ("text".equals(type)) {
            return messageNode.get("text").get("body").asText().toLowerCase();
        } else if ("interactive".equals(type)) {
            return messageNode.get("interactive").get("button_reply").get("id").asText();
        }
        return null;
    }


}
