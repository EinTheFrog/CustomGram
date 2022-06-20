package com.example.customgram;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.IOError;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Example class for TDLib usage from Java.
 */
public final class Example {
    private static final String TAG = "EXAMPLE_CLASS";

    private static int apiId;
    private static String apiHash;
    private static String systemLanguageCode;
    private static String authenticationCode;

    private static ChatManager chatManager;

    private static final ProfilePhotoHandler profilePhotoHandler = new ProfilePhotoHandler();
    private static final ChatHistoryHandler chatHistoryHandler = new ChatHistoryHandler();
    private static final SentMessageHandler sentMessageHandler = new SentMessageHandler();
    private static final Client.ResultHandler defaultHandler = new DefaultHandler();
    private static final MessagePhotoHandler messagePhotoHandler = new MessagePhotoHandler();
    private static final GetMeHandler getMeHandler = new GetMeHandler();
    private static final UserFullInfoHandler userFullInfoHandler = new UserFullInfoHandler();

    private static volatile boolean havePhoneNumber = false;
    private static final Lock phoneNumberLock = new ReentrantLock();
    private static final Condition gotPhoneNumber = phoneNumberLock.newCondition();

    private static long currentChatId = 0;

    private static Client client = null;
    private static String databaseDirectory = "tdlib";
    private static final String PHONE_PREFIX = "999662";
    private static String phoneNumber = PHONE_PREFIX + "1111";

    public static MutableLiveData<TdApi.AuthorizationState> authorizationStateData
            = new MutableLiveData<>();
    private static volatile boolean needQuit = false;

    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, Long> photoIdsToChatIds = new ConcurrentHashMap<>();

    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<>();
    private static boolean haveFullMainChatList = false;

    private static final List<TdApi.Message> currentMessages = new ArrayList<>();
    private static final ConcurrentHashMap<Integer, TdApi.Message> photoIdsToMessages
            = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Long, TdApi.User> users
            = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, Long> photoIdsToUserIds
            = new ConcurrentHashMap<>();

    private static final String newLine = System.getProperty("line.separator");

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public static String getPhotosDir() {
        return databaseDirectory + "/photos";
    }

    public static void setChatViewManager(ChatManager manager) {
        chatManager = manager;
    }

    public static List<TdApi.Chat> getChats() {
        List<TdApi.Chat> list = new ArrayList<>();
        for (OrderedChat orderedChat: mainChatList) {
            list.add(chats.get(orderedChat.chatId));
        }
        return list;
    }

    public static void setPhoneNumber(String number) {
        phoneNumber = PHONE_PREFIX + number;
    }

    public static void enablePhoneNumber() {
        havePhoneNumber = true;
        phoneNumberLock.lock();
        try {
            gotPhoneNumber.signal();
        } finally {
            phoneNumberLock.unlock();
        }
    }

    private static void disablePhoneNumber() {
        havePhoneNumber = false;
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState == null) return;
        Log.d(TAG, "Posting new state value: " + authorizationState.getClass());
        authorizationStateData.postValue(authorizationState);
        Log.d(TAG, "Posted new state value");

        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                Log.d(TAG, "Sending tdlib params");
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = databaseDirectory;
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.apiId = apiId;
                parameters.apiHash = apiHash;
                parameters.systemLanguageCode = systemLanguageCode;
                parameters.deviceModel = getDeviceName();
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;
                parameters.useTestDc = true;
                parameters.systemVersion = Build.VERSION.CODENAME;

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                Log.d(TAG, "Waiting for phone number");
                phoneNumberLock.lock();
                try {
                    while (!havePhoneNumber) {
                        gotPhoneNumber.await();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
                finally {
                    phoneNumberLock.unlock();
                }
                client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                disablePhoneNumber();
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                client.send(new TdApi.CheckAuthenticationCode(authenticationCode), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                client.send(new TdApi.LogOut(), defaultHandler);
                break;
            }
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                Log.e(TAG , "Closed");
                if (!needQuit) {
                    client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                }
                break;
            default:
                Log.e(TAG, "Unsupported authorization state:" + newLine + authorizationState);
        }
    }

    public static void executeGetChats(int limit) {
        getMainChatList(limit);
    }

    public static void executeGetChatHistory(long id) {
        currentChatId = id;
        getChatHistory(0);
    }

    public static void executeSendMessage(String text) {
        if (text.equals("")) return;
        TdApi.FormattedText formattedText = new TdApi.FormattedText(text, null);
        TdApi.InputMessageText messageText = new TdApi.InputMessageText(
                formattedText, false, true
        );
        client.send(
                new TdApi.SendMessage(
                        currentChatId,
                        0,
                        0,
                        null,
                        null,
                        messageText),
                sentMessageHandler
        );
    }

