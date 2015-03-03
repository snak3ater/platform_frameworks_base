/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.android.systemui.qs.tiles;
 
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
 
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
 
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
 
public class HeadsupTile extends QSTile<QSTile.BooleanState> {
    private boolean mListening;
    private HeadsupObserver mObserver;
 
    public HeadsupTile(Host host) {
        super(host);
        mObserver = new HeadsupObserver(mHandler);
    }
 
    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }
 
    @Override
    protected void handleClick() {
        toggleState();
        refreshState();
        mHost.collapsePanels(); /* dismissShade */
        toast();
    }
 
     @Override
    protected void handleSecondaryClick() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings",
            "com.android.settings.Settings$HeadsUpSettingsActivity");
        mHost.startSettingsActivity(intent);
    }
 
    @Override
    public void handleLongClick() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings",
            "com.android.settings.Settings$HeadsUpSettingsActivity");
        mHost.startSettingsActivity(intent);
    }
 
    protected void toggleState() {
         Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HEADS_UP_USER_ENABLED, !getUserHeadsUpState() ? 1 : 0);
    }
 
 
    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        if (getUserHeadsUpState()) {
            state.iconId = R.drawable.ic_qs_ringer_audible;
            state.label = mContext.getString(R.string.quick_settings_heads_up_on);
    } else {
            state.iconId = R.drawable.ic_qs_zen_on;
            state.label = mContext.getString(R.string.quick_settings_heads_up_off);
        }
    }
 
    private boolean getUserHeadsUpState() {
         return Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.HEADS_UP_USER_ENABLED,
                Settings.System.HEADS_UP_USER_ON,
                UserHandle.USER_CURRENT) != 0;
    }
 
    @Override
    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            mObserver.startObserving();
        } else {
            mObserver.endObserving();
        }
    }
 
    protected void toast() {
    /* show a toast */
        String enabled = mContext.getString(R.string.heads_up_enabled);
        String disabled = mContext.getString(R.string.heads_up_disabled);
        int duration = Toast.LENGTH_SHORT;
        if (getUserHeadsUpState()) {
            Toast toast = Toast.makeText(mContext, enabled, duration);
            toast.show();
        } else {
            Toast toast = Toast.makeText(mContext, disabled, duration);
            toast.show();
        }
    }
 
    private class HeadsupObserver extends ContentObserver {
        public HeadsupObserver(Handler handler) {
            super(handler);
        }
 
        @Override
        public void onChange(boolean selfChange) {
            refreshState();
        }
 
        public void startObserving() {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.HEADS_UP_USER_ENABLED),
                    false, this);
        }
 
        public void endObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}

