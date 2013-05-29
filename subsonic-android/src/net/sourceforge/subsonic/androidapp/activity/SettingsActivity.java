/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.androidapp.activity;

import java.io.File;
import java.net.URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import net.sourceforge.subsonic.androidapp.R;
import net.sourceforge.subsonic.androidapp.provider.SearchSuggestionProvider;
import net.sourceforge.subsonic.androidapp.service.DownloadService;
import net.sourceforge.subsonic.androidapp.service.DownloadServiceImpl;
import net.sourceforge.subsonic.androidapp.service.MusicService;
import net.sourceforge.subsonic.androidapp.service.MusicServiceFactory;
import net.sourceforge.subsonic.androidapp.util.Constants;
import net.sourceforge.subsonic.androidapp.util.ErrorDialog;
import net.sourceforge.subsonic.androidapp.util.FileUtil;
import net.sourceforge.subsonic.androidapp.util.ModalBackgroundTask;
import net.sourceforge.subsonic.androidapp.util.ServerSettingsManager;
import net.sourceforge.subsonic.androidapp.util.Util;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private boolean testingConnection;
    private ListPreference videoPlayer;
    private ListPreference maxBitrateWifi;
    private ListPreference maxBitrateMobile;
    private ListPreference cacheSize;
    private EditTextPreference cacheLocation;
    private ListPreference preloadCount;
    private ServerSettingsManager serverSettingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        serverSettingsManager = new ServerSettingsManager(this);
        videoPlayer = (ListPreference) findPreference(Constants.PREFERENCES_KEY_VIDEO_PLAYER);
        maxBitrateWifi = (ListPreference) findPreference(Constants.PREFERENCES_KEY_MAX_BITRATE_WIFI);
        maxBitrateMobile = (ListPreference) findPreference(Constants.PREFERENCES_KEY_MAX_BITRATE_MOBILE);
        cacheSize = (ListPreference) findPreference(Constants.PREFERENCES_KEY_CACHE_SIZE);
        cacheLocation = (EditTextPreference) findPreference(Constants.PREFERENCES_KEY_CACHE_LOCATION);
        preloadCount = (ListPreference) findPreference(Constants.PREFERENCES_KEY_PRELOAD_COUNT);

        createServerSettings();

        findPreference("clearSearchHistory").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SettingsActivity.this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                Util.toast(SettingsActivity.this, R.string.settings_search_history_cleared);
                return false;
            }
        });

        SharedPreferences prefs = Util.getPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        update();
    }

    private void createServerSettings() {
        PreferenceCategory serverCategory = (PreferenceCategory)findPreference("servers");
        serverCategory.removeAll();

        for (final ServerSettingsManager.ServerSettings server : serverSettingsManager.getAllServers()) {
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
            screen.setTitle(server.getName());
            screen.setSummary(server.getUrl());

            final EditTextPreference name = new EditTextPreference(this);
            name.setKey(server.getNameKey());
            name.setTitle(R.string.settings_server_name);
            name.setText(server.getName());
            name.setSummary(server.getName());

            final EditTextPreference url = new EditTextPreference(this);
            url.setKey(server.getUrlKey());
            url.setTitle(R.string.settings_server_address);
            url.setText(server.getUrl());
            url.setSummary(server.getUrl());

            final EditTextPreference username = new EditTextPreference(this);
            username.setKey(server.getUsernameKey());
            username.setTitle(R.string.settings_server_username);
            username.setText(server.getUsername());
            username.setSummary(server.getUsername());

            EditTextPreference password = new EditTextPreference(this);
            password.setKey(server.getPasswordKey());
            password.setTitle(R.string.settings_server_username);
            password.setText(server.getPassword());
            password.setSummary("****");

            Preference testConnection = new Preference(this);
            testConnection.setPersistent(false);
            testConnection.setTitle(R.string.settings_test_connection_title);

            testConnection.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    testConnection(server);
                    return false;
                }
            });

            screen.addPreference(name);
            screen.addPreference(url);
            screen.addPreference(username);
            screen.addPreference(password);
            screen.addPreference(testConnection);
            serverCategory.addPreference(screen);

            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    name.setSummary((String) value);
                    return true;
                }
            });

            url.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String urlString;
                    try {
                        urlString = (String) value;
                        new URL(urlString);
                        if (!urlString.equals(urlString.trim()) || urlString.contains("@") || urlString.contains("_")) {
                            throw new Exception();
                        }
                    } catch (Exception x) {
                        new ErrorDialog(SettingsActivity.this, R.string.settings_invalid_url, false);
                        return false;
                    }
                    url.setSummary(urlString);
                    return true;
                }
            });

            username.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String user = (String) value;
                    if (user == null || !user.equals(user.trim())) {
                        new ErrorDialog(SettingsActivity.this, R.string.settings_invalid_username, false);
                        return false;
                    }
                    username.setSummary(user);
                    return true;
                }
            });

            // TODO: Add "Delete"
            // TODO: Add "Add server"
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = Util.getPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

     // TODO: Can this be done more fine-grained?

        Log.d(TAG, "Preference changed: " + key);
        update();

        if (Constants.PREFERENCES_KEY_HIDE_MEDIA.equals(key)) {
            setHideMedia(sharedPreferences.getBoolean(key, false));
        }
        else if (Constants.PREFERENCES_KEY_MEDIA_BUTTONS.equals(key)) {
            setMediaButtonsEnabled(sharedPreferences.getBoolean(key, true));
        }
        else if (Constants.PREFERENCES_KEY_CACHE_LOCATION.equals(key)) {
            setCacheLocation(sharedPreferences.getString(key, ""));
        }
    }

    private void update() {
        if (testingConnection) {
            return;
        }

        videoPlayer.setSummary(videoPlayer.getEntry());
        maxBitrateWifi.setSummary(maxBitrateWifi.getEntry());
        maxBitrateMobile.setSummary(maxBitrateMobile.getEntry());
        cacheSize.setSummary(cacheSize.getEntry());
        cacheLocation.setSummary(cacheLocation.getText());
        preloadCount.setSummary(preloadCount.getEntry());

        createServerSettings();
    }

    private void setHideMedia(boolean hide) {
        File nomediaDir = new File(FileUtil.getSubsonicDirectory(), ".nomedia");
        if (hide && !nomediaDir.exists()) {
            if (!nomediaDir.mkdir()) {
                Log.w(TAG, "Failed to create " + nomediaDir);
            }
        } else if (nomediaDir.exists()) {
            if (!nomediaDir.delete()) {
                Log.w(TAG, "Failed to delete " + nomediaDir);
            }
        }
        Util.toast(this, R.string.settings_hide_media_toast, false);
    }

    private void setMediaButtonsEnabled(boolean enabled) {
        if (enabled) {
            Util.registerMediaButtonEventReceiver(this);
        } else {
            Util.unregisterMediaButtonEventReceiver(this);
        }
    }

    private void setCacheLocation(String path) {
        File dir = new File(path);
        if (!FileUtil.ensureDirectoryExistsAndIsReadWritable(dir)) {
            Util.toast(this, R.string.settings_cache_location_error, false);

            // Reset it to the default.
            String defaultPath = FileUtil.getDefaultMusicDirectory().getPath();
            if (!defaultPath.equals(path)) {
                SharedPreferences prefs = Util.getPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.PREFERENCES_KEY_CACHE_LOCATION, defaultPath);
                editor.commit();
                cacheLocation.setSummary(defaultPath);
                cacheLocation.setText(defaultPath);
            }

            // Clear download queue.
            DownloadService downloadService = DownloadServiceImpl.getInstance();
            downloadService.clear();
        }
    }

    private void testConnection(final ServerSettingsManager.ServerSettings server) {
        ModalBackgroundTask<Boolean> task = new ModalBackgroundTask<Boolean>(this, false) {
            private int previousActive;

            @Override
            protected Boolean doInBackground() throws Throwable {
                updateProgress(R.string.settings_testing_connection);

                previousActive = serverSettingsManager.getActiveServer().getId();
                testingConnection = true;
                serverSettingsManager.setActiveServerId(server.getId());
                try {
                    MusicService musicService = MusicServiceFactory.getMusicService(SettingsActivity.this);
                    musicService.ping(SettingsActivity.this, this);
                    return musicService.isLicenseValid(SettingsActivity.this, null);
                } finally {
                    serverSettingsManager.setActiveServerId(previousActive);
                    testingConnection = false;
                }
            }

            @Override
            protected void done(Boolean licenseValid) {
                if (licenseValid) {
                    Util.toast(SettingsActivity.this, R.string.settings_testing_ok);
                } else {
                    Util.toast(SettingsActivity.this, R.string.settings_testing_unlicensed);
                }
            }

            @Override
            protected void cancel() {
                super.cancel();
                Util.setActiveServer(SettingsActivity.this, previousActive);
            }

            @Override
            protected void error(Throwable error) {
                Log.w(TAG, error.toString(), error);
                new ErrorDialog(SettingsActivity.this, getResources().getString(R.string.settings_connection_failure) +
                        " " + getErrorMessage(error), false);
            }
        };
        task.execute();
    }
}