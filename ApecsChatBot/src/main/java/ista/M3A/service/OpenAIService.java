package ista.M3A.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generarRespuesta(String mensajeUsuario) {

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> system = Map.of(
                "role", "system",
                "content",
                "Eres APECS Bot. Responde corto (máx 30 palabras), amable y con emojis 😊. " +
                "Si preguntan precios, di: Un asesor humano te dará la mejor oferta 🤝"
        );

        Map<String, Object> user = Map.of(
                "role", "user",
                "content", mensajeUsuario
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", List.of(system, user));
        body.put("temperature", 0.4);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            Map response = restTemplate.postForObject(url, entity, Map.class);
            List choices = (List) response.get("choices");
            Map message = (Map) ((Map) choices.get(0)).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "⚠️ Ahora no puedo responder, intenta más tarde 🙏";
        }
    }
}
