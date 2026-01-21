package ista.M3A.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/webhook")
public class BotController {

    @Value("${whatsapp.token}")
    private String token;

    @Value("${whatsapp.phoneNumberId}")
    private String phoneId;

    @Value("${whatsapp.verifyToken}")
    private String verifyToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. VERIFICACIÓN DEL WEBHOOK (GET)
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

    // 2. RECIBIR MENSAJES (POST)
    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody String body) {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            
            // Navegamos el JSON de Meta para encontrar el mensaje
            if (jsonNode.has("entry") && jsonNode.get("entry").get(0).has("changes") &&
                jsonNode.get("entry").get(0).get("changes").get(0).get("value").has("messages")) {

                JsonNode messageNode = jsonNode.get("entry").get(0).get("changes").get(0).get("value").get("messages").get(0);
                String from = messageNode.get("from").asText();
                String type = messageNode.get("type").asText();
                String msgBody = "";

                // Detectar si es texto o botón
                if ("text".equals(type)) {
                    msgBody = messageNode.get("text").get("body").asText().toLowerCase();
                } else if ("interactive".equals(type)) {
                    msgBody = messageNode.get("interactive").get("button_reply").get("id").asText();
                }

                System.out.println("Mensaje de " + from + ": " + msgBody);

                // --- LOGICA APECS ---
                
                if (msgBody.contains("hola") || msgBody.contains("inicio")) {
                    enviarMenuPrincipal(from);
                } 
                else if (msgBody.equals("btn_cursos")) {
                    enviarListaCursos(from);
                } 
                else if (msgBody.equals("btn_academia")) {
                    enviarInfoAcademia(from);
                }
                else if (Arrays.asList("1", "2", "3", "4", "5").contains(msgBody)) {
                    enviarAsignacionAsesor(from, "Cursos");
                }
                // Aquí podrías agregar más lógica para capturar nombre/cedula
            }
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("ERROR"); // Respondemos OK para que Meta no reintente infinitamente
        }
    }

    // --- MÉTODOS DE ENVÍO ---

    private void enviarMenuPrincipal(String to) {
        Map<String, Object> message = new HashMap<>();
        message.put("messaging_product", "whatsapp");
        message.put("to", to);
        message.put("type", "interactive");

        Map<String, Object> interactive = new HashMap<>();
        interactive.put("type", "button");
        interactive.put("body", Map.of("text", "¡Hola! Bienvenido a APECS. Expertos en Educación Tecnológica.\n\nSelecciona una opción:"));

        Map<String, Object> action = new HashMap<>();
        List<Map<String, Object>> buttons = new ArrayList<>();
        
        buttons.add(crearBoton("btn_cursos", "Ver Cursos para Mí"));
        buttons.add(crearBoton("btn_academia", "Crear mi Academia"));
        
        action.put("buttons", buttons);
        interactive.put("action", action);
        message.put("interactive", interactive);

        sendMessage(message);
    }

    private void enviarListaCursos(String to) {
        String text = "¿Qué habilidad quieres dominar?\n\n" +
                      "1. Ofimática con IA\n" +
                      "2. Análisis de Datos\n" +
                      "3. Programación\n" +
                      "4. Habilidades Blandas\n" +
                      "5. Ver Todo\n\n" +
                      "*Escribe el número de tu interés (ej: 1)*";
        enviarTexto(to, text);
    }

    private void enviarInfoAcademia(String to) {
        String text = "¡Entendido! Creamos tu Propia Plataforma de Capacitación.\n\n" +
                      "Te estamos conectando con un Asesor.\n" +
                      "Por favor déjanos:\n1. Tu Nombre\n2. Cédula o RUC";
        enviarTexto(to, text);
        
        // Simular respuesta del sistema tras 2 seg (en un caso real usarías colas o tareas programadas)
        // Para simplificar aquí, el usuario deberá responder y nosotros detectamos el texto.
    }

    private void enviarAsignacionAsesor(String to, String tema) {
        enviarTexto(to, "✅ Asignado un Asesor de *" + tema + "*. En breve te contactará.");
    }

    // --- UTILIDADES ---

    private void enviarTexto(String to, String contenido) {
        Map<String, Object> message = new HashMap<>();
        message.put("messaging_product", "whatsapp");
        message.put("to", to);
        message.put("type", "text");
        message.put("text", Map.of("body", contenido));
        sendMessage(message);
    }

    private Map<String, Object> crearBoton(String id, String titulo) {
        Map<String, Object> btn = new HashMap<>();
        btn.put("type", "reply");
        btn.put("reply", Map.of("id", id, "title", titulo));
        return btn;
    }

    private void sendMessage(Map<String, Object> payload) {
        String url = "https://graph.facebook.com/v17.0/" + phoneId + "/messages";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            System.err.println("Error enviando mensaje: " + e.getMessage());
        }
    }
}