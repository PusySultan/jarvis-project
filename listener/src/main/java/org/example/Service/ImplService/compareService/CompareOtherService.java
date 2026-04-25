package org.example.Service.ImplService.compareService;

import org.example.Service.keyWordBank.KeyWordIntegerValue;
import org.example.Service.keyWordBank.KeyWordOther;
import org.example.models.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("OTHER")
public class CompareOtherService implements IComparator
{
    @Autowired
    private KeyWordOther keyWordOther;

    @Autowired
    private KeyWordIntegerValue keyWordIntegerValue;

    @Override
    public JSONObject process(String[] usedWords)
    {
        Intent intent = keyWordOther.getGroupIndex(usedWords);
        Integer report_type = keyWordIntegerValue.getGroupIndex(usedWords);

        if(intent == null)
        {
            System.out.println("Не определена команда в OTHER");
            return null;
        }

        if(report_type == null)
        {
            System.out.println("Не определен номер документа");
            return null;
        }

        return createJsonAnswer(intent, report_type);
    }

    private JSONObject createJsonAnswer(Intent intent, Integer report_type)
    {
        JSONObject request = new JSONObject();
        JSONObject parametrs = new JSONObject();

        try
        {
            parametrs.put("report_type", report_type);

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
