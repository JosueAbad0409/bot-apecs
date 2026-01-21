package ista.M3A.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsappService {
	
	@Value("${whatsapp.token}")
    private String token;

    @Value("${whatsapp.phoneNumberId}")
    private String phoneId;

    @Value("${whatsapp.apiUrl}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    
    public WhatsappService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // --- LÓGICA DEL NEGOCIO ---
    
    public void procesarMensaje(String from, String msgBody) {
        System.out.println("Procesando mensaje de " + from + ": " + msgBody);

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
            // Aquí podrías validar si viene de la opción cursos
            enviarAsignacionAsesor(from, "Cursos");
        } 
        else {
            // Manejo por defecto o captura de datos (Nombre/Cedula)
            // Por ahora, solo confirmamos recepción si no es un comando conocido
            // enviarTexto(from, "Gracias, un asesor revisará tu mensaje.");
        }
    }

    // --- MÉTODOS DE RESPUESTA ESPECÍFICOS ---

    private void enviarMenuPrincipal(String to) {
        List<Map<String, Object>> buttons = new ArrayList<>();
        buttons.add(crearBoton("btn_cursos", "Ver Cursos para Mí"));
        buttons.add(crearBoton("btn_academia", "Crear mi Academia"));

        String bodyText = "¡Hola! Bienvenido a APECS. Expertos en Educación Tecnológica.\n\n" +
                          "Para brindarte la mejor información, selecciona una opción:";
        
        enviarMensajeInteractivo(to, bodyText, buttons);
    }

    private void enviarListaCursos(String to) {
        String text = "¿Qué habilidad quieres dominar hoy?\n\n" +
                      "1. Ofimática con IA\n" +
                      "2. Análisis de Datos\n" +
                      "3. Programación\n" +
                      "4. Habilidades Blandas\n" +
                      "5. Ver Todo\n\n" +
                      "*Escribe el número de la opción (ej: 1)*";
        enviarTexto(to, text);
    }

    private void enviarInfoAcademia(String to) {
        String text = "¡Entendido! Nos especializamos en crear Tu Propia Plataforma.\n\n" +
                      "Estamos conectándote con un Asesor de Proyectos.\n" +
                      "Por favor déjanos tus datos:\n1. Tu Nombre\n2. Cédula o RUC";
        enviarTexto(to, text);
        
        // Simulación de asignación automática tras unos segundos (opcional)
        // En producción usarías @Async o Scheduled tasks
    }

    private void enviarAsignacionAsesor(String to, String tema) {
        String text = "✅ Se le ha asignado un Asesor especializado en *" + tema + "*. En breve le escribirá.";
        enviarTexto(to, text);
    }

    // --- MÉTODOS GENÉRICOS DE ENVÍO API META ---

    private void enviarTexto(String to, String contenido) {
        Map<String, Object> message = new HashMap<>();
        message.put("messaging_product", "whatsapp");
        message.put("to", to);
        message.put("type", "text");
        message.put("text", Map.of("body", contenido));
        ejecutarEnvio(message);
    }

    private void enviarMensajeInteractivo(String to, String bodyText, List<Map<String, Object>> buttons) {
        Map<String, Object> message = new HashMap<>();
        message.put("messaging_product", "whatsapp");
        message.put("to", to);
        message.put("type", "interactive");

        Map<String, Object> interactive = new HashMap<>();
        interactive.put("type", "button");
        interactive.put("body", Map.of("text", bodyText));
        
        Map<String, Object> action = new HashMap<>();
        action.put("buttons", buttons);
        
        interactive.put("action", action);
        message.put("interactive", interactive);

        ejecutarEnvio(message);
    }

    private Map<String, Object> crearBoton(String id, String titulo) {
        Map<String, Object> btn = new HashMap<>();
        btn.put("type", "reply");
        btn.put("reply", Map.of("id", id, "title", titulo));
        return btn;
    }

    private void ejecutarEnvio(Map<String, Object> payload) {
        String url = apiUrl + phoneId + "/messages";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            System.err.println("Error enviando a Meta: " + e.getMessage());
        }
    }
}
