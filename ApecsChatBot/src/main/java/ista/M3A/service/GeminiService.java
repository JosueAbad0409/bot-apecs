package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generarRespuesta(String mensajeUsuario) {
        // URL oficial de la API de Google Gemini (Modelo Flash 1.5, rápido y gratuito)
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // =================================================================================
            // 🧠 CEREBRO DEL VENDEDOR (PROMPT DEL SISTEMA)
            // Aquí es donde defines la personalidad de tu bot. Edita esto si quieres cambiar cómo habla.
            // =================================================================================
            String systemPrompt = """
                Eres 'APECS Bot', el asistente virtual experto de la empresa APECS (Educación Tecnológica).
                
                TUS REGLAS DE ORO:
                1. Tu tono es profesional, cercano y motivador. Usa emojis ocasionalmente (🚀, 🎓, ✅).
                2. Tienes prohibido inventar precios. Si preguntan precios, di: "Un asesor humano te dará la mejor oferta personalizada".
                3. Tus respuestas deben ser CORTAS (máximo 30 palabras) para que se lean bien en WhatsApp.
                4. El objetivo final es que el usuario elija un curso del menú o pida hablar con un asesor.
                
                TUS PRODUCTOS (CURSOS):
                - Ofimática con IA: Excel, Word y herramientas de Inteligencia Artificial.
                - Análisis de Datos: Power BI, SQL, toma de decisiones.
                - Programación: Java, Spring Boot, Python.
                - Habilidades Blandas: Liderazgo y Oratoria.
                
                PREGUNTA DEL USUARIO:
                """ + mensajeUsuario;

            // =================================================================================
            // CONSTRUCCIÓN DEL JSON PARA GOOGLE (NO TOCAR)
            // =================================================================================
            Map<String, String> part = new HashMap<>();
            part.put("text", systemPrompt);

            List<Map<String, String>> partsList = new ArrayList<>();
            partsList.add(part);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", partsList);

            List<Map<String, Object>> contentsList = new ArrayList<>();
            contentsList.add(content);

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contentsList);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Enviar petición a Google
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // Leer respuesta
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentResponse = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> partsResponse = (List<Map<String, Object>>) contentResponse.get("parts");
                    return (String) partsResponse.get(0).get("text");
                }
            }
            return "Lo siento, estoy actualizando mi base de datos. Por favor escribe 'Menu'.";

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // CASO 1: Google rechaza la petición (Error 400 o 401)
            // Esto nos dirá exactamente por qué Google se queja (API Key mal, JSON mal, etc.)
            String errorReal = e.getResponseBodyAsString();
            System.err.println("❌ ERROR GOOGLE: " + errorReal);
            return "⚠️ Google dice: " + errorReal;

        } catch (Exception e) {
            // CASO 2: Error interno de Java (Conexión, variables nulas, etc.)
            e.printStackTrace();
            return "⚠️ Error Interno: " + e.getMessage(); 
        }
    }
}