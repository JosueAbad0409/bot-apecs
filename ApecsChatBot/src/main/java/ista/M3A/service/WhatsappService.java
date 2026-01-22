package ista.M3A.service;

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

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> userState = new ConcurrentHashMap<>();

    // ================= CEREBRO DEL BOT =================
    public void procesarMensaje(String from, String msgBody) {
        String mensaje = msgBody.trim().toLowerCase();
        String estadoActual = userState.getOrDefault(from, "START");

        System.out.println("📩 " + from + " [" + estadoActual + "]: " + mensaje);

        // Reinicio global
        if (mensaje.contains("hola") || mensaje.contains("inicio") || mensaje.contains("menu")) {
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
                enviarMenuPrincipal(from);
                break;
        }
    }

    // ================= LÓGICA DE FLUJO (Según Diagrama) =================

    private void manejarMenuPrincipal(String from, String opcion) {
        if (opcion.equals("1")) {
            // Rama Izquierda del Diagrama
            enviarListaDeCursos(from);
            userState.put(from, "MENU_CURSOS"); 
        } 
        else if (opcion.equals("2")) {
            // Rama Derecha del Diagrama
            enviarAcademiaVirtual(from);
            // El diagrama dice "En este momento estoy conectándote...", así que enviamos el link de una
            enviarContactoAsesor(from, "Hola, quiero crear mi Academia Virtual. Envío mis datos: ");
            userState.put(from, "START"); // Fin del flujo
        } 
        else {
            enviarTexto(from, "🤖 Por favor selecciona una opción válida (1 o 2).");
        }
    }

    private void manejarMenuCursos(String from, String opcion) {
        String cursoElegido = "";
        String mensajeAsesor = "";

        switch (opcion) {
            case "1":
                cursoElegido = "Ofimática con IA";
                mensajeAsesor = "Hola, deseo información sobre el curso de Ofimática con IA.";
                break;
            case "2":
                cursoElegido = "Análisis de Datos";
                mensajeAsesor = "Hola, deseo información sobre el curso de Análisis de Datos.";
                break;
            case "3":
                cursoElegido = "Programación";
                mensajeAsesor = "Hola, deseo información sobre el curso de Programación.";
                break;
            case "4":
                cursoElegido = "Habilidades Blandas";
                mensajeAsesor = "Hola, deseo información sobre Habilidades Blandas.";
                break;
            case "5":
                cursoElegido = "Oferta Completa";
                mensajeAsesor = "Hola, deseo descargar su oferta completa de cursos.";
                break;
            default:
                enviarTexto(from, "⚠️ Opción incorrecta. Elige un número del 1 al 5.");
                return;
        }

        // Según el diagrama: "Se le asigna un Asesor"
        enviarTexto(from, "✅ Has seleccionado: *" + cursoElegido + "*\nTe conectamos con un asesor para darte la información correspondiente.");
        enviarContactoAsesor(from, mensajeAsesor);
        
        userState.put(from, "START"); // Reinicia
    }

    // ================= MENSAJES EXACTOS (Copy-Paste del Diagrama) =================

    private void enviarMenuPrincipal(String numero) {
        String texto =
                "👋 *¡Hola! Bienvenido a APECS.*\n" +
                "Somos expertos en Educación y Capacitación Tecnológica.\n\n" +
                "Para brindarte la mejor información, por favor selecciona una opción:\n\n" +
                "1️⃣ Ver Cursos para Mí / Capacitación\n" +
                "2️⃣ Crear mi academia virtual";

        enviarTexto(numero, texto);
        userState.put(numero, "MENU_PRINCIPAL");
    }

    private void enviarListaDeCursos(String numero) {
        String texto =
                "¿Qué habilidad quieres dominar hoy?\n" +
                "Tenemos el curso perfecto para impulsar tu perfil profesional:\n\n" +
                "1️⃣ *Ofimática con IA:*\nDomina Excel y herramientas inteligentes.\n\n" +
                "2️⃣ *Análisis de Datos:*\nAprende a tomar decisiones con datos reales.\n\n" +
                "3️⃣ *Programación:*\nCrea soluciones y soporte técnico.\n\n" +
                "4️⃣ *Habilidades Blandas:*\nLiderazgo y comunicación efectiva.\n\n" +
                "5️⃣ *Ver Todo:*\nDescarga nuestra oferta completa.";

        enviarTexto(numero, texto);
    }

    private void enviarAcademiaVirtual(String numero) {
        // Texto exacto del cuadro derecho
        String texto =
                "¡Entendido! Nos especializamos en crear *Tu Propia Plataforma de Capacitación*.\n\n" +
                "Te entregamos tu Aula Virtual lista para que puedas entrenar a tu equipo de trabajo o publicar tus cursos fácilmente.\n\n" +
                "En este momento estoy conectándote con un Asesor de Proyectos para atenderte mejor.\n\n" +
                "📝 *Por favor, espera un momento y déjanos tus datos:*\n" +
                "1. Tu Nombre.\n" +
                "2. Tu número de Cédula o RUC.";

        enviarTexto(numero, texto);
    }

    private void enviarContactoAsesor(String numero, String mensajePredefinido) {
        String linkWa = "https://wa.me/593990844161?text=";
        try {
            linkWa += URLEncoder.encode(mensajePredefinido, StandardCharsets.UTF_8);
        } catch (Exception e) {
            linkWa += "Hola,%20solicito%20información";
        }

        String texto = "👉 *Clic aquí para hablar con el Asesor:*\n" + linkWa;
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