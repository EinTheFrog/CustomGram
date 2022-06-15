package com.example.customgram;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChatManager {
    private static final String TAG = "CHAT_MANAGER";

    private static ChatManager instance;

    private final List<TdApi.Chat> chats = new LinkedList<>();
    private final List<TdApi.Message> messages = new ArrayList<>();
    private final List<TdApi.User> users = new ArrayList<>();
    private TdApi.Chat currentChat;
    private final MutableLiveData<TdApi.User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<TdApi.UserFullInfo> selectedUserFullInfo = new MutableLiveData<>();

    private BiConsumer<TdApi.Chat, Integer> onNewChat;
    private Consumer<TdApi.Chat> onRemoveChat;
    private Consumer<TdApi.Chat> onChatPhotoChange;
    private Consumer<TdApi.Chat> onChatLastMessageChange;
    private Consumer<TdApi.Message> onNewMessage;
    private Consumer<TdApi.Message[]> onNewMessages;
    private Consumer<TdApi.Message> onMessageUpdate;
    private final List<Consumer<TdApi.User>> onNewUser = new ArrayList<>();
    private Consumer<TdApi.User> onUserPhotoChange;

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
        messages.add(0, message);
        if (onNewMessage != null) {
            onNewMessage.accept(message);
        }
    }

    public void addMessages(TdApi.Message[] newMessages) {
        for (int i = newMessages.length - 1; i >= 0; i--) {
            messages.add(0, newMessages[i]);
        }
        if (onNewMessages != null) {
            onNewMessages.accept(newMessages);
        }
    }

    public void updateMessage(TdApi.Message message) {
        if (onMessageUpdate != null) {
            onMessageUpdate.accept(message);
        }
    }

    public void addUser(TdApi.User user) {
        users.add(user);
        if (onNewUser != null) {
            for (Consumer<TdApi.User> fun: onNewUser) {
                fun.accept(user);
            }
        }
    }

    public void addUserPhoto(TdApi.User user) {
        if (onUserPhotoChange != null) {
            onUserPhotoChange.accept(user);
            if (user.id == currentChat.id) {
                currentUser.postValue(user);
            }
        }
    }

    public List<TdApi.Chat> getChats() {
        return new LinkedList<>(chats);
    }

    public void clearChats() {
        chats.clear();
    }

    public List<TdApi.Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessages() {
        messages.clear();
    }

    public List<TdApi.User> getUsers() {
        return new ArrayList<>(users);
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

    public void setCurrentUser(TdApi.User user) {
        currentUser.postValue(user);
    }

    public void setSelectedUserFullInfo(TdApi.UserFullInfo userFullInfo) {
        selectedUserFullInfo.postValue(userFullInfo);
    }

    public void setOnNewChat(BiConsumer<TdApi.Chat, Integer> fun) {
        onNewChat = fun;
    }

    public void setOnRemoveChat(Consumer<TdApi.Chat> fun) {
        onRemoveChat = fun;
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

    public void setOnNewMessages(Consumer<TdApi.Message[]> fun) {
        onNewMessages = fun;
    }

    public void setOnMessageUpdate(Consumer<TdApi.Message> fun) {
        onMessageUpdate = fun;
    }

    public void addOnNewUser(Consumer<TdApi.User> fun) {
        onNewUser.add(fun);
    }

    public void removeOnNewUser(Consumer<TdApi.User> fun) {
        onNewUser.remove(fun);
    }

    public void setOnUserPhotoChange(Consumer<TdApi.User> fun) {
        onUserPhotoChange = fun;
    }

    public LiveData<TdApi.User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<TdApi.UserFullInfo> getSelectedUserFullInfo() {
        return selectedUserFullInfo;
    }
}
