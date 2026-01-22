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

    // 🧠 MEMORIA DEL BOT
    private final Map<String, String> userState = new ConcurrentHashMap<>();

    // ================= CEREBRO DEL BOT =================
    public void procesarMensaje(String from, String msgBody) {
        String mensaje = msgBody.trim().toLowerCase();
        String estadoActual = userState.getOrDefault(from, "START");

        System.out.println("📩 " + from + " [" + estadoActual + "]: " + mensaje);

        // Comandos globales
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

    // ================= LÓGICA DE MENÚS =================

    private void manejarMenuPrincipal(String from, String opcion) {
        if (opcion.equals("1")) {
            enviarListaDeCursos(from);
            userState.put(from, "MENU_CURSOS"); // Cambia estado
        } 
        else if (opcion.equals("2")) {
            // Opción 2: Academia Virtual
            enviarTexto(from, "🏫 *Excelente elección.*\nEstamos preparando la información para tu Academia Virtual.");
            // Link personalizado para Academia
            enviarContactoAsesor(from, "Hola, quiero crear mi propia Academia Virtual 🏫");
            userState.put(from, "START"); // Reinicia
        } 
        else {
            enviarTexto(from, "🤖 Por favor escribe *1* o *2*.");
        }
    }

    private void manejarMenuCursos(String from, String opcion) {
        String cursoElegido = "";
        String mensajeParaAsesor = "";

        switch (opcion) {
            case "1":
                cursoElegido = "Informática con IA 🤖";
                mensajeParaAsesor = "Hola, me interesa el curso de Informática con IA 🤖";
                break;
            case "2":
                cursoElegido = "Análisis de Datos 📊";
                mensajeParaAsesor = "Hola, me interesa el curso de Análisis de Datos 📊";
                break;
            case "3":
                cursoElegido = "Programación 💻";
                mensajeParaAsesor = "Hola, me interesa el curso de Programación 💻";
                break;
            case "4":
                cursoElegido = "Habilidades Blandas 🗣️";
                mensajeParaAsesor = "Hola, me interesa el curso de Habilidades Blandas 🗣️";
                break;
            case "5": // NUEVA OPCIÓN
                cursoElegido = "Catálogo Completo 📚";
                mensajeParaAsesor = "Hola, deseo recibir el catálogo de TODOS los cursos disponibles 📚";
                break;
            default:
                enviarTexto(from, "⚠️ Opción incorrecta. Elige un número del 1 al 5.");
                return;
        }

        // Confirmación al usuario
        String respuesta = "✅ Has seleccionado: *" + cursoElegido + "*\n\n" +
                           "Un asesor académico te enviará el temario y costos a continuación. 👇";
        enviarTexto(from, respuesta);

        // Link dinámico
        enviarContactoAsesor(from, mensajeParaAsesor);
        
        userState.put(from, "START"); // Reiniciamos flujo
    }

    // ================= MENSAJES DE RESPUESTA =================

    private void enviarMenuPrincipal(String numero) {
        String texto =
                "👋 *Bienvenido a APECS* 🎓\n" +
                "Selecciona una opción:\n\n" +
                "1️⃣ Ver cursos disponibles\n" +
                "2️⃣ Crear mi academia virtual";
        enviarTexto(numero, texto);
        userState.put(numero, "MENU_PRINCIPAL");
    }

    private void enviarListaDeCursos(String numero) {
        String texto =
                "📚 *Nuestros Cursos Disponibles*\n" +
                "Selecciona uno para hablar con un asesor:\n\n" +
                "1️⃣ Informática con IA 🤖\n" +
                "2️⃣ Análisis de Datos 📊\n" +
                "3️⃣ Programación 💻\n" +
                "4️⃣ Habilidades Blandas 🗣️\n" +
                "5️⃣ Ver TODOS los cursos 📋"; // Opción agregada

        enviarTexto(numero, texto);
    }

    // ⭐ MÉTODO MEJORADO: Genera el link según lo que el usuario eligió
    private void enviarContactoAsesor(String numero, String mensajeInteres) {
        
        String linkWa = "https://wa.me/593990844161?text=";
        
        try {
            // Codificamos el mensaje para que funcione en la URL (espacios -> %20, etc)
            String mensajeCodificado = URLEncoder.encode(mensajeInteres, StandardCharsets.UTF_8);
            linkWa += mensajeCodificado;
        } catch (Exception e) {
            linkWa += "Hola,%20quiero%20información"; // Fallback por si falla el encoder
        }

        String texto =
                "👨‍💼 *Contactar Asesor Académico*\n" +
                "Dale clic al enlace para chatear directamente:\n\n" +
                "👉 " + linkWa;

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