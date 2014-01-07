package com.example.scavengerhunt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditGame extends Activity {

    private List<ParseUser> playerList = new ArrayList<ParseUser>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);
        setupButtonCallbacks();
        getGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_game, menu);
        return true;
    }

    public void onResume() {
        super.onResume();
    }

    public void setGameInfo(ParseObject game) {
        initializeListViews(game);
        populateDateTimeFields(game);

        EditText gameName = (EditText) findViewById(R.id.edit_gameName);
        gameName.setText(game.getString("name"));

        setItemList(game);
    }

    private void setupButtonCallbacks() {
        final Button updateGameButton = (Button) findViewById(R.id.button_update);
        final Button addItemButton = (Button) findViewById(R.id.button_editAddItem);
        updateGameButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doUpdateGame();
            }
        });

        addItemButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showItemAddDialog();
            }
        });
    }

    private void doUpdateGame() {
        Bundle extras = getIntent().getExtras();
        final String gameId = extras.getString("gameId");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        final ParseUser currentUser = ParseUser.getCurrentUser();
        query.getInBackground(gameId, new GetCallback<ParseObject>() {
            public void done(final ParseObject game, ParseException e) {
                if (e == null) {
                    game.put("name", getGameName());
                    game.put("start_datetime", getStartDateTime());
                    game.put("end_datetime", getEndDateTime());
                    game.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d("Game Update", "Game Updated!");
                                updateGamePlayers(game);
                                updateGameItems(game);
                                sendPushInvitation(game, currentUser);
                                showToast("Game Updated!");
                                launchGameView(game.getObjectId());
                            } else {
                                Log.d("Game Creation", "Error creating game: "
                                        + e);
                            }
                        }
                    });
                }
            }
        });
    }

    private void launchGameView(String gameId) {
        Intent intent = new Intent(EditGame.this, ViewGame.class);
        intent.putExtra("gameId", gameId);
        Log.d("GameId", "game id is " + gameId);
        startActivity(intent);
    }

    private void updateGameItems(ParseObject game) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GameItems");
        query.whereEqualTo("game", game);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject gameItems, ParseException e) {
                if (e == null) {
                    gameItems.put("items", getItemList());
                    gameItems.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d("Save", "Items Saved");
                            } else {
                                Log.d("Save", "Error saving gameItem: " + e);
                            }
                        }

                    });
                }
            }

        });
    }
    
    private ArrayList<String> getItemList() {
        ArrayList<String> itemList = new ArrayList<String>();
        ArrayAdapter<String> adapter = getItemAdapter();
        for (int i = 0; i < (adapter.getCount()); i++) {
            itemList.add(adapter.getItem(i));
        }
        return itemList;

    }

    private void updateGamePlayers(ParseObject game) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GamePlayers");
        query.whereEqualTo("game", game);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject gamePlayers, ParseException e) {
                if (e == null) {
                    gamePlayers.put("users", getChosenPlayerList());
                    gamePlayers.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d("Save", "gamePlayer data saved!");
                            } else {
                                Log.d("Save", "Error saving gamePlayer: " + e);
                            }
                        }

                    });
                }
            }

        });

    }

    private void showToast(String message) {
        ScavengerHuntApplication.getInstance()
                .showToast(EditGame.this, message);
    }

    private List<ParseUser> getChosenPlayerList() {
        final List<ParseUser> chosenPlayers = new ArrayList<ParseUser>();
        final ListView playerListView = (ListView) findViewById(R.id.listview_players);
        final SparseBooleanArray checkedItems = playerListView
                .getCheckedItemPositions();
        if (checkedItems != null) {
            for (int i = 0; i < checkedItems.size(); i++) {
                if (checkedItems.valueAt(i)) {
                    chosenPlayers.add(playerList.get(checkedItems.keyAt(i)));
                }
            }
        }
        return chosenPlayers;
    }

    private String getGameName() {
        return getUserInput(R.id.edit_gameName);
    }

    private Date getStartDateTime() {
        return convertToDateTime(getUserInput(R.id.editStartDate) + " "
                + getUserInput(R.id.editStartTime));
    }

    private Date getEndDateTime() {
        return convertToDateTime(getUserInput(R.id.editEndDate) + " "
                + getUserInput(R.id.editEndTime));
    }

    private String getUserInput(int id) {
        EditText input = (EditText) findViewById(id);
        return input.getText().toString();
    }

    private static Date convertToDateTime(String dateString) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM-dd-yyyy h:mm a", Locale.US);
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public void showStartDatePickerDialog(View v) {
        final DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "startDatePicker");
    }

    public void showStartTimePickerDialog(View v) {
        final DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "startTimePicker");
    }

    public void showEndDatePickerDialog(View v) {
        final DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "endDatePicker");
    }

    public void showEndTimePickerDialog(View v) {
        final DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "endTimePicker");
    }

    private void showItemAddDialog() {
        final DialogFragment itemAddDialogFragment = new ItemAddDialogFragment();
        itemAddDialogFragment.show(getFragmentManager(), "editGameItems");
    }

    private void addItemToList(String item, ArrayAdapter<String> adapter) {
        adapter.add(item);
        adapter.notifyDataSetChanged();
    }

    public void onFinishItemDialog(String item) {
        addItemToList(item, getItemAdapter());
    }

    private void populateDateTimeFields(ParseObject game) {
        EditText startDateView = (EditText) findViewById(R.id.editStartDate);
        EditText startTimeView = (EditText) findViewById(R.id.editStartTime);
        EditText endDateView = (EditText) findViewById(R.id.editEndDate);
        EditText endTimeView = (EditText) findViewById(R.id.editEndTime);

        startDateView.setText(getGameDate(game.getDate("start_datetime")));
        endDateView.setText(getGameDate(game.getDate("end_datetime")));
        startTimeView.setText(getGameTime(game.getDate("start_datetime")));
        endTimeView.setText(getGameTime(game.getDate("end_datetime")));
    }

    private String getGameDate(Date date) {
        return convertDatetimeToDate(date);

    }

    private String getGameTime(Date date) {
        return convertDatetimeToTime(date);
    }

    private String convertDatetimeToTime(Date dateTime) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a",
                Locale.US);
        String convertedTime = new String();
        convertedTime = dateFormat.format(dateTime);
        return convertedTime;
    }

    private String convertDatetimeToDate(Date dateTime) {

        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy",
                Locale.US);

        String convertedDate = new String();
        convertedDate = dateFormat.format(dateTime);
        return convertedDate;
    }

    private void initializeListViews(ParseObject game) {
        setupPlayerListView(game);
        setupItemListView();
    }

    private void setupPlayerListView(ParseObject game) {
        getGamePlayers(game);

    }

    private void getGamePlayers(ParseObject game) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GamePlayers");
        query.whereEqualTo("game", game);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject players, ParseException e) {
                if (e == null) {
                    Log.d("User List", "Retrieved " + players.toString());
                    getPlayers(players);
                } else {
                    Log.w("Parse Error", "game retreival failure");
                }
            }
        });
    }

    private void getPlayers(ParseObject gamePlayers) {
        String[] gamePlayerIds = new String[gamePlayers.getJSONArray("users")
                .length()];
        JSONArray userInfo = gamePlayers.getJSONArray("users");
        for (int i = 0; i < userInfo.length(); i++) {
            JSONObject user = null;
            try {
                user = userInfo.getJSONObject(i);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            String userId = null;
            try {
                userId = user.getString("objectId");
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            gamePlayerIds[i] = userId;
        }

        final ArrayList<ParseUser> players = new ArrayList<ParseUser>();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn("objectId", Arrays.asList(gamePlayerIds));
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    for (ParseUser user : users) {
                        Log.d("Parse Username",
                                "Retrieved User " + user.getString("username"));
                        players.add(user);
                    }
                } else {
                    Log.w("Parse Error", "player username retreival failure");
                }
            }
        });
        setParseUserList(players);
    }

    private void sendPushInvitation(ParseObject game, ParseUser currentUser) {

        for (ParseUser player : getChosenPlayerList()) {
            ParseQuery<ParseInstallation> pushQuery = ParseInstallation
                    .getQuery();
            pushQuery.whereEqualTo("owner", player);

            Log.d("push player", player.getString("username"));

            // Send push notification to query
            ParsePush push = new ParsePush();
            push.setQuery(pushQuery);
            push.setMessage("You've been invited to play in the game "
                    + game.getString("name") + " by "
                    + currentUser.getString("username") + ".");
            push.sendInBackground();
        }
    }

    private void setParseUserList(final ArrayList<ParseUser> gamePlayerList) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.selectKeys(Arrays.asList("username"));
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, ParseException e) {
                if (e == null) {
                    String[] usernameList = new String[userList.size()];
                    Log.d("User List", "Retrieved " + userList.size());
                    for (int i = 0; i < userList.size(); i++) {
                        Log.d("Username data", "Retrieved User: "
                                + userList.get(i).getString("username"));
                        usernameList[i] = userList.get(i).getString("username");
                    }
                    playerList = userList;
                    setupUsernameListView(usernameList,
                            findUserMatch(userList, gamePlayerList));
                } else {
                    Log.w("error", "game retreival failure");
                }
            }
        });

    }

    private int[] findUserMatch(final List<ParseUser> userList,
            final ArrayList<ParseUser> gamePlayerList) {
        int[] gamePlayerPositions = new int[gamePlayerList.size()];
        for (int a = 0; a < gamePlayerList.size(); a++) {
            for (int i = 0; i < userList.size(); i++) {
                String userId = userList.get(i).getObjectId();
                String playerId = gamePlayerList.get(a).getObjectId();
                Log.d("user comparison",
                        gamePlayerList.get(a).getString("username") + " : "
                                + userList.get(i).getString("username"));

                if (userId.equals(playerId)) {
                    Log.d("user comparison", "match found");
                    gamePlayerPositions[a] = i;
                    continue;
                }
            }
        }
        return gamePlayerPositions;
    }

    private void setupUsernameListView(String[] usernameList,
            int[] gamePlayerPositions) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, usernameList);
        final ListView playerListView = (ListView) findViewById(R.id.listview_players);
        playerListView.setAdapter(adapter);
        playerListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        for (int position : gamePlayerPositions) {
            playerListView.setItemChecked(position, true);
        }
    }

    private void setItemList(ParseObject game) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GameItems");
        query.whereEqualTo("game", game);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject gameItems, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < gameItems.getJSONArray("items")
                            .length(); i++) {
                        try {
                            addToListView(gameItems.getJSONArray("items")
                                    .getString(i), getItemAdapter());
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                } else {
                    Log.w("Parse Error", "game items retreival failure");
                }
            }
        });

    }

    private void setupItemListView() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        final ListView itemListView = (ListView) findViewById(R.id.listview_items);
        itemListView.setAdapter(adapter);
        itemListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            final View view, int position, long id) {
                        final String item = (String) parent
                                .getItemAtPosition(position);

                        adapter.remove(item);
                        adapter.notifyDataSetChanged();
                        view.setAlpha(1);
                    }
                });

    }

    private void addToListView(String item, ArrayAdapter<String> adapter) {
        adapter.add(item);
        adapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> getItemAdapter() {
        final ListView itemListView = (ListView) findViewById(R.id.listview_items);
        final ArrayAdapter<String> adapter = (ArrayAdapter<String>) itemListView
                .getAdapter();
        return adapter;
    }

    private void getGame() {
        Bundle extras = getIntent().getExtras();
        String gameId = extras.getString("gameId");
        Log.d("Game Info", "Game View ID is " + gameId);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.getInBackground(gameId, new GetCallback<ParseObject>() {
            public void done(ParseObject game, ParseException e) {
                if (e == null) {
                    Log.d("Game Info", "Game name is " + game.getString("name"));
                    setGameInfo(game);
                } else {
                    Log.w("error", "game retreival failure");
                    setGameInfo(game);
                }
            }
        });
    }

}
