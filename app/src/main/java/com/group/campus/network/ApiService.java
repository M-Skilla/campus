package com.group.campus.network;

import com.group.campus.models.Suggestion;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

/**
 * Retrofit interface for Suggestions API endpoints
 * TODO: Replace base URL with your actual backend server URL
 */
public interface ApiService {

    // Base URL - TODO: Update with your actual server URL
    String BASE_URL = "https://your-api-server.com/api/";

    /**
     * Get authorized departments for current user
     * @return List of departments the user can send suggestions to
     */
    @GET("user/departments")
    Call<List<Department>> getUserDepartments();

    /**
     * Get suggestions for a specific department
     * @param departmentId Department ID to filter suggestions
     * @param page Page number for pagination (optional)
     * @param limit Number of items per page (optional)
     * @return List of suggestions for the department
     */
    @GET("suggestions")
    Call<List<Suggestion>> getSuggestions(
        @Query("departmentId") String departmentId,
        @Query("page") Integer page,
        @Query("limit") Integer limit
    );

    /**
     * Create a new suggestion
     * @param suggestion The suggestion data to send
     * @return The created suggestion with server-assigned ID
     */
    @POST("suggestions")
    Call<Suggestion> createSuggestion(@Body CreateSuggestionRequest suggestion);

    /**
     * Upload file attachment
     * @param file The file to upload as multipart
     * @param description Optional file description
     * @return Upload response with file URL
     */
    @Multipart
    @POST("uploads")
    Call<UploadResponse> uploadFile(
        @Part MultipartBody.Part file,
        @Part("description") RequestBody description
    );

    /**
     * Get current user information (for authorization checks)
     * @return User data including roles and permissions
     */
    @GET("user/profile")
    Call<UserProfile> getUserProfile();

    /**
     * Mark suggestion as read (optional feature)
     * @param suggestionId ID of the suggestion to mark as read
     * @return Success response
     */
    @PUT("suggestions/{id}/read")
    Call<ResponseBody> markSuggestionAsRead(@Path("id") String suggestionId);

    /**
     * Request class for creating suggestions
     */
    class CreateSuggestionRequest {
        private String text;
        private String departmentId;
        private List<String> attachmentUrls;

        public CreateSuggestionRequest(String text, String departmentId, List<String> attachmentUrls) {
            this.text = text;
            this.departmentId = departmentId;
            this.attachmentUrls = attachmentUrls;
        }

        // Getters
        public String getText() { return text; }
        public String getDepartmentId() { return departmentId; }
        public List<String> getAttachmentUrls() { return attachmentUrls; }
    }

    /**
     * Response class for file uploads
     */
    class UploadResponse {
        private String url;
        private String fileName;
        private long fileSize;
        private String mimeType;
        private String thumbnailUrl;

        // Getters
        public String getUrl() { return url; }
        public String getFileName() { return fileName; }
        public long getFileSize() { return fileSize; }
        public String getMimeType() { return mimeType; }
        public String getThumbnailUrl() { return thumbnailUrl; }
    }

    /**
     * User profile response for authorization checks
     */
    class UserProfile {
        private String id;
        private String name;
        private String email;
        private String avatar;
        private List<String> roles;
        private List<String> permissions;

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getAvatar() { return avatar; }
        public List<String> getRoles() { return roles; }
        public List<String> getPermissions() { return permissions; }

        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }

        public boolean hasPermission(String permission) {
            return permissions != null && permissions.contains(permission);
        }
    }

    /**
     * Simple Department class for API responses
     */
    class Department {
        private String id;
        private String name;
        private String description;

        public Department() {}

        public Department(String id, String name) {
            this.id = id;
            this.name = name;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return name; // For spinner display
        }
    }
}
