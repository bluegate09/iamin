package idv.tfp10101.iamin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.LongFunction;

import de.hdodenhof.circleimageview.CircleImageView;
import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.message.Message;
import idv.tfp10101.iamin.network.RemoteAccess;

public class MessageFragment extends Fragment {
    private static final String TAG = "TAG_MessageFragment";
    private Activity activity;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView rvMessages;
    private ListenerRegistration registration;
    private EditText etMessage;
    private String token = "";
    private Message message;
    private List<Message> messages;
    private Member member;
    private String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getArguments();
        activity = (AppCompatActivity) getActivity();
        member = (Member) bundle.getSerializable("member");
        message = new Message();
        db = FirebaseFirestore.getInstance();
        messages = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        listenMessages();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(activity));
        etMessage = view.findViewById(R.id.etMessage);

        view.findViewById(R.id.ivSendButton).setOnClickListener(v -> {
            String lastMessage = etMessage.getText().toString().trim();
            if (lastMessage.length() <= 0) {
                Toast.makeText(activity, R.string.textMessageIsInvalid, Toast.LENGTH_SHORT).show();
                return;
            }
            id = db.collection("Messages").document().getId();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            long time = date.getTime();
            String timeStr = sdf.format(time);
            message.setMessage(lastMessage);

            message.setSender(auth.getCurrentUser().getUid());
            message.setToken(member.getFCM_token());
            message.setReceiver(member.getuUId());
            message.setTime(timeStr);
            sendFcm();
            db.collection("Messages").document(id).set(message).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    String result = getString(R.string.textSendMessage);
                    Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
                } else {
                    String result = task1.getException() == null ?
                            getString(R.string.textSendMessageFail) :
                            task1.getException().getMessage();
                    Log.e(TAG, "message: " + result);
                    Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
                }
            });

            etMessage.setText("");
        });
    }

    private void sendFcm() {
        // 發送單一FCM
        String action = "singleFcm";
        if (RemoteAccess.networkConnected(activity)) {
            String url = RemoteAccess.URL_SERVER + "FcmChatServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", action);
            jsonObject.addProperty("title", "+1團購");
            jsonObject.addProperty("body", "Someone sent a message to you!");
            jsonObject.addProperty("data", "Message_Fragment");
            String result = RemoteAccess.getRemoteChatData(url, jsonObject.toString());
            Log.d(TAG, result);
        } else {
            Toast.makeText(activity, R.string.textNoNetwork, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        showAllMessages();
    }

    private void showAllMessages() {
        Log.d(TAG, "showAllMessages");
        db.collection("Messages").orderBy("time", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // TODO: check
                for (QueryDocumentSnapshot document : task.getResult()) {
                    messages.add(document.toObject(Message.class));
                }
                Log.d(TAG, "showAllMessages");
                showMessages(messages);
            } else {
                String result = task.getException() == null ?
                        getString(R.string.textNoMessageFound) : task.getException().getMessage();
                Log.e(TAG, "exception message :" + result);
                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMessages(List<Message> messages) {
        Log.d(TAG, "showMessages() called");
        MessageAdapter messageAdapter = (MessageAdapter) rvMessages.getAdapter();
        if (messageAdapter == null) {
            rvMessages.setAdapter(new MessageAdapter(activity, messages));
        } else {
            messageAdapter.setMessages(messages);
            messageAdapter.notifyDataSetChanged();
            //
            rvMessages.scrollToPosition(messages.size()-1);
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
        Context context;
        List<Message> messages;

        MessageAdapter(Context context, List<Message> messageList) {
            this.context = context;
            this.messages = messageList;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }

        // Receiver
        public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            public ReceivedMessageHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
            }
            void bind(Message message) {
                tvMessage.setText(message.getMessage());
            }
        }

        // Sender
        public class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView tvSenderMessage;
            public SentMessageHolder(@NonNull View itemView) {
                super(itemView);
                tvSenderMessage = itemView.findViewById(R.id.tvSenderMessage);
            }
            void bind(Message message) {
                tvSenderMessage.setText(message.getMessage());
            }
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
//            Log.d(TAG, "VIEWTYPE member id : ======" + member.getId());
//            Log.d(TAG, "VIEWTYPE member uuid : ======" + member.getuUId());
//            Log.d(TAG, "FUser uuid" + auth.getCurrentUser().getUid());
            if (message.getSender().equals(auth.getCurrentUser().getUid())) {
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me, parent, false);
                return new SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other, parent, false);
                return new ReceivedMessageHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "setOnClickListener");
                InputMethodManager imm =(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(holder.itemView.getWindowToken(), 0);
            });
            Message message = messages.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
            }
        }
    }



    private void listenMessages() {
        if (registration == null) {
            registration = db.collection("Messages").orderBy("time", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        Log.d(TAG, "listen message event happened");
                        if (e == null) {
                            List<Message> messages = new ArrayList<>();
                            if (snapshots != null) {
                                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                    Message message = dc.getDocument().toObject(Message.class);
                                    switch (dc.getType()) {
                                        case ADDED:
                                            Log.d(TAG, "Added message" + message.getReceiver());
                                            break;
                                        case MODIFIED:
                                            Log.d(TAG, "MODIFIED message" + message.getReceiver());
                                            break;
                                        case REMOVED:
                                            Log.d(TAG, "REMOVED message" + message.getReceiver());
                                            break;
                                        default:
                                            break;
                                    }
                                }

                                for (DocumentSnapshot document : snapshots.getDocuments()) {
                                    messages.add(document.toObject(Message.class));
                                }
                                Log.d(TAG, "listenMessages");
                                showMessages(messages);
                            }
                        } else {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
            registration = null;
            delete(message);
        }
    }

    private void delete(final Message message) {
        // 刪除Firestore內的資料
        db.collection("Messages").document().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        messages.remove(message);
                        showMessages(messages);
                    } else {
                        Toast.makeText(activity, R.string.textDeleteFail, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}