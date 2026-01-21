package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    // --- CEREBRO DEL BOT 🧠 ---
    public void procesarMensaje(String from, String msgBody) {
        // 1. Limpiamos el mensaje (quitar espacios y minúsculas)
        String mensaje = msgBody.trim().toLowerCase();

        System.out.println("📩 Mensaje de " + from + ": " + mensaje);

        // 2. Lógica del Menú Numérico
        if (mensaje.contains("hola") || mensaje.contains("inicio") || mensaje.contains("buenas")) {
            enviarMenuPrincipal(from);
        } 
        else if (mensaje.equals("1")) {
            enviarListaCursos(from);
        } 
        else if (mensaje.equals("2")) {
            enviarContactoAsesor(from);
        } 
        else {
            // Si escribe cualquier otra cosa, le recordamos el menú
            enviarTexto(from, "🤖 No entendí. Por favor responde con el número de la opción:\n\n1️⃣ Ver Cursos\n2️⃣ Hablar con Asesor");
        }
    }

    // --- OPCIÓN 0: EL MENÚ PRINCIPAL ---
    private void enviarMenuPrincipal(String numero) {
        String texto = "👋 *¡Hola! Bienvenido a APECS* 🎓\n" +
                       "Tu futuro tecnológico empieza aquí.\n\n" +
                       "¿En qué podemos ayudarte hoy?\n" +
                       "*(Escribe el número de la opción)*\n\n" +
                       "1️⃣ Ver Cursos Disponibles\n" +
                       "2️⃣ Hablar con un Asesor Humano";
        enviarTexto(numero, texto);
    }

    // --- OPCIÓN 1: LOS CURSOS ---
    private void enviarListaCursos(String numero) {
        String texto = "📚 *Nuestros Cursos Destacados:*\n\n" +
                       "☕ *Java Spring Boot* - Backend Pro\n" +
                       "🐍 *Python para Datos* - IA y Big Data\n" +
                       "📱 *Desarrollo Android* - Apps Móviles\n" +
                       "🎨 *Diseño UX/UI* - Prototipado Figma\n\n" +
                       "👇 *¿Te interesa uno?*\n" +
                       "Escribe *2* para contactar a un asesor y e inscribirte.";
        enviarTexto(numero, texto);
    }

    // --- OPCIÓN 2: EL ASESOR (LINK) ---
    private void enviarContactoAsesor(String numero) {
        // OJO: Cambia el 593... por el número REAL del asesor de APECS
        String linkWa = "https://wa.me/593999999999?text=Hola,%20quiero%20info%20de%20los%20cursos";
        
        String texto = "👨‍💼 *Asesor Académico APECS*\n\n" +
                       "Para una atención personalizada, chatea directo con nuestro asesor aquí:\n\n" +
                       "👉 " + linkWa + "\n\n" +
                       "¡Te esperamos!";
        enviarTexto(numero, texto);
    }

    // --- MÉTODO GENÉRICO PARA ENVIAR TEXTO (EL MOTOR) ---
    private void enviarTexto(String numeroDestino, String mensaje) {
        String url = apiUrl + phoneId + "/messages";

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", numeroDestino);
        payload.put("type", "text");
        
        Map<String, String> textObj = new HashMap<>();
        textObj.put("body", mensaje);
        payload.put("text", textObj);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            System.out.println("✅ Mensaje enviado a: " + numeroDestino);
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje: " + e.getMessage());
        }
    }
}