package com.example.scavengerhunt;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MyGamesList extends Activity {

    ParseUser currentUser = ParseUser.getCurrentUser();
    List<ParseObject> myGames;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_games_list);
        setMyGamesListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_games_list, menu);
        return true;
    }

    private void setMyGamesListView() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        final ListView myGamesListview = (ListView) findViewById(R.id.listview_myGames);
        myGamesListview.setAdapter(adapter);
        findMyCreatedGames();
        myGamesListview
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            final View view, int position, long id) {
                        final ParseObject game = myGames.get(position);
                        launchGameView(game.getObjectId());

                    }
                });

    }

    private void launchGameView(String gameId) {
        Intent intent = new Intent(MyGamesList.this, ViewGame.class);
        intent.putExtra("gameId", gameId);
        Log.d("GameId", "game id is " + gameId);
        startActivity(intent);
    }

    private void addToListView(ParseObject game, ArrayAdapter<String> adapter) {
        adapter.add(game.getString("name"));
        adapter.notifyDataSetChanged();
    }

    private void findMyCreatedGames() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("creator", currentUser);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> games, ParseException e) {
                if (e == null) {
                    for (final ParseObject game : games) {
                        Log.d("Game Info",
                                "Game name is " + game.getString("name"));
                        addToListView(game, getMyGamesAdapter());
                    }
                    myGames = games;
                } else {
                    Log.w("error", "game retreival failure");
                }
            }
        });
    }

    private ArrayAdapter<String> getMyGamesAdapter() {
        final ListView myGamesListView = (ListView) findViewById(R.id.listview_myGames);
        final ArrayAdapter<String> adapter = (ArrayAdapter<String>) myGamesListView
                .getAdapter();
        return adapter;
    }
}
