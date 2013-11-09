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

 Copyright 2010 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.androidapp.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import net.sourceforge.subsonic.androidapp.R;
import net.sourceforge.subsonic.androidapp.domain.Artist;
import net.sourceforge.subsonic.androidapp.domain.MusicDirectory;
import net.sourceforge.subsonic.androidapp.domain.SearchCritera;
import net.sourceforge.subsonic.androidapp.domain.SearchResult;
import net.sourceforge.subsonic.androidapp.service.MusicService;
import net.sourceforge.subsonic.androidapp.service.MusicServiceFactory;

/**
 * Provides search suggestions based on queries to the server.
 *
 * @author Sindre Mehus
 */
public class CustomSuggestionsProvider extends ContentProvider {

    private static final String[] COLUMNS = {"_id",
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_ICON_1};


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = selectionArgs[0] + "*";
        SearchResult searchResult = search(query);
        return createCursor(searchResult);

        // TODO: Add icon
    }

    private SearchResult search(String query) {
        MusicService musicService = MusicServiceFactory.getMusicService(getContext());
        if (musicService == null) {
            return null;
        }

        try {
            return musicService.search(new SearchCritera(query, 5, 10, 10), getContext(), null);
        } catch (Exception e) {
            return null;
        }
    }

    private Cursor createCursor(SearchResult searchResult) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        if (searchResult == null) {
            return cursor;
        }
        for (Artist artist : searchResult.getArtists()) {
            String icon = "android.resource://net.sourceforge.subsonic.androidapp/" + R.drawable.media_start;
            cursor.addRow(new Object[]{artist.getId(), artist.getName(), null, artist.getId(), icon});
        }
        for (MusicDirectory.Entry album : searchResult.getAlbums()) {
            cursor.addRow(new Object[]{album.getId(), album.getTitle(), album.getArtist(), album.getId(), null});
        }
        for (MusicDirectory.Entry song : searchResult.getSongs()) {
            cursor.addRow(new Object[]{song.getId(), song.getTitle(), song.getArtist(), song.getParent(), null});
        }
        return cursor;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}