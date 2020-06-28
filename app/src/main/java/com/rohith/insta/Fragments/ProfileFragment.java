package com.rohith.insta.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rohith.insta.AccountSettingsActivity;
import com.rohith.insta.Adapters.MyPostsAdapter;
import com.rohith.insta.Models.Notification;
import com.rohith.insta.Models.User;
import com.rohith.insta.Models.post;
import com.rohith.insta.R;
import com.rohith.insta.SignInActivity;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {


    private Button settings;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference UserRef;
    private DatabaseReference FollowRef;
    private DatabaseReference Posts;
    private TextView userName , FullName , Bio;
    private TextView posts , followers , following;
    private ImageButton gridit, saved;
    private CircleImageView circleImageView;
    private ImageView options;
    private String ProfileID;
    private int PostsCount = 0;
    private RecyclerView recyclerView;
    private List<post> list;
    private MyPostsAdapter myPostsAdapter;

    private List<String> mysaves;
    private RecyclerView recyclerView_saves;
    private List<post> list_saves;
    private MyPostsAdapter mysavesAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        settings=view.findViewById(R.id.account_settings);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        ProfileID = sharedPreferences.getString("profileID", "none");

        firebaseAuth = FirebaseAuth.getInstance();
        userName = view.findViewById(R.id.profileUserName);
        FullName = view.findViewById(R.id.fullNameProfile);
        Bio = view.findViewById(R.id.BioProfile);
        followers = view.findViewById(R.id.followersProfile);
        following = view.findViewById(R.id.followingProfile);
        posts = view.findViewById(R.id.postsProfile);
        circleImageView = view.findViewById(R.id.ProfileActImage);
        options = view.findViewById(R.id.optionsProfile);
        gridit = view.findViewById(R.id.GridView);
        saved = view.findViewById(R.id.SavedPhotos);

        recyclerView = view.findViewById(R.id.recycler_view_photos);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        list = new ArrayList<>();
        myPostsAdapter = new MyPostsAdapter(list , getContext());
        recyclerView.setAdapter(myPostsAdapter);


        recyclerView_saves = view.findViewById(R.id.recycler_view_saved);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new GridLayoutManager(getContext(), 3);
        recyclerView_saves.setLayoutManager(linearLayoutManager1);
        list_saves = new ArrayList<>();
        mysaves = new ArrayList<>();
        mysavesAdapter = new MyPostsAdapter(list_saves , getContext());
        recyclerView_saves.setAdapter(mysavesAdapter);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);


        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("button", "followers");
                editor.apply();
                ((FragmentActivity)getContext()).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.frag_container,
                        new FollowFragment()).commit();
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("button", "following");
                editor.apply();
                ((FragmentActivity)getContext()).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.frag_container,
                        new FollowFragment()).commit();
            }
        });

        gridit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        /**/
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(getContext(), SignInActivity.class));
                getActivity().finish();
            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (settings.getText().toString().equals("Edit Profile")){
                  startActivity(new Intent(getContext(), AccountSettingsActivity.class));
              } else if (settings.getText().toString().equals("Follow")){
                  FirebaseDatabase.getInstance().getReference().child("follow")
                          .child(firebaseAuth.getCurrentUser().getUid()).child("following")
                          .child(ProfileID).setValue(true)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  FirebaseDatabase.getInstance().getReference().child("follow")
                                          .child(ProfileID).child("followers")
                                          .child(firebaseAuth.getCurrentUser().getUid()).setValue(true);
                                  DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Notifications")
                                          .child(ProfileID);
                                  String notiKey = reference.push().getKey();
                                  HashMap<String,Object> notiMap = new HashMap<>();
                                  notiMap.put("notificationId",notiKey);
                                  notiMap.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                  notiMap.put("seen",false);
                                  notiMap.put("message"," started following you");
                                  reference.child(notiKey).setValue(notiMap);
                              }
                          });
                  settings.setText("Following");
              } else if (settings.getText().toString().equals("Following")){
                  FirebaseDatabase.getInstance().getReference().child("follow")
                          .child(firebaseAuth.getCurrentUser().getUid()).child("following")
                          .child(ProfileID).removeValue()
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  FirebaseDatabase.getInstance().getReference().child("follow")
                                          .child(ProfileID).child("followers")
                                          .child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                  final DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Notifications")
                                          .child(ProfileID);
                                  reference.addValueEventListener(new ValueEventListener() {
                                      @Override
                                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                          if(dataSnapshot.exists())
                                          {
                                              for(DataSnapshot snapshot:dataSnapshot.getChildren())
                                              {
                                                  Notification notification =snapshot.getValue(Notification.class);
                                                  if(notification.getPostID()==null&&
                                                          notification.getUserId()==
                                                                  FirebaseAuth.getInstance().getCurrentUser().getUid()
                                                          &&settings.getText() == "Follow")
                                                  {
                                                      reference.child(notification.getNotificationId()).removeValue();
                                                  }
                                              }
                                          }
                                      }

                                      @Override
                                      public void onCancelled(@NonNull DatabaseError databaseError) {

                                      }
                                  });
                              }
                          });
                  settings.setText("Follow");
              }
            }
        });
        options.setVisibility(View.VISIBLE);
        if (ProfileID.equals(firebaseAuth.getCurrentUser().getUid())){
            saved.setVisibility(View.VISIBLE);
            getUserDataSelf();
            settings.setText("Edit Profile");
            options.setVisibility(View.VISIBLE);
        } else {
            options.setVisibility(View.GONE);
            saved.setVisibility(View.GONE);
            checkFollowingStatus(ProfileID);
            getUserDataOther();

        }
        retrievePosts();
        getMySaves();
        return view;
    }

    private void getUserDataSelf() {
        UserRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getCurrentUser().getUid());
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
                    userName.setText(user.getUsername());
                    FullName.setText(user.getFullname());
                    if (!TextUtils.isEmpty(user.getBio())){
                        Bio.setText(user.getBio());
                    } else {Bio.setText("Insta User");}
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(circleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Posts = FirebaseDatabase.getInstance().getReference("Posts");
        Posts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        post posts = snapshot.getValue(post.class);
                        if (posts.getPostUser().equals(firebaseAuth.getCurrentUser().getUid())){
                            PostsCount++;
                        }
                    }
                    posts.setText(String.valueOf(PostsCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FollowRef = FirebaseDatabase.getInstance().getReference("follow");
        FollowRef.child(firebaseAuth.getCurrentUser().getUid()).child("followers").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            followers.setText(String.valueOf((int) dataSnapshot.getChildrenCount()));
                        } else {
                            followers.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        FollowRef.child(firebaseAuth.getCurrentUser().getUid()).child("following").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            following.setText(String.valueOf((int) dataSnapshot.getChildrenCount()));
                        } else {
                            following.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getUserDataOther() {
        UserRef = FirebaseDatabase.getInstance().getReference("Users").child(ProfileID);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
                    userName.setText(user.getUsername());
                    FullName.setText(user.getFullname());
                    if (!TextUtils.isEmpty(user.getBio())){
                        Bio.setText(user.getBio());
                    } else {Bio.setText("Insta User");}
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(circleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Posts = FirebaseDatabase.getInstance().getReference("Posts");
        Posts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        post posts = snapshot.getValue(post.class);
                        if (posts.getPostUser().equals(ProfileID)){
                            PostsCount++;
                        }
                    }
                    posts.setText(String.valueOf(PostsCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FollowRef = FirebaseDatabase.getInstance().getReference("follow");
        FollowRef.child(ProfileID).child("followers").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            followers.setText(String.valueOf((int) dataSnapshot.getChildrenCount()));
                        } else {
                            followers.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        FollowRef.child(ProfileID).child("following").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            following.setText(String.valueOf((int) dataSnapshot.getChildrenCount()));
                        } else {
                            following.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void getMySaves(){
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Saves")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            mysaves.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                mysaves.add(snapshot.getKey());
                            }
                            ShowSaves();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void ShowSaves(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        list_saves.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            post posts = snapshot.getValue(post.class);
                            for (String str : mysaves){
                                if (posts.getPostId().equals(str)){
                                    list_saves.add(posts);
                                }
                            }
                        }
                        mysavesAdapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollowingStatus(final String userId) {
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("follow")
                .child(firebaseAuth.getCurrentUser().getUid()).child("following");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists())
                {

                   settings.setText("Following");

                }
                else
                {
                    settings.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrievePosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    list.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        post posts = snapshot.getValue(post.class);
                        if (posts.getPostUser().equals(ProfileID)){
                            list.add(posts);
                        }
                    }
                }
                Collections.reverse(list);
                myPostsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
