package org.example.Service.keyWordBank;

import org.example.models.GroupIndexTopLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class KeyWordTopLevel implements IKeyWordBank
{
    private static final Map.Entry<GroupIndexTopLevel,List<String>> kvrGroup =
            Map.entry(GroupIndexTopLevel.KVR, List.of("код", "видов", "вида", "расход", "расходов", "расхода"));

    private static final Map.Entry<GroupIndexTopLevel,List<String>> kosguGroup =
            Map.entry(GroupIndexTopLevel.KOSGU, List.of("классификация", "классификатор", "операций", "операции"));

    private static final Map.Entry<GroupIndexTopLevel,List<String>> statusGroup =
            Map.entry(GroupIndexTopLevel.STATUS, List.of("статус", "статусы", "статусу"));

    private static final List<Map.Entry<GroupIndexTopLevel,List<String>>> allKeyWords = List.of(kvrGroup, kosguGroup, statusGroup);

    @Override
    public GroupIndexTopLevel getGroupIndex(String[] allWords)
    {
        GroupIndexTopLevel groupIndexTopLevel = null;

        float countContains = 0;
        float tempIndex = 0;
        float maxIndex = 0.1f;

        for (Map.Entry<GroupIndexTopLevel,List<String>> group : allKeyWords)
        {
           countContains = getCountContains(group.getValue(), allWords);

           tempIndex = countContains / group.getValue().size();

           if(tempIndex > maxIndex)
           {
               maxIndex = tempIndex;
               groupIndexTopLevel = group.getKey();
           }
        }

        groupIndexTopLevel = groupIndexTopLevel == null ? GroupIndexTopLevel.OTHER : groupIndexTopLevel;
        return groupIndexTopLevel;
    }

    private float getCountContains(List<String> group, String[] allWords)
    {
        float temp = 0;
        List<String> cutWords = new ArrayList<>();

        for (String str : allWords)
        {
            if(group.contains(str))
            {
               temp ++;
               continue;
            }

            cutWords.add(str);
        }

        allWords = cutWords.toArray(String[]::new);
        return temp;
    }
}
