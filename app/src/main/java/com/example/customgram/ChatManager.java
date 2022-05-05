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
    private static final String TAG = "CHAT_MANAGER";

    private final List<TdApi.Chat> chats = new LinkedList<>();
    private final List<TdApi.Message> messages = new ArrayList<>();
    private final Map<Long, TdApi.User> users = new HashMap<>();
    private static ChatManager instance;
    private BiConsumer<TdApi.Chat, Integer> onNewChat;
    private Consumer<TdApi.Chat> onRemoveChat;
    private Consumer<TdApi.Chat> onChatPhotoChange;
    private Consumer<TdApi.Chat> onChatLastMessageChange;
    private Consumer<TdApi.Message> onNewMessage;
    private Consumer<TdApi.User> onNewUser;
    private TdApi.Chat currentChat;

    private ChatManager() {
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void addChat(TdApi.Chat chat, int pos) {
        if (chats.contains(chat)) {
            Log.d(TAG, "Ignoring chat with id: " + chat.id);
            return;
        }
        Log.d(TAG, "Adding chat. Chats size: " + chats.size());
        chats.add(pos, chat);
        if (onNewChat != null) {
            onNewChat.accept(chat, pos);
        }
    }

    public void removeChat(TdApi.Chat chat) {
        if (!chats.contains(chat)) return;
        Log.d(TAG, "Removing chat: Chats size: " + chats.size());
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

    public void addMessage(TdApi.Message message) {
        Log.d(TAG, "Adding message");
        messages.add(message);
        if (onNewMessage != null) {
            onNewMessage.accept(message);
        }
    }

    public void addUser(TdApi.User user) {
        users.put(user.id, user);
        if (onNewUser != null) {
            onNewUser.accept(user);
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

    public void setOnNewMessage(Consumer<TdApi.Message> fun) {
        onNewMessage = fun;
    }

    public List<TdApi.Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessages() {
        messages.clear();
    }

    public void setOnNewUser(Consumer<TdApi.User> fun) {
        onNewUser = fun;
    }

    public Map<Long, TdApi.User> getUsers() {
        return new HashMap<>(users);
    }

    public void clearUsers() {
        users.clear();
    }

    public void setCurrentChat(TdApi.Chat chat) {
        currentChat = chat;
    }

    public TdApi.Chat getCurrentChat() {
        return currentChat;
    }
}
