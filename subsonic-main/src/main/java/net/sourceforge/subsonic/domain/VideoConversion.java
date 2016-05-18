/*
 * This file is part of Subsonic.
 *
 *  Subsonic is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Subsonic is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2016 (C) Sindre Mehus
 */

package net.sourceforge.subsonic.domain;

import java.util.Date;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class VideoConversion {

    private Integer id;
    private int mediaFileId;
    private Integer audioTrackId;
    private String username;
    private Status status;
    private String targetFile;
    private String logFile;
    private Integer bitRate;
    private final Integer progressSeconds;
    private Date created;
    private Date changed;
    private Date started;

    public VideoConversion(Integer id, int mediaFileId, Integer audioTrackId, String username, Status status, String targetFile, String logFile, Integer bitRate,
                           Integer progressSeconds, Date created, Date changed, Date started) {
        this.id = id;
        this.mediaFileId = mediaFileId;
        this.audioTrackId = audioTrackId;
        this.username = username;
        this.status = status;
        this.targetFile = targetFile;
        this.logFile = logFile;
        this.bitRate = bitRate;
        this.progressSeconds = progressSeconds;
        this.created = created;
        this.changed = changed;
        this.started = started;
    }

    public Integer getId() {
        return id;
    }

    public int getMediaFileId() {
        return mediaFileId;
    }

    public Integer getAudioTrackId() {
        return audioTrackId;
    }

    public String getUsername() {
        return username;
    }

    public Status getStatus() {
        return status;
    }

    public String getTargetFile() {
        return this.targetFile;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public Integer getBitRate() {
        return this.bitRate;
    }

    public Integer getProgressSeconds() {
        return progressSeconds;
    }

    public Date getCreated() {
        return created;
    }

    public Date getChanged() {
        return changed;
    }

    public Date getStarted() {
        return started;
    }

    public enum Status {
        NEW,
        IN_PROGRESS,
        ERROR,
        COMPLETED
    }
}
