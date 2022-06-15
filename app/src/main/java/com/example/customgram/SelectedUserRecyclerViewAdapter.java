package com.example.customgram;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.function.Consumer;

public class SelectedUserRecyclerViewAdapter
        extends RecyclerView.Adapter<SelectedUserRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "USER_RECYCLER";

    private final List<TdApi.User> users;
    private Consumer<Integer> onUserClicked;

    public SelectedUserRecyclerViewAdapter(List<TdApi.User> users) {
        this.users = users;
    }

    @Override
    public SelectedUserRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_user_mini, parent, false);
        return new SelectedUserRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            final SelectedUserRecyclerViewAdapter.ViewHolder holder,
            int position
    ) {
        TdApi.User user = users.get(position);
        if (user == null) return;
        holder.userName.setText(user.firstName);
        setUserPhoto(user, holder);

        holder.parentView.findViewById(R.id.user_button).setOnClickListener(v -> {
            int currentPos = users.indexOf(user);
            if (onUserClicked != null) {
                onUserClicked.accept(currentPos);
            }
        });
    }

    private void setUserPhoto(TdApi.User user, SelectedUserRecyclerViewAdapter.ViewHolder holder) {
        String photoPath = user.profilePhoto == null ? "" : user.profilePhoto.small.local.path;
        ProfilePhotoHelper.setPhoto(
                photoPath,
                user.firstName,
                holder.userPhoto,
                holder.altUserPhotoText
        );
    }

    public void setOnUserClicked(Consumer<Integer> fun) {
        this.onUserClicked = fun;
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View parentView;
        public final TextView userName;
        public final ImageView userPhoto;
        public final TextView altUserPhotoText;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            userName = view.findViewById(R.id.user_name);
            userPhoto = view.findViewById(R.id.user_photo);
            altUserPhotoText = view.findViewById(R.id.alt_user_photo);
        }

        @Override
        public String toString() {
            return userName.getText().toString();
        }
    }
}
