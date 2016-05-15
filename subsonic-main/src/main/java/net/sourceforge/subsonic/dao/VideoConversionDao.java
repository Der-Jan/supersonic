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
package net.sourceforge.subsonic.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import net.sourceforge.subsonic.domain.VideoConversion;

/**
 * Provides database services for video conversions.
 *
 * @author Sindre Mehus
 */
public class VideoConversionDao extends AbstractDao {

    private static final String COLUMNS = "id, media_file_id, audio_track_id, username, status, target_file, log_file, bit_rate, progress_seconds, created, changed, started";

    private VideoConversionRowMapper rowMapper = new VideoConversionRowMapper();

    public synchronized void createVideoConversion(VideoConversion conversion) {
        Integer audioTrackId = conversion.getAudioTrackId() == null ? -1 : conversion.getAudioTrackId();
        update("insert into video_conversion (" + COLUMNS + ") values (" + questionMarks(COLUMNS) + ")", null,
               conversion.getMediaFileId(), audioTrackId, conversion.getUsername(), conversion.getStatus().name(),
               conversion.getTargetFile(), conversion.getLogFile(), conversion.getBitRate(), conversion.getProgressSeconds(),
               conversion.getCreated(), conversion.getChanged(), conversion.getStarted());
    }

    public synchronized void updateProgress(Integer id, Integer progressSeconds) {
        update("update video_conversion set progress_seconds=? where id=?", progressSeconds, id);
    }

    public synchronized void updateStatus(Integer id, VideoConversion.Status status) {
        Date now = new Date();
        if (status  == VideoConversion.Status.IN_PROGRESS) {
            update("update video_conversion set status=?, changed=?, started=? where id=?", status.name(), now, now, id);
        } else {
            update("update video_conversion set status=?, changed=? where id=?", status.name(), now, id);
        }
    }

    public synchronized VideoConversion getVideoConversionForFile(int mediaFileId) {
        return queryOne("select " + COLUMNS + " from video_conversion where media_file_id=? order by created desc",
                        rowMapper, mediaFileId);
    }

    public synchronized List<VideoConversion> getAllVideoConversions() {
        return query("select " + COLUMNS + " from video_conversion order by created desc", rowMapper);
    }

    public synchronized void deleteVideoConversionsForFile(Integer mediaFileId) {
        update("delete from video_conversion where media_file_id=?", mediaFileId);
    }

    public synchronized void deleteVideoConversion(int id) {
        update("delete from video_conversion where id=?", id);
    }

    public synchronized VideoConversion getVideoConversionById(Integer id) {
        return queryOne("select " + COLUMNS + " from video_conversion where id=?", rowMapper, id);
    }

    public synchronized List<VideoConversion> getVideoConversionsByStatus(VideoConversion.Status status) {
        return query("select " + COLUMNS + " from video_conversion where status=? order by created", rowMapper, status.name());
    }

    private static class VideoConversionRowMapper implements ParameterizedRowMapper<VideoConversion> {
        public VideoConversion mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer audioTrackId = rs.getInt(3);
            if (audioTrackId == -1) {
                audioTrackId = null;
            }

			Integer bitRate = rs.getInt(8);
			if (bitRate.intValue() == 0) {
				bitRate = null;
			}			
			      
			return new VideoConversion(rs.getInt(1), rs.getInt(2), audioTrackId, rs.getString(4), VideoConversion.Status.valueOf(rs.getString(5)), 
					                   rs.getString(6), rs.getString(7), bitRate, rs.getInt(9), rs.getTimestamp(10), rs.getTimestamp(11), rs.getTimestamp(12));
        }
    }
}
