package org.example.Service.ImplService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.time.Duration;

@Service
public class TextToSpeechService
{
    private final WebClient webClient;

    public TextToSpeechService() {
        // Настраиваем WebClient с увеличенным лимитом памяти (10 МБ)
        this.webClient = WebClient.builder()
                .exchangeStrategies(builder -> builder
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(60))
                ))
                .build();
    }

    public void getSpeech(String text)
    {
        byte[] audioBytes= getResult(text);
        playAudio(audioBytes);
    }

    private byte[] getResult(String text)
    {
        try
        {
            return webClient.get()
                    .uri("http://localhost:9898/process?VOICE=xenia&INPUT_TEXT=" + text)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

        }
        catch (Exception e)
        {
            System.err.println("Ошибка при получении результата: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void playAudio(byte[] audioData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais)) {

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            // Ждём окончания воспроизведения (для тестов)
            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch (Exception e) {
            System.err.println("Ошибка воспроизведения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}