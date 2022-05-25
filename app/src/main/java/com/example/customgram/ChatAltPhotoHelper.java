package com.example.customgram;

import org.drinkless.td.libcore.telegram.TdApi;

public class ChatAltPhotoHelper {
    public static String getTitleInitials(String title) {
        String altPhotoText;
        String[] words = title.split(" ");
        switch (words.length) {
            case 0: {
                altPhotoText = "";
                break;
            }
            case 1: {
                String firstWord = words[0];
                altPhotoText = Character.toString(firstWord.charAt(0));
                break;
            }
            default: {
                String firstWord = words[0];
                String lastWord = words[words.length - 1];
                altPhotoText = Character.toString(firstWord.charAt(0)) + lastWord.charAt(0);
            }
        }
        altPhotoText = altPhotoText.toUpperCase();

        return altPhotoText;
    }
}
