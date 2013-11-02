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
package net.sourceforge.subsonic.androidapp.util;

import android.app.Activity;
import net.sourceforge.subsonic.androidapp.R;
import net.sourceforge.subsonic.androidapp.service.MusicService;
import net.sourceforge.subsonic.androidapp.service.MusicServiceFactory;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public final class StarUtil {

    private StarUtil() {
    }

    public static void starInBackground(final Activity context, final String id, final boolean star) {
        new SilentBackgroundTask<Void>(context) {
            @Override
            protected Void doInBackground() throws Throwable {
                MusicService musicService = MusicServiceFactory.getMusicService(context);
                musicService.star(id, star, context, null);
                return null;
            }

            @Override
            protected void done(Void result) {
            }

            @Override
            protected void error(Throwable error) {
                String msg = context.getResources().getString(R.string.common_star_failed) + " " + getErrorMessage(error);
                Util.toast(context, msg);
            }
        }.execute();
    }

}
