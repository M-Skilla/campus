package com.group.campus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.group.campus.models.Announcement;
import com.group.campus.models.Author;
import com.group.campus.utils.HtmlRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnnouncementViewActivity extends AppCompatActivity {

    private Announcement announcement;

    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView departmentAvatar, announcementImage;
    private TextView departmentName, announcementDate, announcementTitle, announcementContent;
    private TextView viewCount, readTime, contactEmail, contactPhone;
    private MaterialButton shareButton;
    private ExtendedFloatingActionButton fabMoreActions;
    private MaterialCardView imageCard, contactCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_announcement_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the announcement data from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("announcement")) {
            announcement = intent.getParcelableExtra("announcement");
        }

        initializeViews();
        setupToolbar();
        displayAnnouncementData();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        departmentAvatar = findViewById(R.id.imageView_department_avatar);
        announcementImage = findViewById(R.id.imageView_announcement_image);
        departmentName = findViewById(R.id.textView_announcement_department);
        announcementDate = findViewById(R.id.textView_announcement_date);
        announcementTitle = findViewById(R.id.textView_announcement_title);
        announcementContent = findViewById(R.id.textView_announcement_content);
        viewCount = findViewById(R.id.textView_view_count);
        readTime = findViewById(R.id.textView_read_time);
        contactEmail = findViewById(R.id.textView_contact_email);
        contactPhone = findViewById(R.id.textView_contact_phone);

        shareButton = findViewById(R.id.button_share);

        fabMoreActions = findViewById(R.id.fab_more_actions);
        imageCard = findViewById(R.id.image_card);
        contactCard = findViewById(R.id.contact_card);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayAnnouncementData() {
        if (announcement == null) {
            Toast.makeText(this, "No announcement data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set announcement title and content
        announcementTitle.setText(announcement.getTitle() != null ? announcement.getTitle() : "No Title");

        // Render HTML content properly with ordered list support using HtmlRenderer
        String bodyContent = announcement.getBody();
        if (bodyContent != null && bodyContent.contains("<")) {
            // Content contains HTML, render it properly with ordered list support
            announcementContent.setText(HtmlRenderer.fromHtml(bodyContent));
        } else {
            // Plain text content
            announcementContent.setText(bodyContent != null ? bodyContent : "No content available");
        }

        // Set department name
        departmentName.setText(announcement.getDepartment() != null ? announcement.getDepartment() : "Unknown Department");

        // Set formatted date
        if (announcement.getCreatedAt() != null) {
            String formattedDate = formatDate(announcement.getCreatedAt());
            announcementDate.setText("Posted " + formattedDate);
        } else {
            announcementDate.setText("Date not available");
        }


        // Set engagement stats (placeholder values for now)
        viewCount.setText("156 views");
        readTime.setText(calculateReadTime(announcement.getBody()) + " min read");

        // Handle images
        handleImages();

        // Set contact information
        setContactInformation();
    }


    private void handleImages() {
        // Handle department avatar
        Author author = announcement.getAuthor();
        if (author != null && author.getProfilePicUrl() != null && !author.getProfilePicUrl().isEmpty()) {
            Glide.with(this)
                    .load(author.getProfilePicUrl())
                    .placeholder(R.drawable.profile_image)
                    .into(departmentAvatar);
        } else {
            departmentAvatar.setImageResource(R.drawable.profile_image);
        }

        // Handle announcement images
        if (announcement.getImageUrls() != null && !announcement.getImageUrls().isEmpty()) {
            imageCard.setVisibility(View.VISIBLE);
            String firstImageUrl = announcement.getImageUrls().get(0);
            Glide.with(this)
                    .load(firstImageUrl)
                    .placeholder(R.drawable.udom_logo)
                    .into(announcementImage);
        } else {
            imageCard.setVisibility(View.GONE);
        }
    }

    private void setContactInformation() {
        // Set contact information based on author or department
        Author author = announcement.getAuthor();
        if (author != null) {
            // Since Author class doesn't have email field, use placeholder or derive from other fields
            contactEmail.setText("Email not available");

            // Set phone number (if available in author model)
            contactPhone.setText("+255 123 456 789"); // Placeholder
        } else {
            contactEmail.setText("contact@udom.ac.tz");
            contactPhone.setText("+255 123 456 789");
        }
    }

    private void setupClickListeners() {
        shareButton.setOnClickListener(v -> shareAnnouncement());

        fabMoreActions.setOnClickListener(v -> showMoreActions());
    }

    private void shareAnnouncement() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Handle null author gracefully
        String authorName = "Unknown Author";
        String collegeName = "UDOM";
        String collegeAbbrv = "";

        if (announcement.getAuthor() != null) {
            authorName = announcement.getAuthor().getName() != null ? announcement.getAuthor().getName() : "Unknown Author";
            if (announcement.getAuthor().getCollege() != null) {
                collegeName = announcement.getAuthor().getCollege().getName() != null ?
                        announcement.getAuthor().getCollege().getName() : "UDOM";
                collegeAbbrv = announcement.getAuthor().getCollege().getAbbrv() != null ?
                        " (" + announcement.getAuthor().getCollege().getAbbrv() + ")" : "";
            }
        }

        // Use HtmlRenderer to convert HTML content to properly formatted plain text
        String bodyText = HtmlRenderer.toPlainText(announcement.getBody());

        String shareText = "🇹🇿🇹🇿🇹🇿🇹🇿🇹🇿🇹🇿🇹🇿🇹🇿🇹🇿🇹🇿" + "\n" +
                announcement.getDepartment() + "\n" +
                formatDate(announcement.getCreatedAt())
                + "\n\n" + announcement.getTitle() +
                "\n\n" + bodyText +
                "\n\n" + "Authored By: " + authorName + "\n" + "College: " + collegeName + collegeAbbrv;

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, announcement.getTitle());
        startActivity(Intent.createChooser(shareIntent, "Share Announcement"));
    }

    private void bookmarkAnnouncement() {
        // Implement bookmark functionality
        Toast.makeText(this, "Announcement bookmarked!", Toast.LENGTH_SHORT).show();
    }

    private void showMoreActions() {
        // Implement more actions functionality
        Toast.makeText(this, "More actions coming soon!", Toast.LENGTH_SHORT).show();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private int calculateReadTime(String content) {
        if (content == null || content.isEmpty()) return 1;
        int wordCount = content.split("\\s+").length;
        int readTime = (int) Math.ceil(wordCount / 200.0); // Assuming 200 words per minute
        return Math.max(1, readTime);
    }
}