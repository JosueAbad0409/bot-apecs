package ista.M3A.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WhatsappService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.api.token}")
    private String token;

    @Value("${whatsapp.phone.id}")
    private String phoneId;

    @Autowired
    private OpenAIService openAIService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> userState = new ConcurrentHashMap<>();

    // ================= CEREBRO DEL BOT =================
    public void procesarMensaje(String from, String msgBody) {

        String mensaje = msgBody.trim();
        String estadoActual = userState.getOrDefault(from, "START");

        System.out.println("📩 " + from + " [" + estadoActual + "]: " + mensaje);

        // 🔄 Reinicio global
        if (mensaje.equalsIgnoreCase("hola")
                || mensaje.equalsIgnoreCase("inicio")
                || mensaje.equalsIgnoreCase("menu")) {
            enviarMenuPrincipal(from);
            return;
        }

        // 🔢 Si es número → manejar menús
        if (esNumero(mensaje)) {
            switch (estadoActual) {
                case "MENU_PRINCIPAL":
                    manejarMenuPrincipal(from, mensaje);
                    return;
                case "MENU_CURSOS":
                    manejarMenuCursos(from, mensaje);
                    return;
                default:
                    enviarMenuPrincipal(from);
                    return;
            }
        }

        // 🤖 Si NO es número → OpenAI responde
        System.out.println("🧠 Consultando a OpenAI para: " + mensaje);
        String respuestaIA = openAIService.generarRespuesta(mensaje);
        enviarTexto(from, respuestaIA);
    }

    // ================= AUXILIAR =================
    private boolean esNumero(String texto) {
        try {
            Integer.parseInt(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ================= MENÚS =================
    private void manejarMenuPrincipal(String from, String opcion) {
        if (opcion.equals("1")) {
            enviarListaDeCursos(from);
            userState.put(from, "MENU_CURSOS");
        } else if (opcion.equals("2")) {
            enviarAcademiaVirtual(from);
            enviarContactoAsesor(from, "Hola, quiero crear mi Academia Virtual.");
            userState.put(from, "START");
        } else {
            enviarTexto(from, "⚠️ Opción inválida. Responde *1* o *2*.");
        }
    }

    private void manejarMenuCursos(String from, String opcion) {
        String curso;
        String mensajeAsesor;

        switch (opcion) {
            case "1":
                curso = "Ofimática con IA 🤖";
                mensajeAsesor = "Hola, deseo información sobre Ofimática con IA.";
                break;
            case "2":
                curso = "Análisis de Datos 📊";
                mensajeAsesor = "Hola, deseo información sobre Análisis de Datos.";
                break;
            case "3":
                curso = "Programación 💻";
                mensajeAsesor = "Hola, deseo información sobre Programación.";
                break;
            case "4":
                curso = "Habilidades Blandas 🗣️";
                mensajeAsesor = "Hola, deseo información sobre Habilidades Blandas.";
                break;
            case "5":
                curso = "Oferta completa 📂";
                mensajeAsesor = "Hola, deseo la oferta completa de cursos.";
                break;
            default:
                enviarTexto(from, "⚠️ Elige un número del *1 al 5*.");
                return;
        }

        enviarTexto(from, "✅ Elegiste *" + curso + "*.\nTe conectamos con un asesor 👤");
        enviarContactoAsesor(from, mensajeAsesor);
        userState.put(from, "START");
    }

    // ================= MENSAJES =================
    private void enviarMenuPrincipal(String numero) {
        String texto =
                "👋 *Bienvenido a APECS*\n\n" +
                "1️⃣ Ver Cursos 🎓\n" +
                "2️⃣ Crear mi Academia Virtual 🏫";

        enviarTexto(numero, texto);
        userState.put(numero, "MENU_PRINCIPAL");
    }

    private void enviarListaDeCursos(String numero) {
        String texto =
                "🎓 *Nuestros Cursos:*\n\n" +
                "1️⃣ Ofimática con IA 🤖\n" +
                "2️⃣ Análisis de Datos 📊\n" +
                "3️⃣ Programación 💻\n" +
                "4️⃣ Habilidades Blandas 🗣️\n" +
                "5️⃣ Ver Todo 📂";

        enviarTexto(numero, texto);
    }

    private void enviarAcademiaVirtual(String numero) {
        enviarTexto(numero,
                "💻 Creamos tu *Academia Virtual* lista para usar 🚀\n" +
                "Un asesor te contactará enseguida 👨‍💻");
    }

    private void enviarContactoAsesor(String numero, String mensaje) {
        String link = "https://wa.me/593990844161?text=";
        try {
            link += URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
        } catch (Exception ignored) {}

        enviarTexto(numero, "📲 Habla con un asesor:\n" + link);
    }

    // ================= ENVÍO WHATSAPP =================
    private void enviarTexto(String numeroDestino, String mensaje) {

        String url = apiUrl + phoneId + "/messages";

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", numeroDestino);
        payload.put("type", "text");
        payload.put("text", Map.of("body", mensaje));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            System.err.println("❌ Error WhatsApp: " + e.getMessage());
        }
    }
}
