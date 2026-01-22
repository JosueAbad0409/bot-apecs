package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    // ================= CEREBRO DEL BOT =================
    public void procesarMensaje(String from, String msgBody) {

        String mensaje = msgBody.trim().toLowerCase();
        System.out.println("📩 Mensaje de " + from + ": " + mensaje);

        if (mensaje.contains("hola") || mensaje.contains("inicio") || mensaje.contains("menu")) {
            enviarMenuPrincipal(from);
        }
        else if (mensaje.equals("1")) {
            enviarCursos(from);
        }
        else if (mensaje.equals("2")) {
            enviarAcademiaVirtual(from);
        }
        else {
            enviarTexto(from,
                    "🤖 *No entendí tu mensaje*\n\n" +
                    "Por favor escribe el número de una opción:\n\n" +
                    "1️⃣ Ver cursos para mí / capacitación\n" +
                    "2️⃣ Crear mi academia virtual");
        }
    }

    // ================= MENÚ PRINCIPAL =================
    private void enviarMenuPrincipal(String numero) {

        String texto =
                "👋 *¡Hola! Bienvenido a APECS* 🎓\n\n" +
                "Somos expertos en *Educación y Capacitación Tecnológica* 💻\n\n" +
                "Para brindarte la mejor información, selecciona una opción:\n\n" +
                "1️⃣ Ver cursos para mí / capacitación\n" +
                "2️⃣ Crear mi academia virtual";

        enviarTexto(numero, texto);
    }

    // ================= OPCIÓN 1: CURSOS =================
    private void enviarCursos(String numero) {

        String texto =
                "📚 *¿Qué habilidad quieres dominar hoy?*\n\n" +
                "Tenemos el curso perfecto para impulsar tu perfil profesional 🚀\n\n" +
                "1️⃣ Informática con IA 🤖\n" +
                "   • Domina Excel y herramientas inteligentes\n\n" +
                "2️⃣ Análisis de Datos 📊\n" +
                "   • Aprende a tomar decisiones con datos reales\n\n" +
                "3️⃣ Programación 💻\n" +
                "   • Crea soluciones y soporte técnico\n\n" +
                "4️⃣ Habilidades Blandas 🗣️\n" +
                "   • Liderazgo y comunicación efectiva\n\n" +
                "📌 *Un asesor se comunicará contigo para brindarte la información correspondiente*";

        enviarTexto(numero, texto);
        enviarContactoAsesor(numero);
    }

    // ================= OPCIÓN 2: ACADEMIA VIRTUAL =================
    private void enviarAcademiaVirtual(String numero) {

        String texto =
                "🏫 *Crear tu Academia Virtual APECS*\n\n" +
                "Nos especializamos en crear tu *propia plataforma de capacitación* 🎓\n\n" +
                "📦 Te entregamos:\n" +
                "✅ Aula virtual lista\n" +
                "✅ Herramientas para capacitar a tu equipo\n" +
                "✅ Publicación fácil de tus cursos\n\n" +
                "👨‍💼 En este momento te conectamos con un *Asesor de Proyectos*\n\n" +
                "📝 Por favor ten listos los siguientes datos:\n" +
                "1️⃣ Tu nombre\n" +
                "2️⃣ Tu número de cédula o RUC\n\n" +
                "⏳ En un momento nos comunicamos contigo";

        enviarTexto(numero, texto);
        enviarContactoAsesor(numero);
    }

    // ================= ASESOR HUMANO =================
    private void enviarContactoAsesor(String numero) {

        String linkWa = "https://wa.me/593990844161?text=Hola,%20quiero%20información%20de%20APECS";

        String texto =
                "👨‍💼 *Asesor Académico APECS*\n\n" +
                "Para una atención personalizada, escríbenos aquí 👇\n\n" +
                "👉 " + linkWa + "\n\n" +
                "✨ ¡Será un gusto ayudarte!";

        enviarTexto(numero, texto);
    }

    // ================= MOTOR DE ENVÍO =================
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
            System.out.println("✅ Mensaje enviado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje: " + e.getMessage());
        }
    }
}
