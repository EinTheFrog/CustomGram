package com.example.customgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.drinkless.td.libcore.telegram.TdApi;

public class ProfilePhotoHelper {
    private static String getTitleInitials(String title) {
        String altPhotoText;
        String[] words = title.split(" ");
        switch (words.length) {
            case 0: {
                altPhotoText = "";
                break;
            }
            case 1: {
                String firstWord = words[0];
                altPhotoText = Character.toString(firstWord.charAt(0));
                break;
            }
            default: {
                String firstWord = words[0];
                String lastWord = words[words.length - 1];
                altPhotoText = Character.toString(firstWord.charAt(0)) + lastWord.charAt(0);
            }
        }
        altPhotoText = altPhotoText.toUpperCase();

        return altPhotoText;
    }

    public static void setPhoto(
            String photoPath,
            String altPhotoText,
            ImageView profileImage,
            TextView altPhoto
    ) {
        if (!photoPath.equals("")) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
            profileImage.setImageBitmap(bitmap);
            altPhoto.setText("");
        } else {
            profileImage.setImageDrawable(null);
            Context photoContext = profileImage.getContext();
            profileImage.setBackgroundColor(
                    ContextCompat.getColor(photoContext, R.color.pink)
            );
            altPhoto.setText(
                    ProfilePhotoHelper.getTitleInitials(altPhotoText)
            );
        }
    }
}
