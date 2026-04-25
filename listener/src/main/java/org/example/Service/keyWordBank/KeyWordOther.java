package org.example.Service.keyWordBank;

import org.example.models.Intent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KeyWordOther implements IKeyWordBank
{
    private static final Map.Entry<Intent, List<String>> deadlineGroup =
            Map.entry(Intent.get_reporting_deadline, List.of("числа", "число", "дата", "даты", "сдать","сдача", "до", "когда", "какого"));

    private static final Map.Entry<Intent,List<String>> instructionGroup =
            Map.entry(Intent.get_reporting_instructions, List.of("как", "какая", "инструкция", "заполнить", "заполнение","заполнению"));

    private static final List<Map.Entry<Intent,List<String>>> allKeyWords = List.of(deadlineGroup, instructionGroup);

    @Override
    public Intent getGroupIndex(String[] allWords)
    {
        Intent groupIndexKVR = null;

        float countContains = 0;
        float tempIndex = 0;
        float maxIndex = 0.1f;

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
