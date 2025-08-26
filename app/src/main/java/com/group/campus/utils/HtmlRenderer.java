package com.group.campus.utils;

import android.text.Html;
import android.text.Spanned;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.os.Build;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlRenderer {

    /**
     * Converts HTML content to Spanned text with proper ordered list support
     */
    public static Spanned fromHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return new SpannableStringBuilder("");
        }

        // Convert ordered lists to numbered format before processing
        String processedHtml = processOrderedLists(htmlContent);
        // Process unordered lists to add proper indentation
        processedHtml = processUnorderedLists(processedHtml);

        // Use Android's Html.fromHtml() for the rest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(processedHtml, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(processedHtml);
        }
    }

    /**
     * Processes ordered lists and converts them to numbered format with proper indentation
     */
    private static String processOrderedLists(String html) {
        // Pattern to match ordered lists
        Pattern olPattern = Pattern.compile("<ol[^>]*>(.*?)</ol>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher olMatcher = olPattern.matcher(html);

        StringBuffer result = new StringBuffer();

        while (olMatcher.find()) {
            String listContent = olMatcher.group(1);
            String numberedList = convertToNumberedList(listContent);
            olMatcher.appendReplacement(result, numberedList);
        }
        olMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Processes unordered lists to add proper indentation
     */
    private static String processUnorderedLists(String html) {
        // Pattern to match unordered lists
        Pattern ulPattern = Pattern.compile("<ul[^>]*>(.*?)</ul>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher ulMatcher = ulPattern.matcher(html);

        StringBuffer result = new StringBuffer();

        while (ulMatcher.find()) {
            String listContent = ulMatcher.group(1);
            String bulletList = convertToBulletList(listContent);
            ulMatcher.appendReplacement(result, bulletList);
        }
        ulMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Converts list items in ordered list to numbered format with proper indentation
     */
    private static String convertToNumberedList(String listContent) {
        // Pattern to match list items
        Pattern liPattern = Pattern.compile("<li[^>]*>(.*?)</li>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher liMatcher = liPattern.matcher(listContent);

        StringBuilder numberedList = new StringBuilder();
        int counter = 1;

        while (liMatcher.find()) {
            String itemContent = liMatcher.group(1).trim();
            // Add proper indentation with margin for numbered lists
            numberedList.append("<p style=\"margin-left: 20px; text-indent: -20px;\">")
                       .append(counter).append(". ").append(itemContent).append("</p>");
            counter++;
        }

        return numberedList.toString();
    }

    /**
     * Converts list items in unordered list to bullet format with proper indentation
     */
    private static String convertToBulletList(String listContent) {
        // Pattern to match list items
        Pattern liPattern = Pattern.compile("<li[^>]*>(.*?)</li>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher liMatcher = liPattern.matcher(listContent);

        StringBuilder bulletList = new StringBuilder();

        while (liMatcher.find()) {
            String itemContent = liMatcher.group(1).trim();
            // Add proper indentation with margin for bullet lists
            bulletList.append("<p style=\"margin-left: 20px; text-indent: -20px;\">")
                      .append("• ").append(itemContent).append("</p>");
        }

        return bulletList.toString();
    }

    /**
     * Converts HTML content to plain text for sharing purposes
     */
    public static String toPlainText(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        // Convert ordered lists to numbered format
        String processedHtml = processOrderedListsForPlainText(htmlContent);
        // Convert unordered lists to bullet format
        processedHtml = processUnorderedListsForPlainText(processedHtml);

        // Remove HTML tags and convert to plain text
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(processedHtml, Html.FROM_HTML_MODE_COMPACT);
        } else {
            spanned = Html.fromHtml(processedHtml);
        }

        return spanned.toString();
    }

    /**
     * Processes ordered lists for plain text conversion
     */
    private static String processOrderedListsForPlainText(String html) {
        // Pattern to match ordered lists
        Pattern olPattern = Pattern.compile("<ol[^>]*>(.*?)</ol>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher olMatcher = olPattern.matcher(html);

        StringBuffer result = new StringBuffer();

        while (olMatcher.find()) {
            String listContent = olMatcher.group(1);
            String numberedList = convertToNumberedListPlainText(listContent);
            olMatcher.appendReplacement(result, numberedList);
        }
        olMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Processes unordered lists for plain text conversion
     */
    private static String processUnorderedListsForPlainText(String html) {
        // Pattern to match unordered lists
        Pattern ulPattern = Pattern.compile("<ul[^>]*>(.*?)</ul>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher ulMatcher = ulPattern.matcher(html);

        StringBuffer result = new StringBuffer();

        while (ulMatcher.find()) {
            String listContent = ulMatcher.group(1);
            String bulletList = convertToBulletListPlainText(listContent);
            ulMatcher.appendReplacement(result, bulletList);
        }
        ulMatcher.appendTail(result);

        return result.toString();
    }

    /**
     * Converts list items to numbered format for plain text with indentation
     */
    private static String convertToNumberedListPlainText(String listContent) {
        // Pattern to match list items
        Pattern liPattern = Pattern.compile("<li[^>]*>(.*?)</li>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher liMatcher = liPattern.matcher(listContent);

        StringBuilder numberedList = new StringBuilder();
        int counter = 1;

        while (liMatcher.find()) {
            String itemContent = liMatcher.group(1).trim();
            // Remove any inner HTML tags for plain text
            String plainItemContent = itemContent.replaceAll("<[^>]+>", "");
            numberedList.append("  ").append(counter).append(". ").append(plainItemContent).append("\n");
            counter++;
        }

        return numberedList.toString();
    }

    /**
     * Converts list items to bullet format for plain text with indentation
     */
    private static String convertToBulletListPlainText(String listContent) {
        // Pattern to match list items
        Pattern liPattern = Pattern.compile("<li[^>]*>(.*?)</li>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher liMatcher = liPattern.matcher(listContent);

        StringBuilder bulletList = new StringBuilder();

        while (liMatcher.find()) {
            String itemContent = liMatcher.group(1).trim();
            // Remove any inner HTML tags for plain text
            String plainItemContent = itemContent.replaceAll("<[^>]+>", "");
            bulletList.append("  • ").append(plainItemContent).append("\n");
        }

        return bulletList.toString();
    }
}
