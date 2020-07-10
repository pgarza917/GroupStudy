Original App Design Project
===

# Study Group 
* Will think of something better

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
-  An app that allows students on campuses across the United States to find study groups for their classes.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category**: Productivity/Social Networking/College
- **Mobile**: Direct messaging to coordinate quickly on mobile devices. Real-time maps to show on-campus locations. Directions to on-campus locations from mobile device.
- **Story**: Users can create profiles and invite other users to join study groups in the app. The creators of the groups can set privacy settings to limit who shows up. Includes real-time data on how crowded on-campus locations are to give a metric on how viable studying will be there. Functionality to log the work accomplished during duration of study time. Ability to share media between people in study group/session.
- **Market**: College students and faculty
- **Habit**: Forming study groups is perhaps one of the best ways for students to study the material they are taught in their college courses. However, it is sometimes rather difficult to coordinate and organize study groups due to a multitude of factors, which is what the features of this app attempt to target. For example, students sometimes plan to go to a certain library to study in a group but get there and realize it’s too noisy and/or there are no spaces to sit down as a group. The app can help in this regard with its metric for viability of studying.
- **Scope**: The app would begin with at least allowing users to create profiles and groups. Maps would also be included to set locations for studying. The next version would include directions, direct messaging, and media sharing. Next would come the metric for determing if a location is or isn’t adequate for studying in a group. Then would come logging functionality for groups/users to track their studying progress.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can login
* User can view timeline of upcoming and/or past group-study events
* User can search for other users or events (based on privacy settings)
* User can create a new account
* User can create a study event
* User can create group chat/discussion 
* User can view details about event
* User can view details about other users (based on privacy settings)

**Optional Nice-to-have Stories**

* User can directly message other users
* User can share events
* Users can share files/media in created groups/events
* Users can log their progress in studying
* Users can notify hosts of attendence
* Users can get directions to location of group study on campus
* Users can create collaborative documents (most likely through Google Docs) to share the notes they take
* Users can like messages in group chats/direct messaging

### 2. Screen Archetypes

* Login Screen
   * User can login
* Timeline Screen
   * User can view timeline of upcoming and/or past group-study events
   * User can quickly change attendence to upcoming events
   * User can share events
* Creation
    * User can create a study event and add it to their timeline
* Search
    * User can search for other users or events (based on privacy settings)
* Registration
    * User can create a new account
* Details
    * User can view details about event
* Profile
    * User can see details about their own profile and other users profiles (based on privacy settings)
* Messaging
    * Users can message in group chats/discussions 

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home timeline
* Create an event
* Search

**Flow Navigation** (Screen to Screen)

* Login Screen
    * => Home timeline
* Registration Screen
    * => Home timeline
* Search Screen
    * => Profile 
    * => Details
* Timeline Screen
    * => Messaging
    * => Creation
    * => Search
* Messaging Screen
    * => None
* Details
    * => Messaging
* Profile
    * => Messaging 

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="https://github.com/pgarza917/GroupStudy/blob/master/GroupStudy_wireframe_v1.jpg" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
### Models

- **User**

| Property       | Type         | Description  |
| :------------: | :----------: | :----------- |
| userId         | String       | unique ID for each user (default)|
| email          | String | email address of the user |
| pasword        | String | password of the user |
| bio            | String | user's bio |
| profileImage   | File   | image that user uses for their profile pic|
| privacy        | String | privacy setting of user |

- **Event**

| Property       | Type         | Description  |
| :------------: | :----------: | :----------- |
| eventId        | String  | unique ID for each event created (default) |
| title          | String | title of the event |
| description    | String | description of the event |
| location       | Location | location of the event |
| users          | List of User objects | list of users invited to an event 
| createdAt      | DateTime | time when event is created |
| privacy        | String | privacy setting of event |
| hosts          | List of User object | list of users hosting event |
| files          | List of File objects | list of files uploaded to event |
| gdocs          | List of Strings | list of urls to google docs of event |

* **Group**

| Property       | Type         | Description  |
| :------------: | :----------: | :----------- |
| groupId        | String       | unique ID for each group created (default) |
| name           | String       | name of the group |
| users          | List of User objects | list of users added to the group |
| createdAt      | DateTime     | time of when group was created |
| privacy        | String       | privacy setting of group |
| files          | List of File objects | list of files uploaded to group |



### Networking
**List of network requests by screen**
* Home timeline Screen
    * (Read/GET) Query all events where user is invited
    * ```java 
        // Specify which class to query from Parse DB
        ParseQuery<Post> query = ParseQuery.getQuery(Event.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.orderByDescending(Post.KEY_CREATED);
        // Using findInBackground to pull all Posts from DB
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Event> events, ParseException e) {
                // The ParseException will not be null if error with populating List with Post objects
                // went wrong with query
                if(e != null) {
                    Log.e(TAG, "Issue with getting events", e);
                    return;
                }
                // The ParseException will be null if List was populated successfully from query
                for(Event event : events) {
                    Log.i(TAG, "Event: " + event.getDescription() + ", username: " + post.getUser().getUsername());
                }
                mAdapter.clear();
                // Update the posts data set and notify the adapter of change
                mAdapter.addAll(events);
                // Now we call setRefreshing(false) to signal refresh has finished
                mSwipeContainer.setRefreshing(false);
            }
        })
        ```
* Event creation screen
    * (Create/POST) Create a new event object
    * ```java 
        Event event = new Post();
        event.setDescription(description);
        event.setUser(currentUser);
        event.setImage(new ParseFile(photoFile));

        // Executes query on a background thread to prevent disruption of main thread for improved
        // user experience
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error creating event", e);
                    Toast.makeText(getContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Event created successfully");
                // Clear our description field to give user more visual confirmation of success
                mEditTextDescription.setText("");
                // Clear out image view for more confirmation of save success
                mImageViewPost.setImageResource(0);
            }
        });
        ```
* Search screen
    * (Read/GET) Query events and users where privacy is open for current user
* Profile details screen
    * (Read/GET) Query selected user (default is current user)
    * (Update/PUT) Update current user's profile details
* Event details creen
    * (Read/GET) Query selected event
    * (Update/PUT) Update event details if user is host
    * (Read/GET) Query files and docs of selected event
* Register screen
    * (Create/POST) Create a new user object
