package com.example.customgram;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

public class ExampleDummy {
    private static final String TAG = "ExampleDummy";

    private static int chatId = 0;
    private static int userId = 123498;
    private static ChatManager chatManager;

    public static void setChatViewManager(ChatManager manager) {
        chatManager = manager;
    }
    private static int i = 0;
    public static void main() throws InterruptedException {
        while (!Thread.interrupted()) {
            i++;
            if (i % 10_000_000 == 0) {
                Log.d("CHATS_DEBUG", "Adding new chat in ExampleDummy: " + i / 10_000_000);
                chatManager.addChat(createDummyChat(), i / 10_000_000);
            }
        }
        Log.d("CHATS_DEBUG", "End of main in ExampleDummy");
    }

    private static TdApi.Chat createDummyChat() {
        TdApi.Chat chat =  new TdApi.Chat(
                chatId,
                new TdApi.ChatTypePrivate(userId),
                "Title",
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                0,
                0,
                0,
                0,
                null,
                0,
                null,
                null,
                null,
                null,
                0,
                null,
                null
        );
        chatId++;
        return chat;
    }
}
