package org.example.Service.ImplService.compareService;

import org.example.Service.keyWordBank.KeyWordIntegerValue;
import org.example.Service.keyWordBank.KeyWordStatus;
import org.example.models.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("STATUS")
public class CompareStatusService implements IComparator
{
    @Autowired
    private KeyWordStatus keyWordStatus;

    @Autowired
    private KeyWordIntegerValue integerValue;

    @Override
    public JSONObject process(String[] usedWords)
    {
        Intent intent = keyWordStatus.getGroupIndex(usedWords);
        Integer document_id = integerValue.getGroupIndex(usedWords);

        if(intent == null)
        {
            System.out.println("Не определена команда статуса");
            return null;
        }

        if(document_id == null)
        {
            System.out.println("Не определен номер документа");
            return null;
        }

        return createJsonAnswer(intent, document_id);
    }

    private JSONObject createJsonAnswer(Intent intent, Integer documentId)
    {
        JSONObject request = new JSONObject();
        JSONObject parametrs = new JSONObject();

        try
        {
            parametrs.put("document_id", documentId);

            request.put("parameters", parametrs);
            request.put("command", intent);

            return request;
        }
        catch (JSONException e)
        {
            return null;
        }
    }
}
