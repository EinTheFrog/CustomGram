package com.example.customgram;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChatManager {
    private final List<TdApi.Chat> chats = new LinkedList<>();
    private static ChatManager instance;
    private BiConsumer<TdApi.Chat, Integer> onNewChat;
    private Consumer<TdApi.Chat> onRemoveChat;
    private Consumer<TdApi.Chat> onChatPhotoChange;
    private Consumer<TdApi.Chat> onChatLastMessageChange;

    private ChatManager() {
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void addChat(TdApi.Chat chat, int pos) {
        chats.add(pos, chat);
        if (onNewChat != null) {
            onNewChat.accept(chat, pos);
        }
    }

    public void removeChat(TdApi.Chat chat) {
        chats.remove(chat);
        if (onRemoveChat != null) {
            onRemoveChat.accept(chat);
        }
    }

    public void addChatPhoto(TdApi.Chat chat) {
        if (onChatPhotoChange != null) {
            onChatPhotoChange.accept(chat);
        }
    }

    public void addChatLastMessage(TdApi.Chat chat) {
        if (onChatLastMessageChange != null) {
            onChatLastMessageChange.accept(chat);
        }
    }

    public void setOnNewChat(BiConsumer<TdApi.Chat, Integer> fun) {
        onNewChat = fun;
    }

    public void setOnRemoveChat(Consumer<TdApi.Chat> fun) {
        onRemoveChat = fun;
    }


    public List<TdApi.Chat> getChats() {
        return new LinkedList<>(chats);
    }

    public void clearChats() {
        chats.clear();
    }

    public void setOnChatPhotoChange(Consumer<TdApi.Chat> fun) {
        onChatPhotoChange = fun;
    }

    public void setOnChatLastMessageChange(Consumer<TdApi.Chat> fun) {
        onChatLastMessageChange = fun;
    }
}
