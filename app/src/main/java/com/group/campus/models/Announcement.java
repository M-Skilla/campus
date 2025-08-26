package com.group.campus.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;
import java.util.List;

public class Announcement implements Parcelable {
    private String body, title, department;

    private Date createdAt;

    private List<String> visibility, imageUrls;

    private Author author;

    public Announcement() {
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Announcement(String body, String title, Date createdAt, List<String> visibility, Author author, String department, List<String> imageUrls) {
        this.body = body;
        this.title = title;
        this.createdAt = createdAt;
        this.visibility = visibility;
        this.author = author;
        this.department = department;
        this.imageUrls = imageUrls;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<String> visibility) {
        this.visibility = visibility;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    // Parcelable implementation
    protected Announcement(Parcel in) {
        body = in.readString();
        title = in.readString();
        department = in.readString();
        long tmpCreatedAt = in.readLong();
        createdAt = tmpCreatedAt != -1 ? new Date(tmpCreatedAt) : null;
        visibility = in.createStringArrayList();
        imageUrls = in.createStringArrayList();

        // Read Author fields individually
        boolean hasAuthor = in.readByte() != 0;
        if (hasAuthor) {
            author = new Author();
            author.setId(in.readString());
            author.setName(in.readString());
            author.setRoles(in.createStringArrayList());
            author.setProfilePictureUrls(in.readString());

            // Read College fields individually
            boolean hasCollege = in.readByte() != 0;
            if (hasCollege) {
                College college = new College();
                college.setId(in.readString());
                college.setName(in.readString());
                college.setAbbrv(in.readString());
                // Note: Programme is not being handled as it would require similar treatment
                author.setCollege(college);
            }
        }
    }

    public static final Creator<Announcement> CREATOR = new Creator<>() {
        @Override
        public Announcement createFromParcel(Parcel in) {
            return new Announcement(in);
        }

        @Override
        public Announcement[] newArray(int size) {
            return new Announcement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(body);
        dest.writeString(title);
        dest.writeString(department);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeStringList(visibility);
        dest.writeStringList(imageUrls);

        if (author != null) {
            dest.writeByte((byte) 1);
            dest.writeString(author.getId());
            dest.writeString(author.getName());
            dest.writeStringList(author.getRoles());
            dest.writeString(author.getProfilePictureUrls());

            College college = author.getCollege();
            if (college != null) {
                dest.writeByte((byte) 1);
                dest.writeString(college.getId());
                dest.writeString(college.getName());
                dest.writeString(college.getAbbrv());

            } else {
                dest.writeByte((byte) 0);
            }
        } else {
            dest.writeByte((byte) 0);
        }
    }
}
