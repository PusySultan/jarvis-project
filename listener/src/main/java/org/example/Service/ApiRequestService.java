package org.example.Service;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class ApiRequestService
{
    private final WebClient webClient;

    public ApiRequestService(WebClient webClient)
    {
        this.webClient = webClient;
    }

    public String getAnswerFromApi(JSONObject jObject)
    {
        try {
            return webClient.post()
                    .uri("http://localhost:8080/api/assistant/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jObject.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
        catch (Exception e)
        {
            System.out.println("Ошибка ответа: " + e.getMessage());
            return "null";
        }
    }
}
