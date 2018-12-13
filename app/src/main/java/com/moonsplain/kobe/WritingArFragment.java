/*
WritingArFragment.java

An extension of ArFragment that allows writing files to phone's storage.

Author: Kobe
 */

package com.moonsplain.kobe;

import android.Manifest;

import com.google.ar.sceneform.ux.ArFragment;

public class WritingArFragment extends ArFragment {
    @Override
    public String[] getAdditionalPermissions() {        //Method to get permission from user to write
        String[] additionalPermissions = super.getAdditionalPermissions();      //files to phone's storage.
        int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
        String[] permissions = new String[permissionLength + 1];
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 1, additionalPermissions.length);
        }
        return permissions;
    }
}
