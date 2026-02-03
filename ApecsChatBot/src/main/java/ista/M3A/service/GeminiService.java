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

    String url = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + apiKey;

    try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
        Eres 'APECS Bot', de la empresa APECS.
        Responde de forma corta (máx 30 palabras), amable y usa emojis 😊

        TUS CURSOS:
        - Ofimática con IA
        - Análisis de Datos
        - Programación
        - Habilidades Blandas

        Si preguntan precios, responde:
        "Un asesor humano te dará la mejor oferta 🤝"

        Pregunta del usuario:
        """ + mensajeUsuario;

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(part);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(content);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("candidates")) {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) responseBody.get("candidates");

            if (!candidates.isEmpty()) {
                Map<String, Object> contentResp =
                        (Map<String, Object>) candidates.get(0).get("content");

                List<Map<String, Object>> partsResp =
                        (List<Map<String, Object>>) contentResp.get("parts");

                return (String) partsResp.get(0).get("text");
            }
        }

        return "🤖 No pude responder en este momento.";

    } catch (Exception e) {
        e.printStackTrace();
        return "⚠️ Error interno: " + e.getMessage();
    }

}