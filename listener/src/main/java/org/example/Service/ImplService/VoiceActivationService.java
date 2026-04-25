package org.example.Service.ImplService;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.Service.ImplService.compareService.CompareCommonService;
import org.example.exception.AIListenException;
import org.example.exception.ApiResponseException;
import org.example.exception.CompareException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class VoiceActivationService
{
    private Porcupine porcupine;
    private AudioFormat format;
    private DataLine.Info info;
    private TargetDataLine microphone;
    private final  int frameLength = 512;
    private final byte[] buffer = new byte[frameLength * 2];        /// буфер для чтения
    private final short[] shortBuffer = new short[frameLength];     /// Конвертированный буфер

    private volatile boolean isRunning = true;

    @Autowired
    private SpeechToTextService vosk;

    @Autowired
    private CompareCommonService compareService;

    @Autowired
    private TextToSpeechService ttsService;

    @Value("${pico.ai.access.key}")
    private String accessKey;

    private String keywordPath;

    @PostConstruct
    public void init()
    {
        loadKeyWordPath();

        initBaseSystem();
        initLLMKeyWord();

        connectMicrophone();

        process();
    }

    private void initLLMKeyWord()
    {
        try
        {
            // Инициализация Porcupine с кастомным словом
            porcupine = new Porcupine.Builder()
                    .setAccessKey(accessKey) // Ваш ключ из консоли
                    .setKeywordPaths(new String[]{keywordPath}) //  Путь к .ppn файлу
                    .build();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadKeyWordPath()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("key_word.ppn");

        if (resourceUrl != null)
        {
            try
            {
                File file = new File(resourceUrl.toURI());
                keywordPath = file.getAbsolutePath();
                return;

            } catch (URISyntaxException e) {
                throw new RuntimeException("Ошибка преобразования URL в файл", e);
            }
        }

        throw new RuntimeException("Ресурс key_word.ppn не найден");
    }

    private void initBaseSystem()
    {
        try
        {
            format = new AudioFormat(16000f, 16, 1, true, false);
            info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
        }
        catch (LineUnavailableException e) {
            System.out.println("Ошибка инициализации микрофона");
        }
    }

    private void process()
    {
       int bytesRead;
       int result;

       while (isRunning)
       {
           bytesRead = microphone.read(buffer, 0, buffer.length);

           if (bytesRead < 0) continue;

           convertReadBufferData();

           // Передаем в Porcupine
           result = getWordPosition();

           if (result >= 0)
           {
               System.out.println("Ключевое слово обнаружено!");
               String cmd = getCMDStrFromVOSK();
               System.out.println(cmd);
               commandProcessor(cmd);
           }
       }
    }

    private void commandProcessor(String strCMD)
    {
        try
        {
            String answer = compareService.process(strCMD);
            System.out.println(answer);

            ttsService.getSpeech(answer);
        }
        catch (ApiResponseException | CompareException e)
        {
            // todo добавить сервис озвучки
            ttsService.getSpeech(e.getMessage());
        }
    }

    private String getCMDStrFromVOSK()
    {
        byte[] buffer = new byte[64];
        boolean commandFinished = false;

        while (!commandFinished)
        {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead <= 0) {
                break;
            }

            commandFinished = vosk.isEndCMD(buffer);
        }
        return vosk.getCMD();
    }

    private int getWordPosition()
    {
       try {
           return porcupine.process(shortBuffer);
       } catch (PorcupineException e) {
           throw new AIListenException(e.getMessage());
       }
    }

    private void convertReadBufferData()
    {
        for (int i = 0; i < frameLength; i++)
        {
            // little-endian: младший байт идет первым
            shortBuffer[i] = (short) ((buffer[2*i] & 0xFF) | (buffer[2*i+1] << 8));
        }
    }

    private void connectMicrophone() {
        try
        {
            microphone.open(format);    // открываем линию с нужным форматом
        }
        catch (LineUnavailableException e)
        {
            throw new RuntimeException(e);
        }
        microphone.start();         // начинаем захват звука
        System.out.println("Слушаю микрофон...");
    }

    @PreDestroy
    public void destroy()
    {
        if (porcupine != null)
        {
            porcupine.delete(); // Освобождаем нативные ресурсы
        }

        if(microphone != null)
        {
            microphone.close();
        }
    }
}
