package com.group.campus.utils;

import com.group.campus.models.Announcement;

import java.util.List;

public class AnnouncementDiffCallback extends androidx.recyclerview.widget.DiffUtil.Callback {
    private final List<Announcement> oldList;

    private final List<Announcement> newList;

    public AnnouncementDiffCallback(List<Announcement> oldList, List<Announcement> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        System.out.println(oldList.get(oldItemPosition).getId() != null ? oldList.get(oldItemPosition).getId() : "No ID");
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