    public static void executeSendMessage(String text, String photoPath) {
        TdApi.FormattedText formattedText = new TdApi.FormattedText(text, null);
        TdApi.InputMessagePhoto messagePhoto = new TdApi.InputMessagePhoto(
                new TdApi.InputFileLocal(photoPath), null, new int[0],
                200, 200, formattedText, 0
        );
        client.send(
                new TdApi.SendMessage(
                        currentChatId,
                        0,
                        0,
                        null,
                        null,
                        messagePhoto),
                sentMessageHandler
        );
    }

    public static void executeGetMe() {
        client.send(new TdApi.GetMe(), getMeHandler);
    }

    public static void executeGetUserFullInfo(TdApi.User user) {
        client.send(new TdApi.GetUserFullInfo(user.id), userFullInfoHandler);
    }

    public static void executeGetContacts() {
        client.send(new TdApi.GetContacts(), new UsersHandler());
    }

    public static void executeCreateGroup(long[] userIds, String title, String photoPath) {
        client.send(new TdApi.CreateNewBasicGroupChat(userIds, title), new NewGroupHandler(photoPath));
    }

    public static void executeLogOut() {
        chats.clear();
        mainChatList.clear();
        currentMessages.clear();
        haveFullMainChatList = false;
        client.send(new TdApi.LogOut(), defaultHandler);
    }

    public static void executeQuit() {
        needQuit = true;
        client.send(new TdApi.Close(), defaultHandler);
    }

