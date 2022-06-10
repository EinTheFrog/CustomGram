package com.example.customgram;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.example.customgram.databinding.UserInfoFragmentBinding;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class UserInfoFragment extends Fragment {
    UserInfoFragmentBinding binding;
    private ChatsActivity activity;
    private boolean animationShowed = false;
    int pxActionBarSize = 0;
    int navigationUpButtonSize = 0;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (ChatsActivity) getActivity();

        /*Resources.Theme theme = activity.getApplication().getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(
                com.google.android.material.R.attr.drawerArrowStyle,
                typedValue,
                true
        );
        TypedArray typedArray = activity.obtainStyledAttributes(
                typedValue.data,
                androidx.constraintlayout.widget.R.styleable.DrawerArrowToggle
        );
        navigationUpButtonSize = typedArray.getInt(
                androidx.constraintlayout.widget.R.styleable.DrawerArrowToggle_drawableSize,
                -1
        );*/
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UserInfoFragmentBinding.inflate(getLayoutInflater());

        binding.customToolbar.inflateMenu(R.menu.user_info_options_menu);
        binding.customToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.item_logout) {
                activity.logOut();
                return true;
            }
            return false;
        });

        NavController navController = Navigation.findNavController(
                activity,
                R.id.nav_host_fragment
        );
        AppBarConfiguration.Builder appBarConfBuilder =
                new AppBarConfiguration.Builder(navController.getGraph());
        AppBarConfiguration appBarConfiguration = appBarConfBuilder.build();
        NavigationUI.setupWithNavController(
                binding.customToolbar,
                navController,
                appBarConfiguration
        );
        setHasOptionsMenu(true);

        pxActionBarSize = binding.customToolbar.getLayoutParams().height;

        View navIcon = getToolbarNavigationIcon(binding.customToolbar);
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                navigationUpButtonSize = navIcon.getHeight();
                navIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };
        navIcon.getViewTreeObserver().addOnGlobalLayoutListener(listener);

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int offset = Math.abs(verticalOffset);
            int totalRange = appBarLayout.getTotalScrollRange();
            int toolbarRange = totalRange - pxActionBarSize;
            int animationRange = totalRange - dpToPx(150);
            int imgTopOffset;
            int imgLeftOffset;

            CollapsingToolbarLayout.LayoutParams params = new CollapsingToolbarLayout.LayoutParams(
                    CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                    CollapsingToolbarLayout.LayoutParams.MATCH_PARENT
            );

            if (offset > animationRange) {
                showAnimation();
                imgTopOffset = offset + navigationUpButtonSize;
            } else {
                revertAnimation();
                imgTopOffset = offset + 10;
            }

            if (offset > toolbarRange) {
                // If collapsed, then do this
                imgTopOffset = offset + 10;
                imgLeftOffset = navigationUpButtonSize;
            } else {
                imgLeftOffset = 0;
            }

            params.setMargins(imgLeftOffset, imgTopOffset, 0, 10);
            binding.expandedUserInfo.expandedInfoConstraintLayout.setLayoutParams(params);
        });

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.getCurrentUser().observe(activity, this::setUserInfo);
        chatManager.getSelectedUserFullInfo().observe(activity, this::setUserFullInfo);

        return binding.getRoot();
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return Math.round(px);
    }

    private void showAnimation() {
        if (animationShowed) return;
        animationShowed = true;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(activity, R.layout.toolbar_user_info_fragment);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateInterpolator(1.0f));
        transition.setDuration(100);


        TransitionManager.beginDelayedTransition(
                binding.expandedUserInfo.expandedInfoConstraintLayout,
                transition
        );
        constraintSet.applyTo(binding.expandedUserInfo.expandedInfoConstraintLayout);
    }

    private void revertAnimation() {
        if (!animationShowed) return;
        animationShowed = false;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(activity, R.layout.expanded_toolbar_user_info);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateInterpolator(1.0f));
        transition.setDuration(100);

        TransitionManager.beginDelayedTransition(
                binding.expandedUserInfo.expandedInfoConstraintLayout,
                transition
        );
        constraintSet.applyTo(binding.expandedUserInfo.expandedInfoConstraintLayout);

    }

    private View getToolbarNavigationIcon(Toolbar toolbar){
        //check if contentDescription previously was set
        boolean hadContentDescription = !TextUtils.isEmpty(toolbar.getNavigationContentDescription());
        String contentDescription = hadContentDescription
                ? toolbar.getNavigationContentDescription().toString()
                : "ContentDescription";
        toolbar.setNavigationContentDescription(contentDescription);
        ArrayList<View> potentialViews = new ArrayList<View>();
        //find the view based on it's content description, set programatically or with android:contentDescription
        toolbar.findViewsWithText(
                potentialViews,
                contentDescription,
                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
        );
        //Nav icon is always instantiated at this point because calling setNavigationContentDescription ensures its existence
        View navIcon = null;
        if(potentialViews.size() > 0){
            navIcon = potentialViews.get(0); //navigation icon is ImageButton
        }
        //Clear content description if not previously present
        if(!hadContentDescription)
            toolbar.setNavigationContentDescription(null);
        return navIcon;
    }

    private void setUserInfo(TdApi.User user) {
        if (user == null) return;
        String userFullName = user.firstName + " " + user.lastName;
        binding.expandedUserInfo.toolbarUserName.setText(userFullName);
        binding.userPhoneNumber.setText(user.phoneNumber);
        String userNickname = "@" + user.username;
        binding.userNickname.setText(userNickname);

        String photoPath = user.profilePhoto == null ? "" : user.profilePhoto.small.local.path;
        ProfilePhotoHelper.setPhoto(
                photoPath,
                userFullName,
                binding.expandedUserInfo.toolbarUserImg,
                binding.expandedUserInfo.toolbarAltUserImg
        );
    }

    private void setUserFullInfo(TdApi.UserFullInfo userFullInfo) {
        if (userFullInfo == null) return;
        binding.userBio.setText(userFullInfo.bio);
    }
}
