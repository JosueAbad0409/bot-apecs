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
        // 1. Convertimos todo a minúsculas para evitar problemas
        String mensaje = msgBody.toLowerCase().trim();
        
        System.out.println("Procesando mensaje de " + from + ": " + mensaje);

        // 2. Lógica mejorada
        if (mensaje.contains("hola") || mensaje.contains("inicio") || mensaje.contains("buenas")) {
            enviarMenuPrincipal(from);
        } 
        else if (mensaje.equals("btn_cursos")) {
            enviarListaCursos(from);
        } 
        else if (mensaje.equals("btn_academia")) {
            enviarInfoAcademia(from);
        }
        else {
            // 3. RESPUESTA POR DEFECTO (¡Esto es lo que te faltaba!)
            // Si escribe cualquier otra cosa, le respondemos para no dejarlo en visto.
            enviarTexto(from, "🤖 No entendí tu mensaje, pero estoy vivo. Escribe 'Hola' para ver el menú.");
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
        
        // 1. Imprimir datos clave para depurar (OJO: No imprimimos el token completo por seguridad)
        System.out.println("--- INICIO DEBUG ENVÍO ---");
        System.out.println("1. URL Destino: " + url);
        System.out.println("2. Phone ID usado: " + phoneId);
        System.out.println("3. Token (Primeros 10 chars): " + (token != null && token.length() > 10 ? token.substring(0, 10) + "..." : "NULO O MUY CORTO"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        
        try {
            // Intentamos enviar
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Si funciona:
            System.out.println("4. ¡ÉXITO! Status Code: " + response.getStatusCode());
            System.out.println("5. Respuesta de Meta: " + response.getBody());
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // SI META NOS RECHAZA (Error 4xx)
            System.err.println("!!! ERROR HTTP DE META !!!");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("CUERPO DEL ERROR (LEER ESTO): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // OTROS ERRORES (Java, Red, etc)
            System.err.println("!!! ERROR INTERNO !!!");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("--- FIN DEBUG ENVÍO ---");
        }
    }
}


