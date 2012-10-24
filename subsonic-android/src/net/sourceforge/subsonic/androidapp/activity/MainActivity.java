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

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import net.sourceforge.subsonic.androidapp.R;
import net.sourceforge.subsonic.androidapp.billing.BillingConstants;
import net.sourceforge.subsonic.androidapp.billing.BillingService;
import net.sourceforge.subsonic.androidapp.billing.PurchaseMode;
import net.sourceforge.subsonic.androidapp.billing.PurchaseObserver;
import net.sourceforge.subsonic.androidapp.billing.ResponseHandler;
import net.sourceforge.subsonic.androidapp.service.DownloadService;
import net.sourceforge.subsonic.androidapp.service.DownloadServiceImpl;
import net.sourceforge.subsonic.androidapp.util.Constants;
import net.sourceforge.subsonic.androidapp.util.FileUtil;
import net.sourceforge.subsonic.androidapp.util.MergeAdapter;
import net.sourceforge.subsonic.androidapp.util.PopupMenuHelper;
import net.sourceforge.subsonic.androidapp.util.Util;

public class MainActivity extends SubsonicTabActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MENU_GROUP_SERVER = 10;
    private static final int MENU_ITEM_SERVER_1 = 101;
    private static final int MENU_ITEM_SERVER_2 = 102;
    private static final int MENU_ITEM_SERVER_3 = 103;

    private static boolean infoDialogDisplayed;

    private SubsonicPurchaseObserver purchaseObserver;
    private BillingService billingService;
    private TextView purchaseButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(Constants.INTENT_EXTRA_NAME_EXIT)) {
            exit();
        }
        setContentView(R.layout.main);

        loadSettings();

        View buttons = LayoutInflater.from(this).inflate(R.layout.main_buttons, null);

        final View serverButton = buttons.findViewById(R.id.main_select_server);
        final TextView serverTextView = (TextView) serverButton.findViewById(R.id.main_select_server_2);

        final TextView offlineButton = (TextView) buttons.findViewById(R.id.main_offline);
        offlineButton.setText(Util.isOffline(this) ? R.string.main_use_connected : R.string.main_use_offline);

        purchaseButton = (TextView) buttons.findViewById(R.id.main_purchase);
        updatePurchaseButtonVisibility();

        final View albumsTitle = buttons.findViewById(R.id.main_albums);
        final View albumsNewestButton = buttons.findViewById(R.id.main_albums_newest);
        final View albumsRandomButton = buttons.findViewById(R.id.main_albums_random);
        final View albumsHighestButton = buttons.findViewById(R.id.main_albums_highest);
        final View albumsRecentButton = buttons.findViewById(R.id.main_albums_recent);
        final View albumsFrequentButton = buttons.findViewById(R.id.main_albums_frequent);

        final View dummyView = findViewById(R.id.main_dummy);

        int instance = Util.getActiveServer(this);
        String name = Util.getServerName(this, instance);
        serverTextView.setText(name);

        ListView list = (ListView) findViewById(R.id.main_list);

        MergeAdapter adapter = new MergeAdapter();

        adapter.addView(offlineButton, true);
        if (!Util.isOffline(this)) {
            adapter.addView(serverButton, true);
            if (Util.getAdRemovalPurchaseMode(this).shouldPurchaseButtonBeVisible()) {
                adapter.addView(purchaseButton, true);
            }
            adapter.addView(albumsTitle, false);
            adapter.addViews(Arrays.asList(albumsNewestButton, albumsRandomButton, albumsHighestButton, albumsRecentButton, albumsFrequentButton), true);
        }
        list.setAdapter(adapter);
        registerForContextMenu(dummyView);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view == offlineButton) {
                    toggleOffline();
                } else if (view == serverButton) {
                    dummyView.showContextMenu();
                } else if (view == purchaseButton) {
                    purchaseAdRemoval();
                } else if (view == albumsNewestButton) {
                    showAlbumList("newest");
                } else if (view == albumsRandomButton) {
                    showAlbumList("random");
                } else if (view == albumsHighestButton) {
                    showAlbumList("highest");
                } else if (view == albumsRecentButton) {
                    showAlbumList("recent");
                } else if (view == albumsFrequentButton) {
                    showAlbumList("frequent");
                }
            }
        });

        purchaseObserver = new SubsonicPurchaseObserver(new Handler());
        ResponseHandler.register(purchaseObserver);

        billingService = new BillingService();
        billingService.setContext(this);

        // Check if billing is supported.
        billingService.checkBillingSupported(BillingConstants.ITEM_TYPE_SUBSCRIPTION);

        // Title: Subsonic
        setTitle(R.string.common_appname);

        // Button 1: gone
        ImageButton actionShuffleButton = (ImageButton)findViewById(R.id.action_button_1);
        actionShuffleButton.setImageResource(R.drawable.action_shuffle);
        actionShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startShufflePlay();
            }
        });

        // Button 2: search
        ImageButton actionSearchButton = (ImageButton)findViewById(R.id.action_button_2);
        actionSearchButton.setImageResource(R.drawable.action_search);
        actionSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchRequested();
            }
        });

        // Button 3: overflow
        final View overflowButton = findViewById(R.id.action_button_3);
        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PopupMenuHelper().showMenu(MainActivity.this, overflowButton, R.menu.main);
            }
        });

        showInfoDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ResponseHandler.register(purchaseObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ResponseHandler.unregister(purchaseObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billingService.unbind();
    }

    private void startShufflePlay() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.main_shuffle_confirm)
                .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                        intent.putExtra(Constants.INTENT_EXTRA_NAME_SHUFFLE, true);
                        Util.startActivityWithoutTransition(MainActivity.this, intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void purchaseAdRemoval() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.main_purchase)
                .setMessage(R.string.main_purchase_confirm)
                .setPositiveButton(R.string.common_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        billingService.requestPurchase(Constants.PRODUCT_ID_AD_REMOVAL, BillingConstants.ITEM_TYPE_INAPP, null);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void updatePurchaseButtonVisibility() {
        boolean purchaseEnabled = Util.getAdRemovalPurchaseMode(MainActivity.this).shouldPurchaseButtonBeVisible();
        purchaseButton.setVisibility(purchaseEnabled ? View.VISIBLE : View.GONE);
    }

    private void loadSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        SharedPreferences prefs = Util.getPreferences(this);
        if (!prefs.contains(Constants.PREFERENCES_KEY_CACHE_LOCATION)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREFERENCES_KEY_CACHE_LOCATION, FileUtil.getDefaultMusicDirectory().getPath());
            editor.commit();
        }

        if (!prefs.contains(Constants.PREFERENCES_KEY_OFFLINE)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.PREFERENCES_KEY_OFFLINE, false);
            editor.putInt(Constants.PREFERENCES_KEY_SERVER_INSTANCE, 1);
            editor.commit();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        MenuItem menuItem1 = menu.add(MENU_GROUP_SERVER, MENU_ITEM_SERVER_1, MENU_ITEM_SERVER_1, Util.getServerName(this, 1));
        MenuItem menuItem2 = menu.add(MENU_GROUP_SERVER, MENU_ITEM_SERVER_2, MENU_ITEM_SERVER_2, Util.getServerName(this, 2));
        MenuItem menuItem3 = menu.add(MENU_GROUP_SERVER, MENU_ITEM_SERVER_3, MENU_ITEM_SERVER_3, Util.getServerName(this, 3));
        menu.setGroupCheckable(MENU_GROUP_SERVER, true, true);
        menu.setHeaderTitle(R.string.main_select_server);

        switch (Util.getActiveServer(this)) {
            case 1:
                menuItem1.setChecked(true);
                break;
            case 2:
                menuItem2.setChecked(true);
                break;
            case 3:
                menuItem3.setChecked(true);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case MENU_ITEM_SERVER_1:
                setActiveServer(1);
                break;
            case MENU_ITEM_SERVER_2:
                setActiveServer(2);
                break;
            case MENU_ITEM_SERVER_3:
                setActiveServer(3);
                break;
            default:
                return super.onContextItemSelected(menuItem);
        }

        // Restart activity
        restart();
        return true;
    }

    private void toggleOffline() {
        Util.setOffline(this, !Util.isOffline(this));
        restart();
    }

    private void setActiveServer(int instance) {
        if (Util.getActiveServer(this) != instance) {
            DownloadService service = getDownloadService();
            if (service != null) {
                service.clearIncomplete();
            }
            Util.setActiveServer(this, instance);
        }
    }

    private void restart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Util.startActivityWithoutTransition(this, intent);
    }

    private void exit() {
        stopService(new Intent(this, DownloadServiceImpl.class));
        finish();
    }

    private void showInfoDialog() {
        if (!infoDialogDisplayed) {
            infoDialogDisplayed = true;
            if (Util.getRestUrl(this, null).contains("demo.subsonic.org")) {
                Util.info(this, R.string.main_welcome_title, R.string.main_welcome_text);
            }
        }
    }

    private void showAlbumList(String type) {
        Intent intent = new Intent(this, SelectAlbumActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_TYPE, type);
        intent.putExtra(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_SIZE, 20);
        intent.putExtra(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_OFFSET, 0);
		Util.startActivityWithoutTransition(this, intent);
	}

    /**
     * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
     * messages to this application so that we can update the UI.
     */
    private class SubsonicPurchaseObserver extends PurchaseObserver {

        public SubsonicPurchaseObserver(Handler handler) {
            super(MainActivity.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {
            if (!supported) {
                Util.setAdRemovalPurchaseMode(MainActivity.this, PurchaseMode.NOT_SUPPORTED);
            } else if (Util.getAdRemovalPurchaseMode(MainActivity.this) == PurchaseMode.NOT_SUPPORTED) {
                Util.setAdRemovalPurchaseMode(MainActivity.this, PurchaseMode.UNKNOWN);
            }

            // Request restore if this is the first time the app is run.
            if (supported && Util.getAdRemovalPurchaseMode(MainActivity.this).shouldRestoreTransactions()) {
                billingService.restoreTransactions();
            }
        }

        @Override
        public void onPurchaseStateChange(BillingConstants.PurchaseState purchaseState, String productId, long purchaseTime, String developerPayload) {
            Log.i(TAG, "onPurchaseStateChange: " + productId + ": " + purchaseState);
            if (Constants.PRODUCT_ID_AD_REMOVAL.equals(productId) && BillingConstants.PurchaseState.PURCHASED.equals(purchaseState)) {
                Util.setAdRemovalPurchaseMode(MainActivity.this, PurchaseMode.PURCHASED);
                updatePurchaseButtonVisibility();
            }
        }

        @Override
        public void onRequestPurchaseResponse(BillingService.RequestPurchase request, BillingConstants.ResponseCode responseCode) {
            Log.i(TAG, "onRequestPurchaseResponse: " + request.productId + ": " + responseCode);
        }

        @Override
        public void onRestoreTransactionsResponse(BillingService.RestoreTransactions request, BillingConstants.ResponseCode responseCode) {
            Log.i(TAG, "onRestoreTransactionsResponse: " + responseCode);
            if (responseCode == BillingConstants.ResponseCode.RESULT_OK) {

                // Update the shared preferences so that we don't perform a RestoreTransactions again.
                if (Util.getAdRemovalPurchaseMode(MainActivity.this).shouldRestoreTransactions()) {
                    Util.setAdRemovalPurchaseMode(MainActivity.this, PurchaseMode.NOT_PURCHASED);
                }
            }
            updatePurchaseButtonVisibility();
        }
    }
}