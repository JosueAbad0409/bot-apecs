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

        String url = "https://api.openai.com/v1/responses";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", "Eres APECS Bot. Responde corto (máx 30 palabras), amable y con emojis 😊. Si preguntan precios: Un asesor humano te dará la mejor oferta 🤝"
                        ),
                        Map.of(
                                "role", "user",
                                "content", mensajeUsuario
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            Map response = restTemplate.postForObject(url, entity, Map.class);

            List output = (List) response.get("output");
            Map message = (Map) ((List)((Map)output.get(0)).get("content")).get(0);

            return (String) message.get("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error OpenAI: " + e.getMessage();
        }
    }
}
