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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import net.sourceforge.subsonic.androidapp.R;
import net.sourceforge.subsonic.androidapp.domain.MusicDirectory;
import net.sourceforge.subsonic.androidapp.service.DownloadFile;
import net.sourceforge.subsonic.androidapp.service.MusicService;
import net.sourceforge.subsonic.androidapp.service.MusicServiceFactory;
import net.sourceforge.subsonic.androidapp.util.EntryAdapter;
import net.sourceforge.subsonic.androidapp.util.Pair;
import net.sourceforge.subsonic.androidapp.util.PopupMenuHelper;
import net.sourceforge.subsonic.androidapp.util.TabActivityBackgroundTask;
import net.sourceforge.subsonic.androidapp.util.Util;

import static net.sourceforge.subsonic.androidapp.util.Constants.*;

public class SelectAlbumActivity extends SubsonicTabActivity {

    private static final String TAG = SelectAlbumActivity.class.getSimpleName();

    private ListView entryList;
    private ViewGroup footer;
    private View emptyView;
    private Button selectButton;
    private Button playNowButton;
    private Button playLastButton;
    private Button pinButton;
    private Button unpinButton;
    private Button deleteButton;
    private Button moreButton;
    private boolean licenseValid;
    private ImageButton playAllButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_album);

        final String id = getIntent().getStringExtra(INTENT_EXTRA_NAME_ID);
        final String name = getIntent().getStringExtra(INTENT_EXTRA_NAME_NAME);
        final String parentId = getIntent().getStringExtra(INTENT_EXTRA_NAME_PARENT_ID);
        final String parentName = getIntent().getStringExtra(INTENT_EXTRA_NAME_PARENT_NAME);
        String playlistId = getIntent().getStringExtra(INTENT_EXTRA_NAME_PLAYLIST_ID);
        String playlistName = getIntent().getStringExtra(INTENT_EXTRA_NAME_PLAYLIST_NAME);
        String albumListType = getIntent().getStringExtra(INTENT_EXTRA_NAME_ALBUM_LIST_TYPE);
        int albumListSize = getIntent().getIntExtra(INTENT_EXTRA_NAME_ALBUM_LIST_SIZE, 0);
        int albumListOffset = getIntent().getIntExtra(INTENT_EXTRA_NAME_ALBUM_LIST_OFFSET, 0);

        entryList = (ListView) findViewById(R.id.select_album_entries);

        footer = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.select_album_footer, entryList, false);
        entryList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        entryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
                if (position >= 0) {
                    MusicDirectory.Entry entry = getEntryAtPosition(position);
                    if (entry == null) {
                        return;
                    }
                    if (entry.isDirectory()) {
                        Intent intent = new Intent(SelectAlbumActivity.this, SelectAlbumActivity.class);
                        intent.putExtra(INTENT_EXTRA_NAME_ID, entry.getId());
                        intent.putExtra(INTENT_EXTRA_NAME_NAME, entry.getTitle());
                        intent.putExtra(INTENT_EXTRA_NAME_PARENT_ID, id);
                        intent.putExtra(INTENT_EXTRA_NAME_PARENT_NAME, name);
                        Util.startActivityWithoutTransition(SelectAlbumActivity.this, intent);
                    } else if (entry.isVideo()) {
                        playVideo(entry);
                    } else {
                        enableButtons();
                    }
                }
                }
        });

        selectButton = (Button) findViewById(R.id.select_album_select);
        playNowButton = (Button) findViewById(R.id.select_album_play_now);
        playLastButton = (Button) findViewById(R.id.select_album_play_last);
        pinButton = (Button) footer.findViewById(R.id.select_album_pin);
        unpinButton = (Button) footer.findViewById(R.id.select_album_unpin);
        unpinButton = (Button) footer.findViewById(R.id.select_album_unpin);
        deleteButton = (Button) footer.findViewById(R.id.select_album_delete);
        moreButton = (Button) footer.findViewById(R.id.select_album_more);
        emptyView = findViewById(R.id.select_album_empty);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAllOrNone();
            }
        });
        playNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download(false, false, true, false);
                selectAll(false, false);
            }
        });
        playLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download(true, false, false, false);
                selectAll(false, false);
            }
        });
        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download(true, true, false, false);
                selectAll(false, false);
            }
        });
        unpinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unpin();
                selectAll(false, false);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
                selectAll(false, false);
            }
        });

        registerForContextMenu(entryList);

        enableButtons();

        if (playlistId != null) {
            getPlaylist(playlistId, playlistName);
        } else if (albumListType != null) {
            getAlbumList(albumListType, albumListSize, albumListOffset);
        } else {
            getMusicDirectory(id, name, parentId, parentName);
        }

        // Button 1: play all
        playAllButton = (ImageButton) findViewById(R.id.action_button_1);
        playAllButton.setImageResource(R.drawable.action_play_all);
        playAllButton.setVisibility(View.GONE);
        playAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAll();
            }
        });

        // Button 2: search
        ImageButton actionSearchButton = (ImageButton) findViewById(R.id.action_button_2);
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
                new PopupMenuHelper().showMenu(SelectAlbumActivity.this, overflowButton, R.menu.main);
            }
        });
    }

    private void playAll() {
        boolean hasSubFolders = false;
        for (int i = 0; i < entryList.getCount(); i++) {
            MusicDirectory.Entry entry = getEntryAtPosition(i);
            if (entry != null && entry.isDirectory()) {
                hasSubFolders = true;
                break;
            }
        }

        String id = getIntent().getStringExtra(INTENT_EXTRA_NAME_ID);
        if (hasSubFolders && id != null) {
            downloadRecursively(id, false, false, true);
        } else {
            selectAll(true, false);
            download(false, false, true, false);
            selectAll(false, false);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        MusicDirectory.Entry entry = getEntryAtPosition(info.position);
        if (entry == null) {
            return;
        }
        if (entry.isDirectory()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.select_album_context, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.select_song_context, menu);
            DownloadFile downloadFile = getDownloadService().forSong(entry);
            menu.findItem(R.id.song_menu_pin).setVisible(!downloadFile.isSaved());
            menu.findItem(R.id.song_menu_unpin).setVisible(downloadFile.isSaved());
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        MusicDirectory.Entry entry = getEntryAtPosition(info.position);
        if (entry == null) {
            return true;
        }

        List<MusicDirectory.Entry> songs = Arrays.asList(entry);
        switch (menuItem.getItemId()) {
            case R.id.album_menu_play_now:
                downloadRecursively(entry.getId(), false, false, true);
                break;
            case R.id.album_menu_play_last:
                downloadRecursively(entry.getId(), false, true, false);
                break;
            case R.id.album_menu_pin:
                downloadRecursively(entry.getId(), true, true, false);
                break;
            case R.id.song_menu_play_now:
                getDownloadService().download(songs, false, true, true);
                break;
            case R.id.song_menu_play_next:
                getDownloadService().download(songs, false, false, true);
                break;
            case R.id.song_menu_play_last:
                getDownloadService().download(songs, false, false, false);
                break;
            case R.id.song_menu_pin:
                getDownloadService().pin(songs);
                break;
            case R.id.song_menu_unpin:
                getDownloadService().unpin(songs);
                break;
            default:
                return super.onContextItemSelected(menuItem);
        }
        return true;
    }

    private void getMusicDirectory(final String id, String name, final String parentId, final String parentName) {
        setTitle(name);

        new LoadTask() {
            @Override
            protected MusicDirectory load(MusicService service) throws Exception {
                boolean refresh = getIntent().getBooleanExtra(INTENT_EXTRA_NAME_REFRESH, false);
                return service.getMusicDirectory(id, refresh, SelectAlbumActivity.this, this);
            }

            @Override
            protected void done(final Pair<MusicDirectory, Boolean> result) {
                super.done(result);
                setTitle(result.getFirst().getName());
                setBackAction(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent;
                        if (result.getFirst().getParentId() != null) {
                            intent = new Intent(SelectAlbumActivity.this, SelectAlbumActivity.class);
                            intent.putExtra(INTENT_EXTRA_NAME_ID, result.getFirst().getParentId());
                        } else if (parentId != null) {
                            intent = new Intent(SelectAlbumActivity.this, SelectAlbumActivity.class);
                            intent.putExtra(INTENT_EXTRA_NAME_ID, parentId);
                            intent.putExtra(INTENT_EXTRA_NAME_NAME, parentName);
                        } else {
                            intent = new Intent(SelectAlbumActivity.this, SelectArtistActivity.class);
                        }
                        Util.startActivityWithoutTransition(SelectAlbumActivity.this, intent);
                    }
                });
            }
        }.execute();
    }

    private void getPlaylist(final String playlistId, String playlistName) {
        setTitle(playlistName);
        setBackAction(new Runnable() {
            @Override
            public void run() {
                Util.startActivityWithoutTransition(SelectAlbumActivity.this, SelectPlaylistActivity.class);
            }
        });

        new LoadTask() {
            @Override
            protected MusicDirectory load(MusicService service) throws Exception {
                return service.getPlaylist(playlistId, SelectAlbumActivity.this, this);
            }
        }.execute();
    }

    private void getAlbumList(final String albumListType, final int size, final int offset) {

        if ("newest".equals(albumListType)) {
            setTitle(R.string.main_albums_newest);
        } else if ("random".equals(albumListType)) {
            setTitle(R.string.main_albums_random);
        } else if ("highest".equals(albumListType)) {
            setTitle(R.string.main_albums_highest);
        } else if ("recent".equals(albumListType)) {
            setTitle(R.string.main_albums_recent);
        } else if ("frequent".equals(albumListType)) {
            setTitle(R.string.main_albums_frequent);
        }

        setBackAction(new Runnable() {
            @Override
            public void run() {
                Util.startActivityWithoutTransition(SelectAlbumActivity.this, MainActivity.class);
            }
        });

        new LoadTask() {
            @Override
            protected MusicDirectory load(MusicService service) throws Exception {
                return service.getAlbumList(albumListType, size, offset, SelectAlbumActivity.this, this);
            }

            @Override
            protected void done(Pair<MusicDirectory, Boolean> result) {
                if (!result.getFirst().getChildren().isEmpty()) {
                    pinButton.setVisibility(View.GONE);
                    unpinButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                    moreButton.setVisibility(View.VISIBLE);
                    entryList.addFooterView(footer);

                    moreButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(SelectAlbumActivity.this, SelectAlbumActivity.class);
                            String type = getIntent().getStringExtra(INTENT_EXTRA_NAME_ALBUM_LIST_TYPE);
                            int size = getIntent().getIntExtra(INTENT_EXTRA_NAME_ALBUM_LIST_SIZE, 0);
                            int offset = getIntent().getIntExtra(INTENT_EXTRA_NAME_ALBUM_LIST_OFFSET, 0) + size;

                            intent.putExtra(INTENT_EXTRA_NAME_ALBUM_LIST_TYPE, type);
                            intent.putExtra(INTENT_EXTRA_NAME_ALBUM_LIST_SIZE, size);
                            intent.putExtra(INTENT_EXTRA_NAME_ALBUM_LIST_OFFSET, offset);
                            Util.startActivityWithoutTransition(SelectAlbumActivity.this, intent);
                        }
                    });
                }
                super.done(result);
            }
        }.execute();
    }

    private void selectAllOrNone() {
        boolean someUnselected = false;
        int count = entryList.getCount();
        for (int i = 0; i < count; i++) {
            MusicDirectory.Entry entry = getEntryAtPosition(i);
            if (!entryList.isItemChecked(i) && entry != null) {
                someUnselected = true;
                break;
            }
        }
        selectAll(someUnselected, true);
    }

    private void selectAll(boolean selected, boolean toast) {
        int count = entryList.getCount();
        int selectedCount = 0;
        for (int i = 0; i < count; i++) {
            MusicDirectory.Entry entry = getEntryAtPosition(i);
            if (entry != null && !entry.isDirectory() && !entry.isVideo()) {
                entryList.setItemChecked(i, selected);
                selectedCount++;
            }
        }

        // Display toast: N tracks selected / N tracks unselected
        if (toast) {
            int toastResId = selected ? R.string.select_album_n_selected
                    : R.string.select_album_n_unselected;
            Util.toast(this, getString(toastResId, selectedCount));
        }

        enableButtons();
    }

    private MusicDirectory.Entry getEntryAtPosition(int i) {
        Object item = entryList.getItemAtPosition(i);
        return item instanceof MusicDirectory.Entry ? (MusicDirectory.Entry) item : null;
    }

    private void enableButtons() {
        if (getDownloadService() == null) {
            return;
        }

        List<MusicDirectory.Entry> selection = getSelectedSongs();
        boolean enabled = !selection.isEmpty();
        boolean unpinEnabled = false;
        boolean deleteEnabled = false;

        for (MusicDirectory.Entry song : selection) {
            DownloadFile downloadFile = getDownloadService().forSong(song);
            if (downloadFile.isCompleteFileAvailable()) {
                deleteEnabled = true;
            }
            if (downloadFile.isSaved()) {
                unpinEnabled = true;
            }
        }

        playNowButton.setEnabled(enabled);
        playLastButton.setEnabled(enabled);
        pinButton.setEnabled(enabled && !Util.isOffline(this));
        unpinButton.setEnabled(unpinEnabled);
        deleteButton.setEnabled(deleteEnabled);
    }

    private List<MusicDirectory.Entry> getSelectedSongs() {
        List<MusicDirectory.Entry> songs = new ArrayList<MusicDirectory.Entry>(10);
        int count = entryList.getCount();
        for (int i = 0; i < count; i++) {
            if (entryList.isItemChecked(i)) {
                MusicDirectory.Entry entry = getEntryAtPosition(i);
                if (entry != null) {
                    songs.add(entry);
                }
            }
        }
        return songs;
    }

    private void download(final boolean append, final boolean save, final boolean autoplay, final boolean playNext) {
        if (getDownloadService() == null) {
            return;
        }

        final List<MusicDirectory.Entry> songs = getSelectedSongs();
        Runnable onValid = new Runnable() {
            @Override
            public void run() {
                if (!append) {
                    getDownloadService().clear();
                }

                warnIfNetworkOrStorageUnavailable();
                getDownloadService().download(songs, save, autoplay, playNext);
                String playlistName = getIntent().getStringExtra(INTENT_EXTRA_NAME_PLAYLIST_NAME);
                if (playlistName != null) {
                    getDownloadService().setSuggestedPlaylistName(playlistName);
                }
                if (autoplay) {
                    Util.startActivityWithoutTransition(SelectAlbumActivity.this, DownloadActivity.class);
                } else if (save) {
                    Util.toast(SelectAlbumActivity.this,
                            getResources().getQuantityString(R.plurals.select_album_n_songs_downloading, songs.size(), songs.size()));
                } else if (append) {
                    Util.toast(SelectAlbumActivity.this,
                            getResources().getQuantityString(R.plurals.select_album_n_songs_added, songs.size(), songs.size()));
                }
            }
        };

        checkLicenseAndTrialPeriod(onValid);
    }

    private void delete() {
        if (getDownloadService() != null) {
            getDownloadService().delete(getSelectedSongs());
        }
    }

    private void unpin() {
        if (getDownloadService() != null) {
            getDownloadService().unpin(getSelectedSongs());
        }
    }

    private void playVideo(MusicDirectory.Entry entry) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MusicServiceFactory.getMusicService(this).getVideoUrl(this, entry.getId())));

        startActivity(intent);
    }

    private void checkLicenseAndTrialPeriod(Runnable onValid) {
        if (licenseValid) {
            onValid.run();
            return;
        }

        int trialDaysLeft = Util.getRemainingTrialDays(this);
        Log.i(TAG, trialDaysLeft + " trial days left.");

        if (trialDaysLeft == 0) {
            showDonationDialog(trialDaysLeft, null);
        } else if (trialDaysLeft < FREE_TRIAL_DAYS / 2) {
            showDonationDialog(trialDaysLeft, onValid);
        } else {
            Util.toast(this, getResources().getString(R.string.select_album_not_licensed, trialDaysLeft));
            onValid.run();
        }
    }

    private void showDonationDialog(int trialDaysLeft, final Runnable onValid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);

        if (trialDaysLeft == 0) {
            builder.setTitle(R.string.select_album_donate_dialog_0_trial_days_left);
        } else {
            builder.setTitle(getResources().getQuantityString(R.plurals.select_album_donate_dialog_n_trial_days_left,
                    trialDaysLeft, trialDaysLeft));
        }

        builder.setMessage(R.string.select_album_donate_dialog_message);

        builder.setPositiveButton(R.string.select_album_donate_dialog_now,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL)));
                    }
                });

        builder.setNegativeButton(R.string.select_album_donate_dialog_later,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (onValid != null) {
                            onValid.run();
                        }
                    }
                });

        builder.create().show();
    }

    private abstract class LoadTask extends TabActivityBackgroundTask<Pair<MusicDirectory, Boolean>> {

        public LoadTask() {
            super(SelectAlbumActivity.this);
        }

        protected abstract MusicDirectory load(MusicService service) throws Exception;

        @Override
        protected Pair<MusicDirectory, Boolean> doInBackground() throws Throwable {
            MusicService musicService = MusicServiceFactory.getMusicService(SelectAlbumActivity.this);
            MusicDirectory dir = load(musicService);
            boolean valid = musicService.isLicenseValid(SelectAlbumActivity.this, this);
            return new Pair<MusicDirectory, Boolean>(dir, valid);
        }

        @Override
        protected void done(Pair<MusicDirectory, Boolean> result) {
            List<MusicDirectory.Entry> entries = result.getFirst().getChildren();

            boolean hasSongs = false;
            for (MusicDirectory.Entry entry : entries) {
                if (!entry.isDirectory()) {
                    hasSongs = true;
                    break;
                }
            }

            if (hasSongs) {
                entryList.addHeaderView(createHeader(entries));
                entryList.addFooterView(footer);
                selectButton.setVisibility(View.VISIBLE);
                playNowButton.setVisibility(View.VISIBLE);
                playLastButton.setVisibility(View.VISIBLE);
            }

            boolean isAlbumList = getIntent().hasExtra(INTENT_EXTRA_NAME_ALBUM_LIST_TYPE);

            emptyView.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
            playAllButton.setVisibility(isAlbumList || entries.isEmpty() ? View.GONE : View.VISIBLE);
            entryList.setAdapter(new EntryAdapter(SelectAlbumActivity.this, getImageLoader(), entries, true));
            licenseValid = result.getSecond();

            boolean playAll = getIntent().getBooleanExtra(INTENT_EXTRA_NAME_AUTOPLAY, false);
            if (playAll && hasSongs) {
                playAll();
            }
        }
    }

    private View createHeader(List<MusicDirectory.Entry> entries) {
        View header = LayoutInflater.from(this).inflate(R.layout.select_album_header, entryList, false);

        View coverArtView = header.findViewById(R.id.select_album_art);
        getImageLoader().loadImage(coverArtView, entries.get(0), false, true);

        TextView titleView = (TextView) header.findViewById(R.id.select_album_title);
        titleView.setText(getTitle());

        int songCount = 0;

        Set<String> artists = new HashSet<String>();
        for (MusicDirectory.Entry entry : entries) {
            if (!entry.isDirectory()) {
                songCount++;
                if (entry.getArtist() != null) {
                    artists.add(entry.getArtist());
                }
            }
        }

        TextView artistView = (TextView) header.findViewById(R.id.select_album_artist);
        if (artists.size() == 1) {
            artistView.setText(artists.iterator().next());
            artistView.setVisibility(View.VISIBLE);
        } else {
            artistView.setVisibility(View.GONE);
        }

        TextView songCountView = (TextView) header.findViewById(R.id.select_album_song_count);
        String s = getResources().getQuantityString(R.plurals.select_album_n_songs, songCount, songCount);
        songCountView.setText(s.toUpperCase());

        return header;
    }
}
