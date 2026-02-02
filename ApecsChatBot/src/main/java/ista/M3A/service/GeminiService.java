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

    @Value("${gemini.api.key}") // Asegúrate de tener esto en application.properties
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generarRespuesta(String mensajeUsuario) {
        // Usamos el modelo 'gemini-1.5-flash' que es rápido y barato
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 1. Configurar el Prompt del Sistema (Personalidad de APECS)
            // En la API REST básica de Gemini, lo más fácil es unirlo al texto.
            String promptCompleto = """
                Instrucciones del Sistema:
                Eres el asistente virtual de APECS (Expertos en Educación Tecnológica).
                Tus respuestas deben ser cortas, amables y profesionales.
                Cursos disponibles: Ofimática con IA, Análisis de Datos, Programación, Habilidades Blandas.
                Si preguntan precios, di que un asesor les contactará.
                
                Pregunta del usuario: 
                """ + mensajeUsuario;

            // 2. Construir la estructura JSON específica de Gemini
            // Estructura: { "contents": [ { "parts": [ { "text": "..." } ] } ] }
            
            Map<String, String> part = new HashMap<>();
            part.put("text", promptCompleto);

            List<Map<String, String>> partsList = new ArrayList<>();
            partsList.add(part);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", partsList);

            List<Map<String, Object>> contentsList = new ArrayList<>();
            contentsList.add(content);

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contentsList);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 3. Enviar petición
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // 4. Extraer respuesta (Parsear el JSON de Gemini)
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentResponse = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> partsResponse = (List<Map<String, Object>>) contentResponse.get("parts");
                    return (String) partsResponse.get(0).get("text");
                }
            }
            
            return "Lo siento, no pude procesar tu respuesta con Gemini.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error interno consultando a Gemini.";
        }
    }
}