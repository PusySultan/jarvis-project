package org.example.Service.ImplService.compareService;

import org.example.Service.ApiRequestService;
import org.example.Service.keyWordBank.KeyWordIntegerValue;
import org.example.Service.keyWordBank.KeyWordKVR;
import org.example.models.GroupIndexKVR;
import org.example.models.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("KVR")
public class CompareKVRService implements IComparator
{
    @Autowired
    private KeyWordKVR keyWordKVR;

    @Autowired
    private KeyWordIntegerValue keyWordIntegerValue;


    @Override
    public JSONObject process(String[] usedWords)
    {
        Intent intent = keyWordKVR.getGroupIndex(usedWords);
        Integer kvrNumber = keyWordIntegerValue.getGroupIndex(usedWords);

        if(intent == null)
        {
            System.out.println("Не определена КВР команда");
            return null;
        }

        if(kvrNumber == null)
        {
            System.out.println("Не определен КВР");
            return null;
        }

        return createJsonAnswer(intent, kvrNumber);
    }

    private JSONObject createJsonAnswer(Intent intent, Integer documentId)
    {
        JSONObject request = new JSONObject();
        JSONObject parametrs = new JSONObject();

        try
        {
            parametrs.put("kvr", documentId);

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
