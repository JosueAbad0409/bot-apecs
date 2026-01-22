package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap; // Importante para manejar estados

@Service
public class WhatsappService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.api.token}")
    private String token;

    @Value("${whatsapp.phone.id}")
    private String phoneId;

    private final RestTemplate restTemplate = new RestTemplate();

    // 🧠 MEMORIA DEL BOT: Guarda en qué paso está cada número de teléfono
    // Clave: Número de teléfono, Valor: Estado actual (ej: "MENU_PRINCIPAL", "MENU_CURSOS")
    private final Map<String, String> userState = new ConcurrentHashMap<>();

    // ================= CEREBRO DEL BOT =================
    public void procesarMensaje(String from, String msgBody) {
        String mensaje = msgBody.trim().toLowerCase();
        
        // 1. Obtener el estado actual del usuario (si no existe, es "START")
        String estadoActual = userState.getOrDefault(from, "START");

        System.out.println("📩 Mensaje de " + from + " | Estado: " + estadoActual + " | Texto: " + mensaje);

        // 2. Comandos globales (siempre funcionan)
        if (mensaje.contains("hola") || mensaje.contains("inicio") || mensaje.contains("menu")) {
            enviarMenuPrincipal(from);
            return;
        }

        // 3. Máquina de Estados: Decide qué hacer según dónde esté el usuario
        switch (estadoActual) {
            case "MENU_PRINCIPAL":
                manejarMenuPrincipal(from, mensaje);
                break;
                
            case "MENU_CURSOS":
                manejarMenuCursos(from, mensaje);
                break;
                
            default:
                // Si el estado es desconocido o START, enviamos el menú
                enviarMenuPrincipal(from);
                break;
        }
    }

    // ================= LÓGICA DE MENÚS =================

    private void manejarMenuPrincipal(String from, String opcion) {
        if (opcion.equals("1")) {
            enviarListaDeCursos(from); // Mostramos cursos y pedimos elegir uno
            userState.put(from, "MENU_CURSOS"); // CAMBIAMOS EL ESTADO A "VIENDO CURSOS"
        } 
        else if (opcion.equals("2")) {
            enviarAcademiaVirtual(from);
            userState.put(from, "START"); // Reiniciamos estado o lo dejamos en START
        } 
        else {
            enviarTexto(from, "🤖 Opción no válida. Por favor escribe *1* o *2*.");
        }
    }

    private void manejarMenuCursos(String from, String opcion) {
        // Aquí el usuario ya está dentro de la opción 1, eligiendo un curso específico
        String cursoElegido = "";
        
        switch (opcion) {
            case "1":
                cursoElegido = "Informática con IA 🤖";
                break;
            case "2":
                cursoElegido = "Análisis de Datos 📊";
                break;
            case "3":
                cursoElegido = "Programación 💻";
                break;
            case "4":
                cursoElegido = "Habilidades Blandas 🗣️";
                break;
            default:
                enviarTexto(from, "⚠️ Opción incorrecta. Elige un número del 1 al 4 para ver detalles del curso.");
                return; // Salimos para no enviar el asesor todavía
        }

        // Si eligió un curso válido:
        enviarDetalleCurso(from, cursoElegido);
        userState.put(from, "START"); // Reiniciamos el flujo tras dar la info
    }

    // ================= MENSAJES DE RESPUESTA =================

    private void enviarMenuPrincipal(String numero) {
        String texto =
                "👋 *¡Hola! Bienvenido a APECS* 🎓\n\n" +
                "Somos expertos en *Educación y Capacitación Tecnológica* 💻\n\n" +
                "Selecciona una opción:\n\n" +
                "1️⃣ Ver cursos disponibles\n" +
                "2️⃣ Crear mi academia virtual";

        enviarTexto(numero, texto);
        userState.put(numero, "MENU_PRINCIPAL"); // Establecemos el estado inicial
    }

    private void enviarListaDeCursos(String numero) {
        String texto =
                "📚 *Nuestros Cursos Disponibles*\n\n" +
                "Escribe el número del curso que te interesa para ver más detalles:\n\n" +
                "1️⃣ Informática con IA 🤖\n" +
                "2️⃣ Análisis de Datos 📊\n" +
                "3️⃣ Programación 💻\n" +
                "4️⃣ Habilidades Blandas 🗣️";

        enviarTexto(numero, texto);
        // NO enviamos el contacto del asesor todavía, esperamos que elija
    }

    private void enviarDetalleCurso(String numero, String nombreCurso) {
        String texto = 
                "✅ Has seleccionado: *" + nombreCurso + "*\n\n" +
                "Este curso está diseñado para potenciar tu perfil profesional al máximo. 🚀\n\n" +
                "📌 *¿Quieres inscribirte o recibir el temario?*\n" +
                "Un asesor está listo para atenderte.";
        
        enviarTexto(numero, texto);
        enviarContactoAsesor(numero); // AHORA SÍ enviamos el asesor
    }

    private void enviarAcademiaVirtual(String numero) {
        String texto =
                "🏫 *Crear tu Academia Virtual APECS*\n\n" +
                "Nos especializamos en crear tu *propia plataforma de capacitación* 🎓\n\n" +
                "📦 Te entregamos aula virtual, herramientas y más.\n\n" +
                "⏳ En un momento nos comunicamos contigo.";

        enviarTexto(numero, texto);
        enviarContactoAsesor(numero);
    }

    private void enviarContactoAsesor(String numero) {
        String linkWa = "https://wa.me/593990844161?text=Hola,%20quiero%20información%20de%20APECS";
        String texto =
                "👨‍💼 *Habla con un Asesor Académico*\n" +
                "Haz clic aquí 👉 " + linkWa;
        enviarTexto(numero, texto);
    }

    // ================= MOTOR DE ENVÍO (Sin cambios) =================
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
            System.out.println("✅ Mensaje enviado a " + numeroDestino);
        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje: " + e.getMessage());
        }
    }
}