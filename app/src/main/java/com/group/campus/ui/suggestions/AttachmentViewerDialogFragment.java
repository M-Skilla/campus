package com.group.campus.ui.suggestions;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.group.campus.R;

/**
 * Full-screen in-app viewer for images and videos.
 */
public class AttachmentViewerDialogFragment extends DialogFragment {

    private static final String ARG_URI = "arg_uri";
    private static final String ARG_MIME = "arg_mime";

    private ImageView iv;
    private VideoView vv;
    private ImageButton close;

    public static AttachmentViewerDialogFragment newInstance(String uriString, String mimeType) {
        AttachmentViewerDialogFragment f = new AttachmentViewerDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_URI, uriString);
        b.putString(ARG_MIME, mimeType);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window w = getDialog().getWindow();
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            setCancelable(true);
        }

        View v;
        try {
            v = inflater.inflate(R.layout.dialog_attachment_viewer, container, false);
            String pkg = requireContext().getPackageName();
            int imgId = getResources().getIdentifier("viewerImage", "id", pkg);
            int vidId = getResources().getIdentifier("viewerVideo", "id", pkg);
            int closeId = getResources().getIdentifier("btnCloseViewer", "id", pkg);
            if (imgId != 0) iv = v.findViewById(imgId);
            if (vidId != 0) vv = v.findViewById(vidId);
            if (closeId != 0) close = v.findViewById(closeId);
        } catch (Throwable inflateError) {
            // Fallback UI (programmatic) if XML inflation fails
            FrameLayout root = new FrameLayout(requireContext());
            root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            root.setBackgroundColor(0xCC000000);

            iv = new ImageView(requireContext());
            iv.setAdjustViewBounds(true);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            FrameLayout.LayoutParams ivLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.addView(iv, ivLp);

            vv = new VideoView(requireContext());
            FrameLayout.LayoutParams vvLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.addView(vv, vvLp);

            close = new ImageButton(requireContext());
            close.setImageResource(R.drawable.ic_close);
            close.setBackgroundColor(Color.TRANSPARENT);
            FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(dp(40), dp(40));
            closeLp.gravity = Gravity.TOP | Gravity.END;
            closeLp.topMargin = dp(16);
            closeLp.rightMargin = dp(16);
            root.addView(close, closeLp);

            v = root;
        }

        // Close handlers
        if (close != null) close.setOnClickListener(view -> dismissAllowingStateLoss());
        v.setOnClickListener(view -> dismissAllowingStateLoss());

        Bundle args = getArguments();
        String uriStr = args != null ? args.getString(ARG_URI) : null;
        String mime = args != null ? args.getString(ARG_MIME) : null;
        Uri uri = uriStr != null ? Uri.parse(uriStr) : null;

        if (uri != null && mime != null) {
            if (mime.startsWith("image")) {
                if (iv != null) iv.setVisibility(View.VISIBLE);
                if (vv != null) vv.setVisibility(View.GONE);
                if (iv != null) Glide.with(requireContext()).load(uri).into(iv);
            } else if (mime.startsWith("video")) {
                if (iv != null) iv.setVisibility(View.GONE);
                if (vv != null) {
                    vv.setVisibility(View.VISIBLE);
                    vv.setVideoURI(uri);
                    MediaController controller = new MediaController(requireContext());
                    controller.setAnchorView(vv);
                    vv.setMediaController(controller);
                    vv.setOnPreparedListener(mp -> {
                        mp.setLooping(false);
                        vv.start();
                    });
                }
            } else {
                dismissAllowingStateLoss();
            }
        } else {
            dismissAllowingStateLoss();
        }

        return v;
    }

    private int dp(int dps) {
        return Math.round(dps * requireContext().getResources().getDisplayMetrics().density);
    }
}
