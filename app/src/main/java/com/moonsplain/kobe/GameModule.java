package com.moonsplain.kobe;

public class GameModule extends AccelTestActivity{
    public static Quest[] GAME;

    String[] questStrings =
            {"You wake up hanging from your ankles, | Jiggle yourself free: 500-700ms throw, land face-up | you slip out of the shackles onto the coarse stone floor| you get a good core workout but remain bound",
            "E", "h"};

    for (int i = 0; i < questStrings.length;  i++ )



    public boolean attempt(int level, Throw t){
        switch(level){
            case 1: //first quest conditions
                return (t.a > 500);
            default:
                return false;
        }
    }
    private class Quest{
        String story, reqString, succeedText, failText;

        Quest(String questStr){
            String s[] = questStr.split("|");
            this.story = s[0];
            this.reqString = s[1];
            this.succeedText = s[2];
            this.failText = s[3];
        }
    }
}
