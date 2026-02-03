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
        // ✅ USAMOS EL MODELO ESTÁNDAR (Flash 1.5)
        // Esta URL es la más estable actualmente.
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // =================================================================================
            // 🧠 PERSONALIDAD DEL BOT
            // =================================================================================
            String systemPrompt = """
                Eres 'APECS Bot', de la empresa APECS.
                Responde de forma corta (máx 30 palabras), amable y usa emojis.
                
                TUS CURSOS:
                - Ofimática con IA
                - Análisis de Datos
                - Programación
                - Habilidades Blandas
                
                Si preguntan precios, di: "Un asesor humano te dará la mejor oferta".
                
                PREGUNTA DEL USUARIO:
                """ + mensajeUsuario;

            // =================================================================================
            // ESTRUCTURA JSON
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

            // Enviar a Google
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
            return "Lo siento, estoy reiniciando mis sistemas. Intenta en un minuto.";

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Muestra el error real de Google en el chat para que sepamos qué pasa
            String errorReal = e.getResponseBodyAsString();
            System.err.println("❌ ERROR GOOGLE: " + errorReal);
            return "⚠️ Error de Configuración: " + errorReal;

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error Interno: " + e.getMessage(); 
        }
    }
}