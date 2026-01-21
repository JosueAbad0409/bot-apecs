package ista.M3A.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ista.M3A.service.WhatsappService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;


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

    @PostConstruct
public void init() {
    System.out.println("════════════════════════════════════════════");
    System.out.println("🤖 WEBHOOK CONTROLLER INICIALIZADO");
    System.out.println("🔑 Verify Token: " + verifyToken);
    System.out.println("📍 Ruta: /webhook");
    System.out.println("════════════════════════════════════════════");
}

    // 1. VERIFICACIÓN (GET)
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        System.out.println("\n════════════════════════════════════════════");
        System.out.println("🔍 VERIFICACIÓN DE WEBHOOK (GET)");
        System.out.println("Mode: " + mode);
        System.out.println("Token recibido: " + token);
        System.out.println("Token esperado: " + verifyToken);
        System.out.println("Challenge: " + challenge);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            System.out.println("✅ VERIFICACIÓN EXITOSA");
            System.out.println("════════════════════════════════════════════\n");
            return ResponseEntity.ok(challenge);
        } else {
            System.out.println("❌ VERIFICACIÓN FALLIDA");
            System.out.println("════════════════════════════════════════════\n");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 2. RECEPCIÓN DE MENSAJES (POST)
    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody String body) {
        System.out.println("\n════════════════════════════════════════════");
        System.out.println("🔔 ¡¡¡WEBHOOK POST RECIBIDO!!!");
        System.out.println("⏰ Timestamp: " + System.currentTimeMillis());
        System.out.println("📦 Body completo:");
        System.out.println(body);
        System.out.println("════════════════════════════════════════════");
        
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            System.out.println("✅ JSON parseado correctamente");

            if (isValidMessage(jsonNode)) {
                System.out.println("✅ Es un mensaje de texto válido");
                
                JsonNode messageNode = jsonNode.get("entry").get(0)
                    .get("changes").get(0).get("value").get("messages").get(0);
                
                String from = messageNode.get("from").asText();
                String type = messageNode.get("type").asText();
                String msgBody = extraerContenidoMensaje(messageNode, type);

                System.out.println("👤 Número: " + from);
                System.out.println("📝 Tipo: " + type);
                System.out.println("💬 Mensaje: " + msgBody);
                System.out.println("🚀 Procesando con WhatsappService...");

                if (msgBody != null) {
                    whatsappService.procesarMensaje(from, msgBody);
                    System.out.println("✅ Mensaje procesado");
                } else {
                    System.out.println("⚠️ msgBody es null");
                }
            } else {
                System.out.println("⚠️ NO es un mensaje válido - posiblemente status update");
                System.out.println("Estructura JSON: " + jsonNode.toString());
            }
            
            System.out.println("════════════════════════════════════════════\n");
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            System.err.println("❌❌❌ ERROR CRÍTICO EN WEBHOOK ❌❌❌");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Clase: " + e.getClass().getName());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("════════════════════════════════════════════\n");
            return ResponseEntity.ok("ERROR");
        }
    }

    private boolean isValidMessage(JsonNode root) {
        boolean hasEntry = root.has("entry");
        if (!hasEntry) {
            System.out.println("❌ No tiene 'entry'");
            return false;
        }
        
        boolean hasChanges = root.get("entry").get(0).has("changes");
        if (!hasChanges) {
            System.out.println("❌ No tiene 'changes'");
            return false;
        }
        
        boolean hasMessages = root.get("entry").get(0)
            .get("changes").get(0).get("value").has("messages");
        if (!hasMessages) {
            System.out.println("❌ No tiene 'messages' - es un status update");
            return false;
        }
        
        return true;
    }

    private String extraerContenidoMensaje(JsonNode messageNode, String type) {
        if ("text".equals(type)) {
            return messageNode.get("text").get("body").asText();
        } else if ("interactive".equals(type)) {
            return messageNode.get("interactive").get("button_reply").get("id").asText();
        }
        return null;
    }
}