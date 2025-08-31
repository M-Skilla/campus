// Firestore Fix Script
// Run this in your Firebase Console > Firestore > Query tab

// 1. Find your user document by registration number or email
// 2. Update it with the following structure:

// EXAMPLE - Replace with your actual data:
{
  "fullName": "Your Full Name",
  "regNo": "YOUR_REGISTRATION_NUMBER",
  "course": "Your Course Name",
  "email": "your.email@student.domain",
  "roles": ["staff"],  // This is the KEY field that's missing!
  "staffDepartment": "Health",  // Choose: "Health", "Facilities", or "Library"
  "profilePicUrl": "your_image_url_if_any",
  "isActive": true
}

// CRITICAL: The "roles" field MUST be an array containing "staff"
// The "staffDepartment" MUST be exactly: "Health", "Facilities", or "Library"
