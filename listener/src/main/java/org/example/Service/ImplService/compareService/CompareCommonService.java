package org.example.Service.ImplService.compareService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Service.ApiRequestService;
import org.example.Service.keyWordBank.KeyWordIntegerValue;
import org.example.Service.keyWordBank.KeyWordTopLevel;
import org.example.exception.ApiResponseException;
import org.example.exception.CompareException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CompareCommonService
{
    @Autowired
    private KeyWordTopLevel wordTopLevel;

    @Autowired
    private KeyWordIntegerValue integerValue;

    @Autowired
    private ApiRequestService apiRequestService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private Map<String, IComparator> comparators;

    public String process(String strCMD)
    {
        String[] usedWords = strCMD.split(" ");
        JSONObject jsonObject = null;
        IComparator currentComparator = null;

        if((currentComparator = comparators
                .get(wordTopLevel.getGroupIndex(usedWords).name())) == null)
        {
            throw new CompareException("Ошибка распознавания команды");
        }

        jsonObject = currentComparator.process(usedWords);
        return sendReq(jsonObject);
    }

    private String sendReq(JSONObject jsonObject)
    {
        if(jsonObject == null)
        {
           throw new CompareException("Ошибка при составлении команды");
        }

        String answer = apiRequestService.getAnswerFromApi(jsonObject);

        return parseAnswerPayload(answer);
    }

    private String parseAnswerPayload(String answer)
    {
        try
        {
            JsonNode js = mapper.readTree(answer);
            chekPayload(js);

            return js.get("value").asText();
        }
        catch (JsonProcessingException e)
        {
            return "Ответ сервера вернул ошибку";
        }
    }

    private void chekPayload(JsonNode js)
    {
        if(!js.has("value"))
        {
            throw new ApiResponseException("Сервер не вернул ответа");
        }
    }
}
