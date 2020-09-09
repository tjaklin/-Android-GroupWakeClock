package com.tjaklin.groupwakeclock.Util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tjaklin.groupwakeclock.Models.AlarmFB;
import com.tjaklin.groupwakeclock.Models.AlarmTemplate;
import com.tjaklin.groupwakeclock.Models.Event;
import com.tjaklin.groupwakeclock.Models.Friendship;
import com.tjaklin.groupwakeclock.Models.Member;
import com.tjaklin.groupwakeclock.Models.Membership;
import com.tjaklin.groupwakeclock.Models.Message;
import com.tjaklin.groupwakeclock.Models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FBObjectManager {

    // This is a singleton class or object (not sure).
    // We use this class to communicate with the FireBase.
    // We also use this to hold some of the data downloaded from the FireBase.

    private static final String TAG = FBObjectManager.class.getSimpleName();

    private static FBObjectManager fbObjectManager;

    // These variables are related to the user currently logged in.
    private static Context context;
    private User thisUser;
    private ArrayList<Membership> myOwnMemberships;
    private ArrayList<Event> myOwnEvents;
    private ArrayList<Friendship> myOwnFriendships;
    private ArrayList<User> myOwnFriends;

    // These variables hold information about the Event that our user is currently browsing.
    private ArrayList<Membership> currentEventMemberships;
    private ArrayList<Member> currentEventMembers;
    private ArrayList<Message> currentEventChat;

    // Listeners
    // We use those to continuously listen to changes made to certain nodes in our FireBase server.
    private ChildEventListener myOwnMebershipsListener = null;
    private ChildEventListener myOwnFriendshipsListener = null;

    private ChildEventListener currentEventChatListener = null;
    private ChildEventListener currentEventMebershipsListener = null;

    // Private constructor
    private FBObjectManager(Context c) {
        context = c;

        myOwnMemberships = new ArrayList<>();
        myOwnEvents = new ArrayList<>();
        myOwnFriendships = new ArrayList<>();
        myOwnFriends = new ArrayList<>();

        currentEventMemberships = new ArrayList<>();
        currentEventMembers = new ArrayList<>();
        currentEventChat = new ArrayList<>();
    }

    /**
     * Singleton getter
     */
    public static FBObjectManager getFbObjectManager(Context c) {
        if (fbObjectManager != null)
            return fbObjectManager;
        else {
            fbObjectManager = new FBObjectManager(c);
            return fbObjectManager;
        }
    }
    public Context getContext() {
        return context;
    }

    /**
     * fb objects getters
     */
    public User getThisUser() {
        return thisUser;
    }
    public ArrayList<Event> getMyOwnEvents() {
        return myOwnEvents;
    }
    public ArrayList<Friendship> getMyOwnFriendships() {
        return myOwnFriendships;
    }
    public ArrayList<User> getMyOwnFriends() {
        return myOwnFriends;
    }
    public ArrayList<Member> getCurrentEventMembers() {
        return currentEventMembers;
    }
    public ArrayList<Message> getCurrentEventChat() {
        return currentEventChat;
    }

    /**
     * setters
     */
    public void setThisUser(User thisUser) {
        this.thisUser = thisUser;
    }

    /**
     * QoL Methods
     */
    // Methods for adding to our variables.
    // These add to our user-information-tracking variables.
    public boolean addToMyOwnMemberships(Membership membership) {
        // Add only if membership not yet present
        for (int i = 0; i < myOwnMemberships.size(); i++) {
            if (membership.getEventID().equals(myOwnMemberships.get(i).getEventID())) {
                // This means that the membership for that eventID already exists.
                Log.e(TAG, "[addToMyOwnMemberships] User is already a member of that event. Ignoring request.");
                return false;
            }
        }

        myOwnMemberships.add(membership);
        Log.d(TAG, "[addToMyOwnMemberships] User is added to event.");
        return true;
    }
    public void addToMyOwnEvents(Event event) {
        // Add only if group not yet present
        for (int i = 0; i < myOwnEvents.size(); i++) {
            if (event.getEventID().equals(myOwnEvents.get(i).getEventID())) {
                // This means that the event already exists.
                Log.e(TAG, "[addToMyOwnEvents] Event is already present in myOwnEvents." +
                        " I'll update said event from the list with new data.");
                myOwnEvents.get(i).set(event);
                return;
            }
        }

        myOwnEvents.add(event);
        Log.d(TAG, "[addToMyOwnGroups] Group is added to user's myOwnGroups.");
    }
    public boolean addToMyOwnFriendships(Friendship friendship) {
        for (int i = 0; i < myOwnFriendships.size(); i++) {
            if (friendship.getUserID2().equals(myOwnFriendships.get(i).getUserID2())) {
                // Znači da friendship s tim prijateljem već postoji, stoga odbacujem ovaj pokušaj upisivanja.
                Log.e(TAG, "[addToMyOwnFriendships] Friendship already present in myOwnFriendships. Ignoring request.");
                return false;
            }
        }

        myOwnFriendships.add(friendship);
        Log.d(TAG, "[addToMyOwnFriendships] Friendship added to myOwnFriendships.");
        return true;
    }
    public void addToMyOwnFriends(User friend) {
        // Add only if friend not yet present
        for (int i = 0; i < myOwnFriends.size(); i++) {
            if (friend.getUserID().equals(myOwnFriends.get(i).getUserID())) {
                // Means that this friend already exists.
                Log.e(TAG, "[addToMyOwnFriends] Friend is already present in myOwnFriends." +
                        " I'll update said friend from the list with new data.");
                myOwnFriends.get(i).set(friend);
                return;
            }
        }

        myOwnFriends.add(friend);
        Log.d(TAG, "[addToMyOwnFriends] Friend is added to user's myOwnFriends.");
    }

    // These add to our temporary variables.
    public void addToCurrentEventMembers(Member newMember) {
        if (currentEventMembers.add(newMember)) {
            Log.d(TAG, "Added Member to CurrentEventMembers!");
        } else {
            Log.e(TAG, "Failed to add Member to CurrentEventMembers!");
        }
    }
    public void addToCurrentEventChat(Message message) {
        if (currentEventChat.add(message)) {
            Log.d(TAG, "Added Message to addToCurrentEventChat!");
        } else {
            Log.e(TAG, "Failed to add Message to addToCurrentEventChat!");
        }

    }

    // Methods for removing from our variables.
    public void removeFromMyOwnFriends(User friend) {
        myOwnFriends.remove(friend);
    }
    public void removeFromMyOwnFriendship(Friendship friendship) {
        myOwnFriendships.remove(friendship);
    }


    /**
     * FB onMultipleValueChangeEvent data listeners
     */

    // These methods are used for Establishing and Canceling Listeners that listen to nodes of
    // interest in our FireBase server.

    // Sets up a listener for user's Memberships.
    public void listenToMyOwnMembershipsByUserID(final String userID, final AsyncListenerVoid listener) {
        // First we clear any memberships and events that we had previously downloaded.
        myOwnMemberships.clear();
        myOwnEvents.clear();

        DatabaseReference membRef = FirebaseDatabase.getInstance().getReference().child("memberships");
        Query query = membRef.orderByChild("userID").equalTo(userID);

        if (listener != null)
            listener.onStart();

        myOwnMebershipsListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // When a relevant child node is added, we want to download it's information
                // and store it locally.
                //
                // In short, this method is triggered when someone adds our user to some event.
                // First we download the membership information. Then, using the just downloaded
                // membership information, we find the eventID that the membership is refering to.
                // Using this eventID we can donwload all relevant information about that Event.
                Membership newMembership = getMembershipFromDataSnapshot(dataSnapshot);
                if (newMembership != null) {
                    addToMyOwnMemberships(newMembership);
                    downloadEvent(newMembership.getEventID(), new AsyncListenerEvent() {
                        @Override
                        public void onStart() {
                            Log.d(TAG, "event. onStart() !");
                        }

                        @Override
                        public void onSuccess(Event event) {
                            Log.d(TAG, "event. onSuccess() !");
                            event.setCurrentUserMembershipID(newMembership.getMembershipID());
                            addToMyOwnEvents(event);

                            // After a successful download of Membership and Event data, we cna
                            // try and download information about the Alarm that is tied to our
                            // user for this Event.
                            //
                            // This thread decides to download the Alarm info only if the Event
                            // is happening in the next 24 hours. If not, it stops itself.
                            new DownloadUpcomingAlarmThread(context, thisUser, newMembership, event).start();

                            if (listener != null)
                                listener.onSuccess();
                        }

                        @Override
                        public void onFailed() {
                            Log.d(TAG, "event. onFailed() !");
                            if (listener != null) {
                                listener.onFailed();
                            }
                        }
                    });

                    Log.d(TAG, "[listenToMyOwnMembershipsByUserID]: New membership Processed!");
                } else {
                    Log.e(TAG, "[listenToMyOwnMembershipsByUserID]: Failed to Process New membership!");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // When a relevant child node is updated, we want to download it's information
                // and store it locally.
                Membership updatedMembership = getMembershipFromDataSnapshot(dataSnapshot);
                if (updatedMembership != null) {
                    // We go through our Events to make sure our user is really participating
                    // in that event.
                    for (int i = 0; i < myOwnEvents.size(); i++) {
                        if (myOwnEvents.get(i).getCurrentUserMembershipID().equals(updatedMembership.getMembershipID())) {
                            Log.d(TAG, "[onChildChanged] group found in myOwnGroups() ");

                            // We assume that this onChildChanged method was called because the admin of some Event
                            // changed our user's AlarmTemplate. This means we have to download the Alarm
                            // again and again every time that admin changes our AlarmTemplate.
                            // But only if the Event is in the next 24 hours.
                            new DownloadUpcomingAlarmThread(context, thisUser, updatedMembership, myOwnEvents.get(i)).start();

                            if (listener != null)
                                listener.onSuccess();

                            break;
                        }

                    }

                    Log.d(TAG, "[listenToMyOwnMembershipsByUserID]: Existing membership Processed!");

                } else {
                    Log.e(TAG, "[listenToMyOwnMembershipsByUserID]: Failed to Process Existing membership!");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // todo: Možda da tu preselim kod koji briše Group iz myOnwGroups? A možda ipak i ne!
                // Tu mi treba kad izađem iz grupe nek mi briše grupu!
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This get's called only if there's an error
                Log.e(TAG, "[listenToMyOwnMembershipsByUserID]: onCancelled() !");
                if (listener != null)
                    listener.onFailed();
            }
        });
        Log.d(TAG, "[listenToMyOwnMembershipsByUserID] listener Attached!");

        // This method is a really inefficient way of canceling the ProgressBar that was shown in
        // EventFragment.
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren())
                    if (listener != null)
                        listener.onFailed();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null)
                    listener.onFailed();
            }
        });
    }

    // This one just cancels the user's memberships listener.
    public void stopListeningToMyOwnMembershipsByUserID(final String userID, final AsyncListenerString listener) {

        if (listener != null) {
            listener.onStart();
        }

        Query query = FirebaseDatabase.getInstance().getReference().child("memberships")
                .orderByChild("userID").equalTo(userID);

        if (query == null) {
            Log.e(TAG, "stopListeningToMyOwnMembershipsByUserID -> query == NULL!");

            if (listener != null) {
                listener.onFailed();
            }

            return;
        }

        if (myOwnMebershipsListener != null) {
            query.removeEventListener(myOwnMebershipsListener);
        } else {
            Log.e(TAG, "MyOwnMebershipsListener is already == NULL");
            return;
        }
        if (listener != null) {
            listener.onSuccess("MyOwnMebershipsListener succesfully removed!");
        }
    }

    // Sets up a listener for user's Friendships.
    public void listenToMyOwnFriendships(final String userID, final AsyncListenerVoid listener) {

        // First we clear any friendships and friend's info that we had previously downloaded.
        // (friends are just users but i use the term friends here because it makes more sense to me)
        myOwnFriendships.clear();
        myOwnFriends.clear();

        DatabaseReference friendshipsRef = FirebaseDatabase.getInstance().getReference().child("friendships").child(userID);

        myOwnFriendshipsListener = friendshipsRef.addChildEventListener(new ChildEventListener() {
            // When a relevant child node is added, we want to download it's information
            // and store it locally.
            //
            // In short, this method is triggered when someone adds our user as a friend.
            // First we download the friendship information. Then, using the just downloaded
            // friendship information, we find the userID of our user's new friend.
            // Using this userID we can donwload all relevant information about that user.
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Friendship newFriendship = getFriendshipFromDataSnapshot(dataSnapshot);
                if (newFriendship != null) {
                    addToMyOwnFriendships(newFriendship);
                    downloadUser(newFriendship.getUserID2(), new AsyncListenerUser() {
                        @Override
                        public void onStart() {
                            Log.d(TAG, "user. onStart() !");
                        }

                        @Override
                        public void onSuccess(User user) {
                            Log.d(TAG, "user. onSuccess() !");

                            // Friend's data is saved.
                            addToMyOwnFriends(user);

                            if (listener != null)
                                listener.onSuccess();
                        }

                        @Override
                        public void onFailed() {
                            Log.d(TAG, "user. onFailed() !");
                            if (listener != null)
                                listener.onFailed();
                        }
                    });

                    Log.d(TAG, "[my_own_friendships_listener]: New friendship Processed!");
                } else {
                    Log.e(TAG, "[my_own_friendships_listener]: Failed to Process New friendship!");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Friendship oldFriendship = getFriendshipFromDataSnapshot(dataSnapshot);
                if (oldFriendship != null) {
                    removeFromMyOwnFriendship(oldFriendship);
                    // We loop through the list of user's friends and find the one that we need to
                    // unfriend. We find the correct user using the userID.
                    for (int i = 0; i < myOwnFriends.size(); i++) {
                        if (myOwnFriends.get(i).getUserID().equals(oldFriendship.getUserID2()))
                            removeFromMyOwnFriends(myOwnFriends.get(i));
                    }

                    Log.d(TAG, "[my_own_friendships_listener]: Old friendship Removed!");
                    if (listener != null)
                        listener.onSuccess();
                } else {
                    Log.e(TAG, "[my_own_friendships_listener]: Failed to Process New friendship!");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d(TAG, "[listenToMyOwnFriendships] listener Attached!");

        // This method is a really inefficient way of canceling the ProgressBar that was shown in
        // FriendsFragment.
        friendshipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren())
                    if (listener != null)
                        listener.onFailed();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null)
                    listener.onFailed();
            }
        });
    }

    public void stopListeningToMyOwnFriendships(final String userID, final AsyncListenerString listener) {

        if (listener != null) {
            listener.onStart();
        }

        Query query = FirebaseDatabase.getInstance().getReference().child("memberships").child(userID);

        if (query == null) {
            Log.e(TAG, "stopListeningToMyOwnFriendships -> query == NULL!");

            if (listener != null) {
                listener.onFailed();
            }

            return;
        }

        if (myOwnFriendshipsListener != null) {
            query.removeEventListener(myOwnFriendshipsListener);
        } else {
            Log.e(TAG, "MyOwnFriendshipListener is already == NULL");
            return;
        }
        if (listener != null) {
            listener.onSuccess("MyOwnFriendshipListener succesfully removed!");
        }
    }

    // Sets up a listener for some Event's Chat.
    public void listenToCurrentEventChat(final String chatID, final AsyncListenerVoid listener) {
        // First we clear any messages that we had previously downloaded.
        currentEventChat.clear();

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats").child(chatID);

        if (chatRef == null) {
            Log.e(TAG, "listenForChat -> chatRef == NULL!");
            return;
        }

        if (listener != null)
            listener.onStart();

        currentEventChatListener = chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // We just download the message and save it locally.
                Message newMessage = getMessageFromDataSnapshot(dataSnapshot);
                if (newMessage != null) {
                    addToCurrentEventChat(newMessage);
                    Log.d(TAG, "[current_event_chat_listener]: New message Processed!");
                    if (listener != null)
                        listener.onSuccess();
                } else {
                    Log.e(TAG, "[current_event_chat_listener]: Failed to Process New message!");
                    if (listener != null)
                        listener.onFailed();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "[current_event_chat_listener] .onCancelled() !");
            }
        });

        Log.d(TAG, "[listenToCurrentEventChat] listener Attached!");
    }

    public void stopListeningToCurrentEventChat(final String chatID, final AsyncListenerString listener) {

        if (listener != null) {
            listener.onStart();
        }

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats").child(chatID);

        if (chatRef == null) {
            Log.e(TAG, "CurrentEventChatListener -> chatRef == NULL!");
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }

        if (currentEventChatListener != null) {
            chatRef.removeEventListener(currentEventChatListener);
            currentEventChat.clear();
        } else {
            Log.e(TAG, "CurrentEventChatListener is already == NULL");
            return;
        }
        if (listener != null) {
            listener.onSuccess("CurrentEventChatListener succesfully removed!");
        }
    }

    // Sets up a listener for some Event's memberships. This is how we get information about all of event's members.
    public void listenToCurrentEventMembershipsByEventID(final String eventID, final AsyncListenerVoid listener) {
        // Clear all old data.
        //
        // currentEventMemberships holds Membership objects of all members of Event eventID.
        // This is the event that our user is currently browsing. We keep only data for the events
        // that the user is browsing. When the user goes back from EventActivity to EventFragments
        // (which lists all user's events) and picks another event - this method will be called to
        // download data about that event's memberships.
        //
        // currentEventMembers holds all Member objects for that event.
        currentEventMemberships.clear();
        currentEventMembers.clear();

        DatabaseReference membRef = FirebaseDatabase.getInstance().getReference().child("memberships");
        Query query = membRef.orderByChild("groupID").equalTo(eventID);

        if (listener != null)
            listener.onStart();

        currentEventMebershipsListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // When a new membership is added to this event.
                Membership newMembership = getMembershipFromDataSnapshot(dataSnapshot);
                if (newMembership != null) {
                    // We download it's member.
                    downloadMember(newMembership, new AsyncListenerMember() {
                        @Override
                        public void onStart() {
                            Log.d(TAG, "member. onStart() !");
                        }

                        @Override
                        public void onSuccess(Member member) {
                            Log.d(TAG, "member. onSuccess() !");
                            addToCurrentEventMembers(member);
                            if (listener != null) {
                                listener.onSuccess();
                            }
                        }

                        @Override
                        public void onFailed() {
                            Log.d(TAG, "member. onFailed() !");
                            if (listener != null) {
                                listener.onFailed();
                            }
                        }
                    });
                    Log.d(TAG, "[current_event_membership_listener]: New membership Processed!");
                } else {
                    Log.e(TAG, "[current_event_membership_listener]: Failed to Process New membership!");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // When a membership is updated (probably because the admin changed
                // this member's alarmTemplate).
                Membership updatedMembership = getMembershipFromDataSnapshot(dataSnapshot);
                if (updatedMembership != null) {
                    // Find a member in currentEventMembers using membership.userID.
                    // If no such member is found, update it with membership.alarmTemplate and
                    // membership.isAwake values.
                    for (Member m : currentEventMembers) {
                        if (m.getUserID().equals(updatedMembership.getUserID())) {
                            // Update values.
                            m.setAlarmTemplate(updatedMembership.getAlarmTemplate());
                            m.isAwake(updatedMembership.isAwake());

                            Log.d(TAG, "[current_event_membership_listener]: Existing member Updated!");

                            if (listener != null) {
                                listener.onSuccess();
                            }

                            break;
                        }
                    }
                    Log.d(TAG, "[current_event_membership_listener]: Existing membership Processed!");

                } else {
                    Log.e(TAG, "[current_event_membership_listener]: Failed to Process Existing membership!");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Membership removed.
                Membership removedMembership = getMembershipFromDataSnapshot(dataSnapshot);
                if (removedMembership != null) {
                    // find member in currentEventMembers using membership.userID.
                    // If such user is found, remove it.
                    for (int i = 0; i <  currentEventMembers.size(); i++) {
                        if (currentEventMembers.get(i).getUserID().equals(removedMembership.getUserID())) {
                            currentEventMembers.remove(i);
                            Log.d(TAG, "[current_event_membership_listener]: Existing member Removed!");

                            if (listener != null) {
                                listener.onSuccess();
                            }

                            break;
                        }
                    }
                    Log.d(TAG, "[current_event_membership_listener]: Existing membership Processed!");

                } else {
                    Log.e(TAG, "[current_event_membership_listener]: Failed to Process Existing membership!");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "[current_event_membership_listener]: onCancelled() !");
            }
        });
        Log.d(TAG, "[listenToCurrentEventMembershipsByEventID] listener Attached!");
    }

    public void stopListeningToCurrentEventMembershipsByEventID(final String eventID, final AsyncListenerString listener) {

        if (listener != null) {
            listener.onStart();
        }

        Query query = FirebaseDatabase.getInstance().getReference().child("memberships")
                .orderByChild("groupID").equalTo(eventID);

        if (query == null) {
            Log.e(TAG, "stopListeningToCurrentEventMembershipsByEventID -> query == NULL!");

            if (listener != null) {
                listener.onFailed();
            }

            return;
        }

        if (currentEventMebershipsListener != null) {
            query.removeEventListener(currentEventMebershipsListener);
        } else {
            Log.e(TAG, "currentEventMebershipsListener is already == NULL");
            return;
        }
        if (listener != null) {
            listener.onSuccess("currentEventMebershipsListener succesfully removed!");
        }
    }


    /**
     * FB onSingleValueChangedEvent data downloaders
     */

    // These methods are used for one-time-downloads of information that are stored at some nodes of
    // interest in our FireBase server.

    // This is used to download Event data
    public void downloadEvent(final String groupID, final AsyncListenerEvent listener) {

        if (listener != null) {
            listener.onStart();
        }

        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupID);
        if (eventsRef == null) {
            Log.e(TAG, "downloadEvent.eventRef == NULL!");
            return;
        }
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() entered!");
                if (dataSnapshot.exists()) {
                    // eventname, description, datetime, adminUserID, dfltAlrmTp, dfltAlrmCmplxt su bitni podaci!
                    String eventname = null, description = null, adminUserID = null, eventChatID = null, defaultAlarmType = null;
                    long datetime = -1;
                    int defaultAlarmComplexity = -1;

                    // In our FireBase server, Events are called Groups because that's the name i used before.
                    if (dataSnapshot.child("groupname").getValue(String.class) != null)
                        eventname = dataSnapshot.child("groupname").getValue(String.class);
                    else
                        Log.d(TAG, "groupname == NULL");

                    if (dataSnapshot.child("description").getValue(String.class) != null)
                        description = dataSnapshot.child("description").getValue(String.class);
                    else
                        Log.d(TAG, "description == NULL");

                    if (dataSnapshot.child("adminUserID").getValue(String.class) != null)
                        adminUserID = dataSnapshot.child("adminUserID").getValue(String.class);
                    else
                        Log.d(TAG, "adminUserID == NULL");

                    if (dataSnapshot.child("groupChatID").getValue(String.class) != null)
                        eventChatID = dataSnapshot.child("groupChatID").getValue(String.class);
                    else
                        Log.d(TAG, "groupChatID == NULL");

                    if (dataSnapshot.child("defaultAlarmType").getValue(String.class) != null)
                        defaultAlarmType = dataSnapshot.child("defaultAlarmType").getValue(String.class);
                    else
                        Log.d(TAG, "defaultAlarmType == NULL");

                    if (dataSnapshot.child("datetime").getValue(Long.class) != null)
                        datetime = dataSnapshot.child("datetime").getValue(Long.class);
                    else
                        Log.d(TAG, "datetime == NULL");

                    if (dataSnapshot.child("defaultAlarmComplexity").getValue(Integer.class) != null)
                        defaultAlarmComplexity = dataSnapshot.child("defaultAlarmComplexity").getValue(Integer.class);
                    else
                        Log.d(TAG, "defaultAlarmComplexity == NULL");

                    // preuzeti su svi podaci
                    if ( (eventname != null) && (description != null)
                            && (adminUserID != null) && (eventChatID != null)
                            && (defaultAlarmType != null) && (datetime != -1)
                            && (defaultAlarmComplexity != -1) ) {
                        Log.d(TAG, "All fields are correct.");

                        AlarmTemplate newAlarmTemplate = new AlarmTemplate(defaultAlarmType, defaultAlarmComplexity);

                        Event newEvent = new Event(groupID, eventname, description, datetime,
                                adminUserID, eventChatID, newAlarmTemplate);
                        if (listener != null)
                            listener.onSuccess(newEvent);
                    } else {
                        Log.d(TAG, "Some fields are incorrect!");
                        Log.d(TAG, "Event not added!");
                    }
                } else {
                    Log.d(TAG, "downloadEvent.datasnapshot.exists == FALSE!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "downloadEvent().onCancelled() entered!");
            }
        });
    }

    // This is used to download User data by using userID
    public void downloadUser(final String userID, final AsyncListenerUser listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        if (userRef == null) {
            Log.e(TAG, "downloadUser.userRef == NULL!");
            return;
        }
        if (listener != null)
            listener.onStart();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() entered!");
                if (dataSnapshot.exists()) {
                    String email = null;

                    if (dataSnapshot.child("email").getValue(String.class) != null)
                        email = dataSnapshot.child("email").getValue(String.class);
                    else
                        Log.d(TAG, "email == NULL");

                    if ( email != null ) {
                        Log.d(TAG, "All fields are correct.");

                        User newUser = new User(userID, email);
                        if (listener != null)
                            listener.onSuccess(newUser);
                    } else {
                        Log.d(TAG, "Some fields are incorrect!");
                        Log.d(TAG, "User not added!");
                    }
                } else {
                    Log.d(TAG, "downloadUser.datasnapshot.exists == FALSE!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "downloadUser().onCancelled() entered!");
            }
        });

    }

    // This is used to download User data by using email instead of userID
    public void downloadUserByEmail(final String email, final AsyncListenerUser listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        if (userRef == null) {
            Log.e(TAG, "downloadUserByEmail.userRef == NULL!");
            return;
        }

        Query query = userRef.orderByChild("email").equalTo(email);
        if (listener != null)
            listener.onStart();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() entered!");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String userID = childSnapshot.getKey();
                        if ( userID != null ) {
                            Log.d(TAG, "All fields are correct.");
                            User newUser = new User(userID, email);
                            if (listener != null)
                                listener.onSuccess(newUser);
                        } else {
                            Log.d(TAG, "Some fields are incorrect!");
                            Log.d(TAG, "Failed to retrieve UserID!");
                            if (listener != null) {
                                listener.onFailed();
                            }
                        }
                    }

                } else {
                    Log.d(TAG, "downloadUserByEmail.datasnapshot.exists == FALSE!");
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "downloadUserByEmail().onCancelled() entered!");
                if (listener != null) {
                    listener.onFailed();
                }
            }
        });
    }

    // Ovu metodu koristim za GroupAc, za punjenje currentGroupMembers!
    public void downloadMember(final Membership membership, final AsyncListenerMember listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(membership.getUserID());
        if (userRef == null) {
            Log.e(TAG, "downloadMember.userRef == NULL!");
            return;
        }
        // todo: Ne znam na koje mjesto da postavim listener.onStart() !
        if (listener != null)
            listener.onStart();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() entered!");
                if (dataSnapshot.exists()) {
                    // email su bitni podaci!
                    String email = null;

                    if (dataSnapshot.child("email").getValue(String.class) != null)
                        email = dataSnapshot.child("email").getValue(String.class);
                    else
                        Log.d(TAG, "email == NULL");

                    // preuzeti su svi podaci
                    if ( email != null ) {
                        Log.d(TAG, "All fields are correct.");
                        Member newMember = new Member(membership.getUserID(), email, membership.getMembershipID(), membership.getAlarmTemplate(), membership.isAwake());
                        if (listener != null)
                            listener.onSuccess(newMember);
                    } else {
                        Log.d(TAG, "Some fields are incorrect!");
                        Log.d(TAG, "User not added!");
                    }
                } else {
                    Log.d(TAG, "getUser.datasnapshot.exists == FALSE!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "getGroupByGroupID().onCancelled() entered!");
            }
        });

    }

    // Ovu metodu koristim u CheckForUpcomingEventsService za preuzimanje cijelog alarma!
    public void downloadAlarmFBs(final AlarmTemplate alarmTemplate, final AsyncListenerAlarmFBs listener) {

        if (alarmTemplate == null)
            return;

        Log.d(TAG, "[downloadAlarmFBs] at = " + alarmTemplate.getType() + ", " + alarmTemplate.getComplexity());

        DatabaseReference alarmTCRef = FirebaseDatabase.getInstance().getReference().child("alarms")
                .child(alarmTemplate.getType())
                .child(String.valueOf(alarmTemplate.getComplexity()));

        // Ok, sad kad imam referencu, moram znati da broj čvorova pod tom referencom može biti između 0 i 10.
        // Prvo budem upisal sve u Mapu<QuestionID, Answer> map; Nakon toga uzimam .size te mape pa odaberem
        // nasumično jedan element iz mape.

        if (alarmTCRef == null) {
            Log.e(TAG, "downloadAlarm.alarmTCRef == NULL!");
            return;
        }

        if (listener != null)
            listener.onStart();

        ArrayList<AlarmFB> alarmRows = new ArrayList<>();

        alarmTCRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() entered!");
                if (dataSnapshot != null) {
                    String questionID = null, answer = null;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        if (childSnapshot.child("questionID").getValue(String.class) != null)
                            questionID = childSnapshot.child("questionID").getValue(String.class);
                        else
                            Log.d(TAG, "questionID = NULL");

                        if (childSnapshot.child("answer").getValue(String.class) != null)
                            answer = childSnapshot.child("answer").getValue(String.class);
                        else
                            Log.d(TAG, "answer = NULL");

                        // end of dling columns
                        if ( (questionID != null) && (answer != null) ) {
                            AlarmFB newAlarmFB = new AlarmFB(questionID, answer);

                            alarmRows.add(newAlarmFB);
                            Log.d(TAG, "alarmRow succesfully inserted into local Map object!");

                        } else {
                            Log.d(TAG, "Illegal fields for alarmRow element!");
                        }
                    }

                    // Nakon punjenja alarmRows, šaljem ih preko listener.onSuccess()!

                    if (listener != null) {
                        listener.onSuccess(alarmRows);
                    }

                } else {
                    Log.d(TAG, "Datasnapshot == NULL for downloadAlarm!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled for downloadAlarm!");
            }
        });

    }
    // Ovu metodu koristim u CheckForUpcomingEventsService za preuzimanje cijelog alarma!
    public void downloadImage(final AlarmTemplate alarmTemplate, final AlarmFB alarmFB, final AsyncListenerImage listener) {

        if (listener != null) {
            listener.onStart();
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        if (storageRef != null) {
            StorageReference questionRef = storageRef.child("alarmQuestions").child(alarmTemplate.getType())
                    .child(String.valueOf(alarmTemplate.getComplexity())).child(alarmFB.getQuestionID());
            if (questionRef != null) {
                final long ONE_MEGABYTE = 1024 * 1024;
                questionRef.getBytes(5 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    Log.d(TAG, "Succesfully downloaded alarm Image from FB.");
                    if (listener != null) {
                        listener.onSuccess(bytes);
                    }
                }).addOnFailureListener(exception -> {
                    Log.d(TAG, "Failed to download alarm Image from FB!");
                    if (listener != null) {
                        listener.onFailed();
                    }
                });

            } else {
                Log.d(TAG, "picRef == NULL! Reading alarm failed!");
            }
        } else {
            Log.e(TAG, "storageRef == NULL!");
        }

    }


    /**
     * FB data uploaders
     */

    // These methods are used to upload into the FireBase server. Some of them are for deleting
    // data from FireBase.

    // Uploads Membership.
    public void uploadMembership(final Membership membership, final AsyncListenerString listener) {
        if (listener != null)
            listener.onStart();

        DatabaseReference membershipRef = FirebaseDatabase.getInstance().getReference()
                .child("memberships").push();

        if (membershipRef != null) {
            Map membershipMap = new HashMap<>();

            membershipMap.put("groupID", membership.getEventID());
            membershipMap.put("userID", membership.getUserID());
            membershipMap.put("datetime", membership.getDatetimeInMillis());
            membershipMap.put("alarmType", membership.getAlarmTemplate().getType());
            membershipMap.put("alarmComplexity", membership.getAlarmTemplate().getComplexity());
            membershipMap.put("isAwake", false);

            membershipRef.updateChildren(membershipMap);

            Log.d(TAG, "Membership uploaded!");
            if (listener != null) {
                listener.onSuccess(membershipRef.getKey());
            }
        } else {
            Log.d(TAG, "Membership upload failed!");
            if (listener != null)
                listener.onFailed();
        }
    }

    // Updates existing Membership. Used when the Event admin changes some user's AlarmTemplate.
    // Also used in AlarmGoingOffActivity when the app notifies the Event members that this user
    // is awake.
    public void updateMembership(final Membership membership, final AsyncListenerString listener) {
        if (listener != null) {
            listener.onStart();
        }
        DatabaseReference membershipRef = FirebaseDatabase.getInstance().getReference()
                .child("memberships").child(membership.getMembershipID());

        Map membershipMap = new HashMap<>();

        membershipMap.put("alarmType", membership.getAlarmTemplate().getType());
        membershipMap.put("alarmComplexity", membership.getAlarmTemplate().getComplexity());
        membershipMap.put("isAwake", membership.isAwake());

        membershipRef.updateChildren(membershipMap);

        Log.d(TAG, "Membership updated!!");

        if (listener != null)
            listener.onSuccess(membership.getMembershipID());
    }

    // Deletes Membership.
    public void deleteMembership(final String membershipID, final AsyncListenerString listener) {

        if (listener != null) {
            listener.onStart();
        }

        DatabaseReference membRef = FirebaseDatabase.getInstance().getReference().child("memberships").child(membershipID);
        if (membRef != null) {
            membRef.removeValue();
            if (listener != null)
                listener.onSuccess("");
        } else {
            Log.e(TAG, "[deleteMembership] reference == NULL!");
            if (listener != null) {
                listener.onFailed();
            }
        }


    }

    // Uploads Friendship.
    public void uploadFriendship(final Friendship friendship, final AsyncListenerFriendship listener) {
        if (listener != null) {
            listener.onStart();
        }

        if (friendship.getUserID1().equals(friendship.getUserID2())) {
            Log.e(TAG, "Cannot add self as a friend :(");
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }

        DatabaseReference friend1Ref = FirebaseDatabase.getInstance().getReference().child("friendships").child(friendship.getUserID1());
        DatabaseReference friend2Ref = FirebaseDatabase.getInstance().getReference().child("friendships").child(friendship.getUserID2());

        String friendship1ID = friend1Ref.push().getKey();
        String friendship2ID = friend2Ref.push().getKey();


        if ( (friendship1ID != null) && (friendship2ID != null) ) {

            friend1Ref.child(friendship1ID).child("friendID").setValue(friendship.getUserID2());
            friend2Ref.child(friendship2ID).child("friendID").setValue(friendship.getUserID1());
            Log.d(TAG, "Friendship uploaded!");
            if (listener != null) {
                listener.onSuccess(friendship);
            }

        } else {
            Log.e(TAG, "Failed to upload friendship!");
        }
    }

    // Deletes Friendship.
    public void deleteFriendship(final Friendship friendship, final AsyncListenerFriendship listener) {

        if (listener != null) {
            listener.onStart();
        }

        DatabaseReference friend1Ref = FirebaseDatabase.getInstance().getReference().child("friendships").child(friendship.getUserID1());
        DatabaseReference friend2Ref = FirebaseDatabase.getInstance().getReference().child("friendships").child(friendship.getUserID2());

        // Delete friendship for user1.
        Query query1 = friend1Ref.orderByChild("friendID").equalTo(friendship.getUserID2());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        childSnapshot.getRef().removeValue();
                        Log.d(TAG, "Friendship succesfully deleted!");
                        if (listener != null)
                            listener.onSuccess(friendship);
                    }

                } else {
                    Log.d(TAG, "deleteFriendship.datasnapshot.exists == FALSE!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "deleteFriendship().onCancelled() Called!");
            }
        });

        // Delete friendship for user2.
        Query query2 = friend2Ref.orderByChild("friendID").equalTo(friendship.getUserID1());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        childSnapshot.getRef().removeValue();
                        Log.d(TAG, "Friendship succesfully deleted!");
                        if (listener != null)
                            listener.onSuccess(friendship);
                    }

                } else {
                    Log.d(TAG, "deleteFriendship.datasnapshot.exists == FALSE!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "deleteFriendship().onCancelled() Called!");
            }
        });
    }

    // Uploads User. This is used in LoginActivity when we register the user.
    public void uploadUser(final String userID, final String userEmail, final AsyncListenerVoid listener) {

        if (listener != null)
            listener.onStart();

        DatabaseReference thisUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userID);

        if (thisUserRef != null) {
            thisUserRef.child("email").setValue(userEmail);

            Log.d(TAG, "User uploaded!");
            if (listener != null)
                listener.onSuccess();
        } else {
            Log.d(TAG, "User upload failed!");
            if (listener != null)
                listener.onFailed();
        }

    }

    // Uploads Event. This is used when creating an Event in CreateEventActivity.
    public void uploadEvent(final Event event, final AsyncListenerString listener) {

        if (listener != null)
            listener.onStart();

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("groups");
        String eventID = eventRef.push().getKey();

        if (eventID != null) {
            eventRef.child(eventID).child("groupname").setValue(event.getEventname());
            eventRef.child(eventID).child("description").setValue(event.getDescription());
            eventRef.child(eventID).child("datetime").setValue(event.getDatetimeInMillis());

            eventRef.child(eventID).child("defaultAlarmType").setValue(event.getDefaultAlarmTemplate().getType());
            eventRef.child(eventID).child("defaultAlarmComplexity").setValue(event.getDefaultAlarmTemplate().getComplexity());

            eventRef.child(eventID).child("adminUserID").setValue(event.getAdminUserID());
            eventRef.child(eventID).child("groupChatID").setValue(event.getEventChatID());

            Log.d(TAG, "Event uploaded!");
            if (listener != null) {
                listener.onSuccess(eventID);
            }
        } else {
            Log.d(TAG, "Event upload failed!");
            if (listener != null)
                listener.onFailed();
        }
    }

    // Uploads a new Chat. This is used when creating an Event in CreateEventActivity.
    public void uploadNewChat(final String initialMsg, final AsyncListenerString listener) {

        if (listener != null)
            listener.onStart();

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
        String chatID = chatsRef.push().getKey();

        if (chatID != null) {
            String msgID = chatsRef.child(chatID).push().getKey();
            if (msgID != null) {

                pushMessageIntoChat(new Message("system", initialMsg), msgID, null);

                if (listener != null) {
                    listener.onSuccess(chatID);
                }
            }
        } else {
            Log.d(TAG, "uploadNewChat() returned NULL");
            if (listener != null) {
                listener.onFailed();
            }
        }
    }

    // This pushes a Message to some existing Chat. Used in EventActivity.
    public void pushMessageIntoChat(final Message msg, final String chatID, final AsyncListenerString listener) {

        if (listener != null)
            listener.onStart();

        DatabaseReference newMsgRef = FirebaseDatabase.getInstance().getReference().child("chats").child(chatID).push();

        if (newMsgRef != null) {
            Map msgMap = new HashMap<>();

            msgMap.put("senderID", msg.getSenderID());
            msgMap.put("content", msg.getContent());

            newMsgRef.updateChildren(msgMap);

            if (listener != null) {
                listener.onSuccess(newMsgRef.getKey());
            }

        } else {
            Log.d(TAG, "pushMessageIntoChat() returned NULL");
            if (listener != null) {
                listener.onFailed();
            }
        }


    }


    /**
     * These methods help extract information from DataSnapshot objetcs
     */

    // These are helper methods that are used to extract information form a DataSnapshot object,
    // which is a FireBase owned object.

    private Membership getMembershipFromDataSnapshot(DataSnapshot childSnapshot) {
        if (childSnapshot == null || !childSnapshot.exists()) {
            Log.e(TAG, "[getMembershipFromDataSnapshot] childSnapshot == NULL or doesn't exist() !");
            return null;
        }
        if (childSnapshot.getChildrenCount() == 0) {
            Log.e(TAG, "[getMembershipFromDataSnapshot] childSnapshot.isEmpty()!");
            return null;
        }

        String membershipID = null, eventID = null, userID = null, alarmType = null;
        long datetime = -1;
        int alarmComplexity = -1;
        boolean isAwake = false;

        // Ovdje punim objekt result podacima iz dataSnapshot-a.
        membershipID = childSnapshot.getKey();

        if (childSnapshot.child("groupID").getValue(String.class) != null)
            eventID = childSnapshot.child("groupID").getValue(String.class);
        else
            Log.d(TAG, "groupID == NULL");

        if (childSnapshot.child("userID").getValue(String.class) != null)
            userID = childSnapshot.child("userID").getValue(String.class);
        else
            Log.d(TAG, "userID == NULL");

        if (childSnapshot.child("alarmType").getValue(String.class) != null)
            alarmType = childSnapshot.child("alarmType").getValue(String.class);
        else
            Log.d(TAG, "alarmType == NULL");

        if (childSnapshot.child("datetime").getValue(Long.class) != null)
            datetime = childSnapshot.child("datetime").getValue(Long.class);
        else
            Log.d(TAG, "alarmComplexity == NULL");

        if (childSnapshot.child("alarmComplexity").getValue(Integer.class) != null)
            alarmComplexity = childSnapshot.child("alarmComplexity").getValue(Integer.class);
        else
            Log.d(TAG, "alarmComplexity == NULL");

        if (childSnapshot.child("isAwake").getValue(Boolean.class) != null)
            isAwake = childSnapshot.child("isAwake").getValue(Boolean.class);
        else
            Log.d(TAG, "isAwake == NULL");

        // end of dling columns
        if ( (membershipID != null) && (eventID != null) && (userID != null) && (datetime != -1)
                && (alarmType != null) && (alarmComplexity != -1) ) {
            Membership result = new Membership(membershipID, eventID, userID, datetime,
                    new AlarmTemplate(alarmType, alarmComplexity));
            if (isAwake)
                result.isAwake(true);

            return result;

        } else {
            Log.d(TAG, "Error extracting 1 or more values from dataSnapshot!");
            return null;
        }

    }

    private Friendship getFriendshipFromDataSnapshot(DataSnapshot childSnapshot) {
        if (childSnapshot == null || !childSnapshot.exists()) {
            Log.e(TAG, "[getFriendshipFromDataSnapshot] childSnapshot == NULL or doesn't exist() !");
            return null;
        }
        if (childSnapshot.getChildrenCount() == 0) {
            Log.e(TAG, "[getFriendshipFromDataSnapshot] childSnapshot.isEmpty()!");
            return null;
        }

        String friendID = null;

        // Here we extract the info.
        if (childSnapshot.child("friendID").getValue(String.class) != null)
            friendID = childSnapshot.child("friendID").getValue(String.class);
        else
            Log.d(TAG, "friendID == NULL");

        // Here we store the extracted info.
        if ( (friendID != null) ) {
            Friendship result = new Friendship(thisUser.getUserID(), friendID);
            return result;

        } else {
            Log.d(TAG, "[getFriendshipFromDataSnapshot] Error extracting 1 or more values from dataSnapshot!");
            return null;
        }
    }

    private Message getMessageFromDataSnapshot(DataSnapshot childSnapshot) {
        if (childSnapshot == null || !childSnapshot.exists()) {
            Log.e(TAG, "[getMessageFromDataSnapshot] childSnapshot == NULL or doesn't exist() !");
            return null;
        }
        if (childSnapshot.getChildrenCount() == 0) {
            Log.e(TAG, "[getMessageFromDataSnapshot] childSnapshot.isEmpty()!");
            return null;
        }

        String senderID = null, content = null;

        if (childSnapshot.child("senderID").getValue(String.class) != null)
            senderID = childSnapshot.child("senderID").getValue(String.class);
        else
            Log.d(TAG, "senderID == NULL");

        if (childSnapshot.child("content").getValue(String.class) != null)
            content = childSnapshot.child("content").getValue(String.class);
        else
            Log.d(TAG, "content == NULL");

        // Store extracted info.
        if ( (senderID != null) && (content != null) ) {
            Log.d(TAG, "All fields are correct.");

            return new Message(senderID, content);

        } else {
            Log.e(TAG, "Error extracting 1 or more values from dataSnapshot!");
            return null;
        }

    }

    /**
     * Listeners
     */

    public interface AsyncListenerVoid {
        void onStart();
        void onSuccess();
        void onFailed();
    }

    public interface AsyncListenerString {
        void onStart();
        void onSuccess(String id);
        void onFailed();
    }

    public interface AsyncListenerMembership {
        void onStart();
        void onSuccess(Membership membership);
        void onFailed();
    }

    public interface AsyncListenerFriendship {
        void onStart();
        void onSuccess(Friendship friendship);
        void onFailed();
    }

    public interface AsyncListenerUser {
        void onStart();
        void onSuccess(User user);
        void onFailed();
    }

    public interface AsyncListenerMember {
        void onStart();
        void onSuccess(Member member);
        void onFailed();
    }

    public interface AsyncListenerEvent {
        void onStart();
        void onSuccess(Event event);
        void onFailed();
    }

    public interface AsyncListenerAlarmFBs {
        void onStart();
        void onSuccess(ArrayList<AlarmFB> map);
        void onFailed();
    }

    public interface AsyncListenerImage {
        void onStart();
        void onSuccess(byte[] image);
        void onFailed();
    }
}