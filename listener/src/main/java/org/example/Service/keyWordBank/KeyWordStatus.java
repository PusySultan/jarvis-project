package org.example.Service.keyWordBank;

import org.example.models.Intent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KeyWordStatus implements IKeyWordBank
{
    private static final Map.Entry<Intent, List<String>> documentsGroup =
            Map.entry(Intent.get_check_document_status, List.of("документ", "документа", "документов", "документ", "док",
                    "доку", "доки", "докам"));

    private static final Map.Entry<Intent,List<String>> reportGroup =
            Map.entry(Intent.get_check_report_status, List.of("отчет", "отчёт", "отчету", "отчёту", "отчеты", "отчёты",
                    "отчетам", "отчётам", "отчётов", "отчёта", "отчета"));

    private static final List<Map.Entry<Intent,List<String>>> allKeyWords = List.of(documentsGroup, reportGroup);

    @Override
    public Intent getGroupIndex(String[] allWords)
    {
        Intent groupIndexKVR = null;

        float countContains = 0;
        float tempIndex = 0;
        float maxIndex = 0.05f;

        for (Map.Entry<Intent,List<String>> group : allKeyWords)
        {
            countContains = getCountContains(group.getValue(), allWords);

            tempIndex = countContains / group.getValue().size();

            if(tempIndex > maxIndex)
            {
                maxIndex = tempIndex;
                groupIndexKVR = group.getKey();
            }
        }

        return groupIndexKVR;
    }

    private float getCountContains(List<String> group, String[] allWords)
    {
        float temp = 0;

        for (String str : allWords)
        {
            if(group.contains(str))
            {
                temp ++;
            }
        }

        return temp;
    }
}
