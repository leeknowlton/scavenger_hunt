package com.example.scavengerhunt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.example.scavengerhunt.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ViewGame extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGame();
        setContentView(R.layout.activity_view_game);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_game, menu);
        return true;
    }
    
    public void onResume() {
        super.onResume();
    }
    
    public void setGameInfo(ParseObject game) {
        TextView gameName = (TextView) findViewById(R.id.editGameName);
        gameName.setText(game.getString("name"));
    }

    public void getGame() {
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
