package com.hyphenate.easeui.modules.chat;

import android.text.TextUtils;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.manager.EaseThreadManager;

import java.util.List;

public class EaseChatMessagePresenterImpl extends EaseChatMessagePresenter {

    @Override
    public void joinChatRoom(String username) {
        EMClient.getInstance().chatroomManager().joinChatRoom(username, new EMValueCallBack<EMChatRoom>() {
            @Override
            public void onSuccess(EMChatRoom value) {
                runOnUI(()-> {
                    if(isActive()) {
                        mView.joinChatRoomSuccess(value);
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                runOnUI(() -> {
                    if(isActive()) {
                        mView.joinChatRoomFail(error, errorMsg);
                    }
                });
            }
        });
    }

    @Override
    public void loadLocalMessages(int pageSize) {
        if(conversation == null) {
            throw new NullPointerException("should first set up with conversation");
        }
        List<EMMessage> messages = null;
        try {
            messages = conversation.loadMoreMsgFromDB(null, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(messages == null || messages.isEmpty()) {
            if(isActive()) {
                runOnUI(()->mView.loadNoLocalMsg());
            }
            return;
        }
        if(isActive()) {
            List<EMMessage> finalMessages = messages;
            runOnUI(()->mView.loadLocalMsgSuccess(finalMessages));
        }
    }

    @Override
    public void loadMoreLocalMessages(String msgId, int pageSize) {
        if(conversation == null) {
            throw new NullPointerException("should first set up with conversation");
        }
        if(!isMessageId(msgId)) {
            throw new IllegalArgumentException("please check if set correct msg id");
        }
        List<EMMessage> moreMsgs = null;
        try {
            moreMsgs = conversation.loadMoreMsgFromDB(msgId, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(moreMsgs == null || moreMsgs.isEmpty()) {
            if(isActive()) {
                runOnUI(()->mView.loadNoMoreLocalMsg());
            }
            return;
        }
        if(isActive()) {
            List<EMMessage> finalMoreMsgs = moreMsgs;
            runOnUI(()->mView.loadMoreLocalMsgSuccess(finalMoreMsgs));
        }
    }

    @Override
    public void loadMoreLocalHistoryMessages(String msgId, int pageSize, EMConversation.EMSearchDirection direction) {
        if(conversation == null) {
            throw new NullPointerException("should first set up with conversation");
        }
        if(!isMessageId(msgId)) {
            throw new IllegalArgumentException("please check if set correct msg id");
        }
        EMMessage message = conversation.getMessage(msgId, false);
        List<EMMessage> messages = conversation.searchMsgFromDB(message.getMsgTime() - 1,
                                                                pageSize, direction);
        if(isActive()) {
            runOnUI(()-> {
                if(messages == null || messages.isEmpty()) {
                    mView.loadNoMoreLocalHistoryMsg();
                }else {
                    mView.loadMoreLocalHistoryMsgSuccess(messages, direction);
                }
            });

        }
    }

    @Override
    public void loadServerMessages(int pageSize) {
        if(conversation == null) {
            throw new NullPointerException("should first set up with conversation");
        }
        EMClient.getInstance().chatManager().asyncFetchHistoryMessage(conversation.conversationId(),
                conversation.getType(), pageSize, "",
                new EMValueCallBack<EMCursorResult<EMMessage>>() {
                    @Override
                    public void onSuccess(EMCursorResult<EMMessage> value) {
                        runOnUI(() -> {
                            if(isActive()) {
                                mView.loadServerMsgSuccess(value.getData());
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        runOnUI(() -> {
                            if(isActive()) {
                                mView.loadMsgFail(error, errorMsg);
                                loadLocalMessages(pageSize);
                            }
                        });
                    }
                });
    }

    @Override
    public void loadMoreServerMessages(String msgId, int pageSize) {
        if(conversation == null) {
            throw new NullPointerException("should first set up with conversation");
        }
        if(!isMessageId(msgId)) {
            throw new IllegalArgumentException("please check if set correct msg id");
        }
        EMClient.getInstance().chatManager().asyncFetchHistoryMessage(conversation.conversationId(),
                conversation.getType(), pageSize, msgId,
                new EMValueCallBack<EMCursorResult<EMMessage>>() {
                    @Override
                    public void onSuccess(EMCursorResult<EMMessage> value) {
                        runOnUI(() -> {
                            if(isActive()) {
                                mView.loadMoreServerMsgSuccess(value.getData());
                            }
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        runOnUI(() -> {
                            if(isActive()) {
                                mView.loadMsgFail(error, errorMsg);
                                loadMoreLocalMessages(msgId, pageSize);
                            }
                        });
                    }
                });
    }

    /**
     * 判断是否是消息id
     * @param msgId
     * @return
     */
    public boolean isMessageId(String msgId) {
        if(TextUtils.isEmpty(msgId)) {
            return false;
        }
        EMMessage message = conversation.getMessage(msgId, false);
        return message != null;
    }
}