    public static void main(String databaseDirectory, String logFileName, int apiId,
                            String apiHash, String systemLanguageCode, String authenticationCode
    ) throws InterruptedException {
        if (client != null) return;
        Example.databaseDirectory = databaseDirectory;
        Example.apiId = apiId;
        Example.apiHash = apiHash;
        Example.systemLanguageCode = systemLanguageCode;
        Example.authenticationCode = authenticationCode;

        Client.execute(new TdApi.SetLogVerbosityLevel(2));

        TdApi.Object result = Client.execute(new TdApi.SetLogStream(
                new TdApi.LogStreamFile(
                        databaseDirectory + logFileName,
                        1 << 27,
                        true
                )
        ));
        if (result instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        // create client
        client = Client.create(new UpdateHandler(), null, null);

        // test Client.execute
        //defaultHandler.onResult(Client.execute(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));
    }

    public static void clearMessages() {
        currentMessages.clear();
    }

    private static void getChatHistory(long fromMessageId) {
        client.send(
                new TdApi.GetChatHistory(
                        currentChatId, fromMessageId, 0, 20, false
                ),
                chatHistoryHandler
        );
    }

    private static void getMainChatList(final int limit) {
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                int chatsToLoadLeft = limit - mainChatList.size();
                client.send(
                        new TdApi.LoadChats(new TdApi.ChatListMain(), chatsToLoadLeft),
                        new ChatHandler(limit)
                );
            }
        }
    }

    private static void setChatPositions(TdApi.Chat chat, TdApi.ChatPosition[] positions) {
        synchronized (mainChatList) {
            synchronized (chat) {
                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isRemoved = mainChatList.remove(new OrderedChat(chat.id, position));
                        chatManager.removeChat(chat);
                        assert isRemoved;
                    }
                }

                chat.positions = positions;

                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isAdded = mainChatList.add(new OrderedChat(chat.id, position));
                        int chatPos = getChatPosition(chat.id, mainChatList);
                        chatManager.addChat(chat, chatPos);
                        assert isAdded;
                    }
                }
            }
        }
    }

    private static int getChatPosition(Long chatId, NavigableSet<OrderedChat> set) {
        int pos = 0;
        for(OrderedChat orderedChat: set) {
            if (chatId == orderedChat.chatId) return pos;
            pos++;
        }
        throw new IllegalArgumentException();
    }

    private static void addChat(TdApi.Chat chat) {
        chats.put(chat.id, chat);

        TdApi.ChatPosition[] positions = chat.positions.clone();
        chat.positions = new TdApi.ChatPosition[0];
        setChatPositions(chat, positions);

        boolean chatHasPhoto = chat.photo != null;
        if (!chatHasPhoto) return;
        photoIdsToChatIds.put(chat.photo.small.id, chat.id);
        if (chat.photo.small.local.path.equals("")) {
            downloadFile(chat.photo.small.id, profilePhotoHandler);
        }
    }

    private static void downloadFile(int id, Client.ResultHandler handler) {
        client.send(
                new TdApi.DownloadFile(id, 31, 0, 0, true),
                handler
        );
    }

    private static void saveUser(TdApi.User user) {
        users.put(user.id, user);
        chatManager.addUser(user);
        if (user.profilePhoto == null) return;
        photoIdsToUserIds.put(user.profilePhoto.small.id, user.id);
        downloadFile(user.profilePhoto.small.id, profilePhotoHandler);
    }

    private static class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            Log.i(TAG, object.toString());
        }
    }

    private static class GetMeHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() != TdApi.User.CONSTRUCTOR) {
                Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
                return;
            }
            TdApi.User user = (TdApi.User) object;
            chatManager.setCurrentUser(user);
            saveUser(user);
        }
    }

    private static class UserFullInfoHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() != TdApi.UserFullInfo.CONSTRUCTOR) {
                Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
                return;
            }
            TdApi.UserFullInfo userFullInfo = (TdApi.UserFullInfo) object;
            chatManager.setSelectedUserFullInfo(userFullInfo);
        }
    }

    private static class  UsersHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() != TdApi.Users.CONSTRUCTOR) {
                Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
                return;
            }
            TdApi.Users users = (TdApi.Users) object;
            for (long userId: users.userIds) {
                client.send(new TdApi.GetUser(userId), new UserHandler());
            }
        }
    }

    private static class UserHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.User.CONSTRUCTOR: {
                    TdApi.User user = (TdApi.User) object;
                    saveUser(user);
                    break;
                }
                default:
                    Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class NewGroupHandler implements Client.ResultHandler {
        String chatPhotoPath;
        public NewGroupHandler(String chatPhotoPath) {
            this.chatPhotoPath = chatPhotoPath;
        }

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() != TdApi.Chat.CONSTRUCTOR) {
                Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
                return;
            }
            TdApi.Chat newChat = (TdApi.Chat) object;
            addChat(newChat);

            if (chatPhotoPath == null) return;
            TdApi.InputFile chatPhotoFile = new TdApi.InputFileLocal(chatPhotoPath);
            TdApi.InputChatPhotoStatic chatPhoto = new TdApi.InputChatPhotoStatic(chatPhotoFile);
            client.send(new TdApi.SetChatPhoto(newChat.id, chatPhoto), defaultHandler);
        }
    }

    private static class SentMessageHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Message.CONSTRUCTOR: {
/*                    TdApi.Message message = (TdApi.Message) object;
                    synchronized (currentMessages) {
                        currentMessages.add(0, message);
                        handleMessageContent(message);
                        chatManager.addMessage(message);
                    }*/
                    break;
                }
                default:
                    Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static void handleMessageContent(TdApi.Message message) {
        switch (message.content.getConstructor()) {
            case TdApi.MessagePhoto.CONSTRUCTOR: {
                TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) message.content;
                int size = messagePhoto.photo.sizes.length - 1;
                TdApi.File photo = messagePhoto.photo.sizes[size].photo;
                photoIdsToMessages.put(photo.id, message);
                downloadFile(photo.id, messagePhotoHandler);
                break;
            }
            default:
                Log.e(TAG, "Can't handle such content:" + newLine + message.content);
        }
    }

    private static class ChatHistoryHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Messages.CONSTRUCTOR: {
                    TdApi.Messages messagesObject = (TdApi.Messages) object;
                    int size = messagesObject.messages.length;
                    for (int i = 0; i < size; i++) {
                        TdApi.Message message = messagesObject.messages[i];
                        currentMessages.add(message);

                        if (message.senderId.getConstructor() == TdApi.MessageSenderUser.CONSTRUCTOR) {
                            TdApi.MessageSenderUser sender
                                    = ((TdApi.MessageSenderUser) message.senderId);
                            if (!users.containsKey(sender.userId)) {
                                client.send(new TdApi.GetUser(sender.userId), new UserHandler());
                            }
                        }

                        handleMessageContent(message);
                    }
                    if (currentMessages.size() < 20 && size > 0) {
                        long lastMessageId = currentMessages.get(currentMessages.size() - 1).id;
                        getChatHistory(lastMessageId);
                    } else {
                        TdApi.Message[] messages = new TdApi.Message[currentMessages.size()];
                        currentMessages.toArray(messages);
                        chatManager.addMessages(messages);
                    }
                    break;
                }
                default:
                    Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class ProfilePhotoHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.File.CONSTRUCTOR: {
                    TdApi.File photo = (TdApi.File) object;
                    if (photo.local.isDownloadingActive) break;
                    if (photoIdsToChatIds.containsKey(photo.id)) {
                        Long chatId = photoIdsToChatIds.get(photo.id);
                        TdApi.Chat chatWithPhoto = chats.get(chatId);
                        chatWithPhoto.photo.small.local.path = photo.local.path;
                        chatManager.addChatPhoto(chatWithPhoto);
                    } else if (photoIdsToUserIds.containsKey(photo.id)) {
                        Long userId = photoIdsToUserIds.get(photo.id);
                        TdApi.User userWithPhoto = users.get(userId);
                        userWithPhoto.profilePhoto.small.local.path = photo.local.path;
                        chatManager.addUserPhoto(userWithPhoto);
                    }

                    break;
                }
                default:
                    Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class MessagePhotoHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.File.CONSTRUCTOR: {
                    TdApi.File photo = (TdApi.File) object;
                    if (photo.local.isDownloadingActive) break;
                    if (!photoIdsToMessages.containsKey(photo.id)) break;
                    TdApi.Message message = photoIdsToMessages.get(photo.id);
                    TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) message.content;
                    int size = messagePhoto.photo.sizes.length - 1;
                    messagePhoto.photo.sizes[size].photo.local.path = photo.local.path;
                    chatManager.updateMessage(message);
                    break;
                }
                default:
                    Log.e(TAG, "Received wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class ChatHandler implements Client.ResultHandler {
        final int limit;

        public ChatHandler(int limit) {
            this.limit = limit;
        }

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    if (((TdApi.Error) object).code == 404) {
                        synchronized (mainChatList) {
                            haveFullMainChatList = true;
                        }
                    } else {
                        Log.e(TAG,
                              "Receive an error for LoadChats:" + newLine + object
                        );
                    }
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // chats had already been received through updates, let's retry request
                    getMainChatList(limit);
                    break;
                default:
                    Log.e(TAG,
                          "Received wrong response from TDLib:" + newLine + object
                    );
            }
        }
    }

    private static class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;
                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                    TdApi.Chat chat = updateNewChat.chat;
                    synchronized (chat) {
                        addChat(chat);
                    }
                    break;
                }
                case TdApi.UpdateSecretChat.CONSTRUCTOR: {
                    Log.d(TAG, "Received secret chat");
                    break;
                }
                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatLastMessage updateChatLastMessage
                            = (TdApi.UpdateChatLastMessage) object;
                    TdApi.Chat chat = chats.get(updateChatLastMessage.chatId);
                    synchronized (chat) {
                        chat.lastMessage = updateChatLastMessage.lastMessage;
                        setChatPositions(chat, updateChatLastMessage.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChatDraftMessage
                            = (TdApi.UpdateChatDraftMessage) object;
                    TdApi.Chat chat = chats.get(updateChatDraftMessage.chatId);
                    synchronized (chat) {
                        setChatPositions(chat, updateChatDraftMessage.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                    TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
                    if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                        break;
                    }

                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        List<TdApi.ChatPosition> positions = new ArrayList<>();
                        if (updateChat.position.order != 0) {
                            positions.add(updateChat.position);
                        }
                        for (int i = 0; i < chat.positions.length; i++) {
                            if (chat.positions[i].list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                                positions.add(chat.positions[i]);
                            }
                        }
                        TdApi.ChatPosition[] positionsArray = new TdApi.ChatPosition[positions.size()];
                        positions.toArray(positionsArray);
                        setChatPositions(chat, positionsArray);
                    }
                    break;
                }
                case TdApi.UpdateFile.CONSTRUCTOR: {
/*                    TdApi.File photo = (TdApi.File) object;
                    if (photo.local.isDownloadingActive) break;
                    if (!photoRemoteIdsToChatIds.containsKey(photo.id)) break;
                    Long chatId = photoRemoteIdsToChatIds.get(photo.id);
                    TdApi.Chat chatWithPhoto = chats.get(chatId);
                    chatWithPhoto.photo.small.local.path = photo.local.path;
                    chatManager.addChatPhoto(chatWithPhoto);*/
                    break;
                }
                case TdApi.UpdateNewMessage.CONSTRUCTOR:
                    TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;
                    TdApi.Message message = updateNewMessage.message;
                    synchronized (currentMessages) {
                        //if (currentMessages.contains(message)) break;
                        currentMessages.add(0, message);
                        handleMessageContent(message);
                        chatManager.addMessage(message);
                    }
                    break;
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChatPhoto = (TdApi.UpdateChatPhoto) object;
                    break;
                }
                default:
                    Log.e(TAG,"Unsupported update:" + newLine + object);
            }
        }
    }

    private static class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error:" + newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    System.err.println("Received wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class OrderedChat implements Comparable<OrderedChat> {
        final long chatId;
        final TdApi.ChatPosition position;

        OrderedChat(long chatId, TdApi.ChatPosition position) {
            this.chatId = chatId;
            this.position = position;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.position.order != o.position.order) {
                return o.position.order < this.position.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }
    }
}
