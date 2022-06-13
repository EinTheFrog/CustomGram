package com.example.customgram;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;

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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.shape.CornerFamily;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

public class UserInfoFragment extends Fragment {
    private static final String TAG = "USER_INFO_FRAGMENT";
    private static final int toolbarMargin = 10;

    private final int animationDuration = 200;

    private UserInfoFragmentBinding binding;
    private ChatsActivity activity;
    private boolean animationShowed = false;
    private MarginAnimationState marginAnimationState;
    private int pxActionBarSize = 0;
    private int navigationUpButtonSize = 0;
    private float imageCornersPercentRounding = 0.0f;
    private int imageLeftMargin = 0;
    private int imageTopMargin = 0;
    private float initialTextSize = 0.0f;
    private float toolbarTextSize = 0.0f;
    private float textSize = 0.0f;

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (ChatsActivity) getActivity();

        TextAppearance textAppearance = new TextAppearance(
                activity,
                R.style.TextAppearance_Customgram_Headline1
        );
        initialTextSize = pixelsToSp(activity, textAppearance.getTextSize());
        textAppearance = new TextAppearance(
                activity,
                R.style.TextAppearance_Customgram_Headline6
        );
        toolbarTextSize = pixelsToSp(activity, textAppearance.getTextSize());
        textSize = initialTextSize;
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

            CollapsingToolbarLayout.LayoutParams params = new CollapsingToolbarLayout.LayoutParams(
                    CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                    CollapsingToolbarLayout.LayoutParams.MATCH_PARENT
            );

            if (offset > animationRange) {
                showAnimation();
            } else {
                revertAnimation();
            }

            if (offset > toolbarRange) {
                animateMarginsToToolbar();
            } else if (offset > animationRange) {
                animateMarginsToExpandedToolbar();
            } else {
                animateMarginsToInitial();
            }

            ShapeableImageView imageView = binding.expandedUserInfo.toolbarUserPhoto;
            float absValue = imageView.getHeight() * imageCornersPercentRounding;
            imageView.setShapeAppearanceModel(
                    imageView.getShapeAppearanceModel()
                            .toBuilder()
                            .setAllCorners(CornerFamily.ROUNDED, absValue)
                            .build()
            );

            int additionalMargin =
                    marginAnimationState == MarginAnimationState.ANIMATED_TO_INITIAL
                    ? 0
                    : toolbarMargin;
            params.setMargins(
                    imageLeftMargin + additionalMargin,
                    offset + imageTopMargin + additionalMargin,
                    additionalMargin,
                    additionalMargin
            );
            binding.expandedUserInfo.expandedInfoConstraintLayout.setLayoutParams(params);
            binding.expandedUserInfo.toolbarUserName.setTextSize(textSize);
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

    private void animateImageCornersRounding(
            float from,
            float to,
            int duration
    ) {
        if (from > 1 || to > 1 || from < 0 || to < 0) {
            Log.e(TAG, "Incorrect arguments (to or from)");
        }
        ValueAnimator animation = ValueAnimator.ofFloat(from, to);
        animation.setDuration(duration);
        animation.addUpdateListener(updatedAnimation -> {
            imageCornersPercentRounding = (float) updatedAnimation.getAnimatedValue();
        });
        animation.start();
    }

    private void animateTextSize(
            float from,
            float to,
            int duration
    ) {
        ValueAnimator animation = ValueAnimator.ofFloat(from, to);
        animation.setDuration(duration);
        animation.addUpdateListener(updatedAnimation -> {
            textSize = (float) updatedAnimation.getAnimatedValue();
        });
        animation.start();
    }

    private void showAnimation() {
        if (animationShowed) return;
        animationShowed = true;

        animateImageCornersRounding(0, 0.5f, animationDuration);
        animateTextSize(initialTextSize, toolbarTextSize, animationDuration);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(activity, R.layout.toolbar_user_info_fragment);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateInterpolator(1.0f));
        transition.setDuration(animationDuration);

        TransitionManager.beginDelayedTransition(
                binding.expandedUserInfo.expandedInfoConstraintLayout,
                transition
        );
        constraintSet.applyTo(binding.expandedUserInfo.expandedInfoConstraintLayout);
    }

    private void revertAnimation() {
        if (!animationShowed) return;
        animationShowed = false;

        animateImageCornersRounding(0.5f, 0, animationDuration);
        animateTextSize(toolbarTextSize, initialTextSize, animationDuration);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(activity, R.layout.expanded_toolbar_user_info);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateInterpolator(1.0f));
        transition.setDuration(animationDuration);

        TransitionManager.beginDelayedTransition(
                binding.expandedUserInfo.expandedInfoConstraintLayout,
                transition
        );
        constraintSet.applyTo(binding.expandedUserInfo.expandedInfoConstraintLayout);

    }

    private void animateMarginsToToolbar() {
        if (marginAnimationState == MarginAnimationState.ANIMATED_TO_TOOLBAR) return;
        marginAnimationState = MarginAnimationState.ANIMATED_TO_TOOLBAR;

        ValueAnimator animation = ValueAnimator.ofInt(0, navigationUpButtonSize);
        animation.setDuration(animationDuration);
        animation.addUpdateListener(updatedAnimation -> {
            int value = (int) updatedAnimation.getAnimatedValue();
            imageLeftMargin = value;
            imageTopMargin = navigationUpButtonSize - value;
        });
        animation.start();
    }

    private void animateMarginsToExpandedToolbar() {
        if (marginAnimationState == MarginAnimationState.ANIMATED_TO_EXPANDED_TOOLBAR) return;
        marginAnimationState = MarginAnimationState.ANIMATED_TO_EXPANDED_TOOLBAR;

        ValueAnimator animation = ValueAnimator.ofInt(navigationUpButtonSize, 0);
        animation.setDuration(animationDuration);
        animation.addUpdateListener(updatedAnimation -> {
            int value = (int) updatedAnimation.getAnimatedValue();
            imageLeftMargin = value;
            imageTopMargin = navigationUpButtonSize - value;
        });
        animation.start();
    }

    private void animateMarginsToInitial() {
        if (marginAnimationState == MarginAnimationState.ANIMATED_TO_INITIAL) return;
        marginAnimationState = MarginAnimationState.ANIMATED_TO_INITIAL;

        ValueAnimator animation = ValueAnimator.ofInt(navigationUpButtonSize, 0);
        animation.setDuration(animationDuration);
        animation.addUpdateListener(updatedAnimation -> {
            int value = (int) updatedAnimation.getAnimatedValue();
            imageLeftMargin = value;
            imageTopMargin = 0;
        });
        animation.start();
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
                binding.expandedUserInfo.toolbarUserPhoto,
                binding.expandedUserInfo.toolbarAltUserPhoto
        );
    }

    private void setUserFullInfo(TdApi.UserFullInfo userFullInfo) {
        if (userFullInfo == null) return;
        binding.userBio.setText(userFullInfo.bio);
    }

    private float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px/scaledDensity;
    }

    private enum MarginAnimationState {
        ANIMATED_TO_TOOLBAR, ANIMATED_TO_EXPANDED_TOOLBAR, ANIMATED_TO_INITIAL
    }
}
