package org.example.Service.ImplService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Service.ITranscriptService;
import org.example.exception.AIListenException;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@Service
public class SpeechToTextService implements ITranscriptService
{

    private String modelPath;
    private Model voskModel;
    private StringBuilder cmd;
    private Recognizer recognizer;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init()
    {
        cmd = new StringBuilder();
        loadLLMTranscriptionPath();
        loadLLMModel();
        loadRecognizer();
    }

    @PreDestroy
    public void cleanup()
    {
        // Закрываем модель при остановке приложения
        if (voskModel != null) {
            voskModel.close();
            System.out.println("Vosk model closed.");
        }
    }

    @Override
    public String getCMD()
    {
        String str_cmd = cmd.toString();
        cmd = new StringBuilder();     // сбрасываем для следующей команды
        loadRecognizer();              // пересоздаём recognizer (если нужно)
        return str_cmd;
    }

    @Override
    public boolean isEndCMD(byte[] audioData)
    {
        if (recognizer.acceptWaveForm(audioData, audioData.length))
        {
            cmd.append(extractTextFromJson(recognizer.getResult()));
            return true; // команда окончена
        }

        return false; // продолжаем слушать
    }

    private String extractTextFromJson(String json)
    {
        if (json == null || json.isEmpty()) {
            return "Не распознанная комманда";
        }

        Optional<JsonNode> payload = parsePayload(json);
        if(payload.isEmpty()) return "Не распознанная комманда";
        if(!payload.get().has("text")) return "Не распознанная комманда";

        return payload.get().get("text").asText();
    }

    private Optional<JsonNode> parsePayload(String json)
    {
        try {
            return Optional.of(mapper.readTree(json));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    private void loadLLMModel()
    {
        try {
            this.voskModel = new Model(modelPath);
        } catch (IOException e) {
            throw new AIListenException(e.getMessage() + " ошибка при загрузке");
        }
    }

    private void loadRecognizer()
    {
        try {
            if(recognizer != null)
            {
                recognizer.close();
            }

            recognizer = new Recognizer(voskModel, 16000);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadLLMTranscriptionPath()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("vosk-model-small-ru-0.22"); // "vosk-model-small-ru-0.22"

        if (resourceUrl != null)
        {
            try
            {
                File file = new File(resourceUrl.toURI());
                modelPath = file.getAbsolutePath();
                return;

            } catch (URISyntaxException e) {
                throw new RuntimeException("Ошибка преобразования URL в файл", e);
            }
        }

        throw new RuntimeException("Ресурс vosk-model-small-ru-0.22 не найден");
    }
}