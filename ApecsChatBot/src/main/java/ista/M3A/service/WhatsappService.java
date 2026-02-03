package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private OpenAIService openAIService;

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.api.token}")
    private String token;

    @Value("${whatsapp.phone.id}")
    private String phoneId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> userState = new ConcurrentHashMap<>();

    // ================= CEREBRO DEL BOT =================
    public void procesarMensaje(String from, String msgBody) {
        String mensaje = msgBody.trim(); // No hacemos toLowerCase para que la IA entienda mejor los nombres propios
        String estadoActual = userState.getOrDefault(from, "START");

        System.out.println("📩 " + from + " [" + estadoActual + "]: " + mensaje);

        // Comandos de reinicio forzado
        if (mensaje.equalsIgnoreCase("hola") || mensaje.equalsIgnoreCase("menu") || mensaje.equalsIgnoreCase("inicio")) {
            userState.put(from, "START"); // Reseteamos estado
            enviarMenuPrincipal(from);
            return;
        }

        switch (estadoActual) {
            case "MENU_PRINCIPAL":
                manejarMenuPrincipal(from, mensaje);
                break;
            case "MENU_CURSOS":
                manejarMenuCursos(from, mensaje);
                break;
            default:
                enviarTexto(from, "🤖 _Pensando..._"); // Opcional: Feedback visual
                String respuestaIA = openAIService.generarRespuesta(mensaje);
                enviarTexto(from, respuestaIA);
                break;
        }
    }

    // ================= LÓGICA DE FLUJO (Según Diagrama) =================

    private void manejarMenuPrincipal(String from, String opcion) {
        if (opcion.equals("1")) {
            enviarListaDeCursos(from);
            userState.put(from, "MENU_CURSOS");
        } 
        else if (opcion.equals("2")) {
            enviarAcademiaVirtual(from);
            enviarContactoAsesor(from, "👋 Hola, quiero crear mi Academia Virtual.");
            userState.put(from, "START");
        } 
        else {
            // CAMBIO: Si no es 1 ni 2, tal vez está haciendo una pregunta
            // Regresamos el estado a START para que la próxima vez entre directo a la IA
            userState.put(from, "START"); 
            String respuestaIA = openAIService.generarRespuesta(opcion);
            enviarTexto(from, respuestaIA);
        }
    }

    private void manejarMenuCursos(String from, String opcion) {
        String cursoElegido = "";
        String mensajeAsesor = "";

        switch (opcion) {
            case "1":
                cursoElegido = "Ofimática con IA 🤖";
                mensajeAsesor = "Hola, deseo información sobre el curso de Ofimática con IA.";
                break;
            case "2":
                cursoElegido = "Análisis de Datos 📊";
                mensajeAsesor = "Hola, deseo información sobre el curso de Análisis de Datos.";
                break;
            case "3":
                cursoElegido = "Programación 💻";
                mensajeAsesor = "Hola, deseo información sobre el curso de Programación.";
                break;
            case "4":
                cursoElegido = "Habilidades Blandas 🗣️";
                mensajeAsesor = "Hola, deseo información sobre Habilidades Blandas.";
                break;
            case "5":
                cursoElegido = "Oferta Completa 📂";
                mensajeAsesor = "Hola, deseo descargar su oferta completa de cursos.";
                break;
            default:
                enviarTexto(from, "⚠️ *Opción incorrecta.*\nPor favor, elige un número del *1 al 5*.");
                return;
        }

        // Según el diagrama: "Se le asigna un Asesor"
        enviarTexto(from, "✅ *¡Excelente elección!*\n\nHas seleccionado: *" + cursoElegido + "*\n\n👤 _Te conectamos con un asesor para darte toda la información._");
        enviarContactoAsesor(from, mensajeAsesor);
        
        userState.put(from, "START"); // Reinicia
    }

    // ================= MENSAJES EXACTOS (Decorados) =================

    private void enviarMenuPrincipal(String numero) {
        String texto =
                "👋 *¡Hola! Bienvenido a APECS.*\n" +
                "🚀 _Expertos en Educación y Capacitación Tecnológica._\n\n" +
                "🎯 *Para brindarte la mejor información, selecciona una opción:*\n\n" +
                "1️⃣  Ver Cursos para Mí / Capacitación 🎓\n" +
                "2️⃣  Crear mi Academia Virtual 🏫";

        enviarTexto(numero, texto);
        userState.put(numero, "MENU_PRINCIPAL");
    }

    private void enviarListaDeCursos(String numero) {
        String texto =
                "🎓 *¿Qué habilidad quieres dominar hoy?*\n" +
                "🔥 _Tenemos el curso perfecto para impulsar tu perfil profesional:_\n\n" +
                "1️⃣  *Ofimática con IA* 🤖\n      _Domina Excel y herramientas inteligentes._\n\n" +
                "2️⃣  *Análisis de Datos* 📊\n      _Aprende a tomar decisiones con datos reales._\n\n" +
                "3️⃣  *Programación* 💻\n      _Crea soluciones y soporte técnico._\n\n" +
                "4️⃣  *Habilidades Blandas* 🗣️\n      _Liderazgo y comunicación efectiva._\n\n" +
                "5️⃣  *Ver Todo* 📂\n      _Descarga nuestra oferta completa._";

        enviarTexto(numero, texto);
    }

    private void enviarAcademiaVirtual(String numero) {
        // Texto exacto del cuadro derecho pero mejorado
        String texto =
                "🙌 *¡Entendido!*\n" +
                "💻 Nos especializamos en crear *Tu Propia Plataforma de Capacitación*.\n\n" +
                "🚀 Te entregamos tu *Aula Virtual lista* para que puedas entrenar a tu equipo o publicar tus cursos fácilmente.\n\n" +
                "👨‍💻 *En este momento estoy conectándote con un Asesor de Proyectos...*\n\n" +
                "📝 *Por favor, espera un momento y déjanos tus datos:*\n" +
                "   🔹 1. Tu Nombre\n" +
                "   🔹 2. Tu número de Cédula o RUC";

        enviarTexto(numero, texto);
    }

    private void enviarContactoAsesor(String numero, String mensajePredefinido) {
        String linkWa = "https://wa.me/593990844161?text=";
        try {
            linkWa += URLEncoder.encode(mensajePredefinido, StandardCharsets.UTF_8);
        } catch (Exception e) {
            linkWa += "Hola,%20solicito%20información";
        }

        String texto = "👇 *Clic aquí para hablar con el Asesor:*\n📲 " + linkWa;
        enviarTexto(numero, texto);
    }

    // ================= MOTOR DE ENVÍO (Standard) =================
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
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje: " + e.getMessage());
        }
    }
}