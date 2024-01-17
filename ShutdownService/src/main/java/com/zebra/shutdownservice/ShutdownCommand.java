package com.zebra.shutdownservice;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.zebra.emdkprofilemanagerhelper.IResultCallbacks;
import com.zebra.emdkprofilemanagerhelper.ProfileManagerCommand;

import java.util.Base64;

public class ShutdownCommand {
    public static void shutdown(Context context, IResultCallbacks callbackInterface) {
        String profileName = "PowerMgr-1";
        String profileData = "";
        try {
            profileData =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                            "<characteristic type=\"Profile\">" +
                            "<parm name=\"ProfileName\" value=\"" + profileName + "\"/>" +
                            "  <characteristic version=\"13.1\" type=\"PowerMgr\">\n" +
                            "    <parm name=\"ResetAction\" value=\"15\" />\n" +
                            "  </characteristic>" +
                            "</characteristic>";

            ProfileManagerCommand profileManagerCommand = new ProfileManagerCommand(context);
            profileManagerCommand.execute(profileData, profileName, callbackInterface);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            if (callbackInterface != null) {
                callbackInterface.onError("Error on profile: " + profileName + "\nError:" + e.getLocalizedMessage() + "\nProfileData:" + profileData, "");
            }
        }
    }
}
