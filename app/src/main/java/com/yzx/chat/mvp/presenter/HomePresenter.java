package com.yzx.chat.mvp.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.mvp.contract.HomeContract;
import com.yzx.chat.mvp.view.activity.ChatActivity;
import com.yzx.chat.network.chat.ChatManager;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.tool.IMMessageHelper;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.util.AndroidUtil;

import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mHomeView;
    private Handler mHandler;
    private IMClient mIMClient;

    @Override
    public void attachView(HomeContract.View view) {
        mHomeView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.chatManager().addChatMessageUnreadCountChangeListener(mOnChatMessageUnreadCountChangeListener);
        mIMClient.chatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, null);
        mIMClient.contactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
    }

    @Override
    public void detachView() {
        mIMClient.chatManager().removeChatMessageUnreadCountChangeListener(mOnChatMessageUnreadCountChangeListener);
        mIMClient.contactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mHandler.removeCallbacksAndMessages(null);
        mHomeView = null;
        mIMClient = null;
        mHandler = null;
    }


    @Override
    public void loadUnreadCount() {
        mHomeView.updateMessageUnreadBadge(mIMClient.chatManager().getChatUnreadCount());
        mHomeView.updateContactUnreadBadge(mIMClient.contactManager().getContactUnreadCount());
        mIMClient.chatManager().updateChatUnreadCount();
        mIMClient.contactManager().updateContactUnreadCount();
    }

    private final ChatManager.OnChatMessageUnreadCountChangeListener mOnChatMessageUnreadCountChangeListener = new ChatManager.OnChatMessageUnreadCountChangeListener() {
        @Override
        public void onChatMessageUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateMessageUnreadBadge(count);
                }
            });
        }
    };

    private final ContactManager.OnContactOperationUnreadCountChangeListener mOnContactOperationUnreadCountChangeListener = new ContactManager.OnContactOperationUnreadCountChangeListener() {
        @Override
        public void onContactOperationUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateContactUnreadBadge(count);
                }
            });
        }
    };

    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(final Message message, int untreatedCount) {
            if (AndroidUtil.getStackTopActivityClass() == ChatActivity.class && message.getTargetId().equals(ChatPresenter.sConversationID)) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ContactBean contact = IMClient.getInstance().contactManager().getContact(message.getTargetId());
                    if (contact != null) {
                        NotificationHelper.getInstance().showPrivateMessageNotification(message, contact);
                    }
                }
            });
        }
    };

}
