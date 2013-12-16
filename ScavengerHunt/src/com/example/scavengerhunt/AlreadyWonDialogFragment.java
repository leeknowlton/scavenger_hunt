package com.example.scavengerhunt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class AlreadyWonDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alreadyWon)
               .setPositiveButton("Return to Game List", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       Intent intent = new Intent(getActivity(), InvitedGamesList.class);
                       startActivity(intent);
                   }
               });
        return builder.create();
    }
    
}
