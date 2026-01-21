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
        try {
            JsonNode jsonNode = objectMapper.readTree(body);

            // Verificamos estructura básica del JSON de Meta
            if (isValidMessage(jsonNode)) {
                JsonNode messageNode = jsonNode.get("entry").get(0).get("changes").get(0).get("value").get("messages").get(0);
                
                String from = messageNode.get("from").asText();
                String type = messageNode.get("type").asText();
                String msgBody = extraerContenidoMensaje(messageNode, type);

                if (msgBody != null) {
                    // Delegamos la lógica al servicio
                    whatsappService.procesarMensaje(from, msgBody);
                }
            }
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("ERROR"); // Siempre responder OK a Meta
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
