package edu.sdu.rnyati.hometownchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by raghavnyati on 4/2/17.
 */

public class Utility {

    public static ProgressDialog mProgressDialog;


    public static String[] convertToArray(String listString)
    {
        String[] list = listString.split("\",\"");
        String[] realList = new String[list.length+1];
        realList[0]=" ";
        int lastElement = list.length-1;

        list[0]=list[0].substring(2);
        list[lastElement]=list[lastElement].substring(0,list[lastElement].length()-2);

        for (int i=1;i<list.length;i++) {
            realList[i]=list[i-1];
        }
        return list;
    }


    public static void createDialog(String title, String message, boolean isError, Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(isError){
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        }else{
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    //Successful posting value
                }
            });
        }
        builder.setMessage(message);
        builder.setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public static void showProgressDialog(Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Loading..");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static boolean checkIsNullOrIsEmpty(String string){
        if( null != string && !string.isEmpty()){
            return true;
        }
        return  false;
    }

}
