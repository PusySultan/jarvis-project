package org.example.Service.ImplService.compareService;

import org.example.Service.keyWordBank.KeyWordKOSGU;
import org.example.models.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("KOSGU")
public class CompareKOSGUService implements IComparator
{
    @Autowired
    KeyWordKOSGU keyWordKOSGU;



    @Override
    public JSONObject process(String[] usedWords)
    {
        String kosgu = keyWordKOSGU.getGroupIndex(usedWords).toString();

        return createJsonAnswer(kosgu);
    }

    private JSONObject createJsonAnswer(String kosgu)
    {
        JSONObject request = new JSONObject();
        JSONObject parametrs = new JSONObject();

        try
        {
            parametrs.put("description", kosgu);

            request.put("parameters", parametrs);
            request.put("command", Intent.get_suggest_kosgu);

            return request;
        }
        catch (JSONException e)
        {
            return null;
        }
    }
}
