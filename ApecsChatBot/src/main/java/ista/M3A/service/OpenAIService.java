package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generarRespuesta(String mensajeUsuario) {
        // 1. CORRECCIÓN: La URL correcta para el chat
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 2. CORRECCIÓN: Estructura del Body (messages en lugar de input)
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of( // Se debe llamar "messages"
                        Map.of(
                                "role", "system",
                                "content", "Eres APECS Bot. Responde corto (máx 30 palabras), amable y con emojis 😊. Si preguntan precios: Un asesor humano te dará la mejor oferta 🤝"
                        ),
                        Map.of(
                                "role", "user",
                                "content", mensajeUsuario
                        )
                ),
                "temperature", 0.7 // Opcional: controla la creatividad
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // Realizamos la petición
            Map response = restTemplate.postForObject(url, entity, Map.class);

            // 3. CORRECCIÓN: Parseo de la respuesta correcta de OpenAI
            // Estructura: choices[0] -> message -> content
            if (response == null || !response.containsKey("choices")) {
                return "❌ Error: Respuesta vacía de OpenAI";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error OpenAI: " + e.getMessage();
        }
    }
}