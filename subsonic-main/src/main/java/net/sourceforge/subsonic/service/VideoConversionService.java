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

package net.sourceforge.subsonic.service;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.dao.VideoConversionDao;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.VideoConversion;
import net.sourceforge.subsonic.service.metadata.MetaData;
import net.sourceforge.subsonic.service.metadata.MetaDataParser;
import net.sourceforge.subsonic.service.metadata.MetaDataParserFactory;
import net.sourceforge.subsonic.service.metadata.Track;
import net.sourceforge.subsonic.util.Util;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class VideoConversionService {

    private static final Logger LOG = Logger.getLogger(VideoConversionService.class);

    private MediaFileService mediaFileService;
    private TranscodingService transcodingService;
    private SettingsService settingsService;
    private VideoConversionDao videoConversionDao;
    private VideoConverter videoConverter;
    private MetaDataParserFactory metaDataParserFactory;

    public void init() {
        videoConverter = new VideoConverter();
        videoConverter.start();

        for (VideoConversion conversion : videoConversionDao.getVideoConversionsByStatus(VideoConversion.Status.IN_PROGRESS)) {
            deleteVideoConversion(conversion);
        }
    }

    public void createVideoConversion(VideoConversion conversion) {
        videoConversionDao.deleteVideoConversionsForFile(conversion.getMediaFileId());
        videoConversionDao.createVideoConversion(conversion);
    }

	public void deleteVideoConversion(VideoConversion conversion) {
		if (conversion.getTargetFile() != null) {
			File targetFile = new File(conversion.getTargetFile());
			if (targetFile.exists()) {
				targetFile.delete();
			}
		}
		if (conversion.getLogFile() != null) {
			File logFile = new File(conversion.getLogFile());
			if (logFile.exists()) {
				logFile.delete();
			}
		}
		
		videoConversionDao.deleteVideoConversion(conversion.getId().intValue());
		videoConverter.cancel(conversion);
    }

    public VideoConversion getVideoConversionForFile(int mediaFileId) {
        VideoConversion conversion = videoConversionDao.getVideoConversionForFile(mediaFileId);
		if (conversion == null) {
            return null;
        }
		
		List<VideoConversion> conversions = deleteIfFileMissing(Collections.singletonList(conversion));
		return (VideoConversion)Iterables.getFirst(conversions, null);
	}
		
	public List<VideoConversion> getAllVideoConversions() {
		return deleteIfFileMissing(this.videoConversionDao.getAllVideoConversions());
	}

    public MetaData getVideoMetaData(MediaFile video) {
        MetaDataParser parser = metaDataParserFactory.getParser(video.getFile());
        return parser != null ? parser.getMetaData(video.getFile()) : null;
    }

	public boolean isStreamable(MediaFile file) {
		if (!Arrays.asList(new String[] { "mp4", "m4v" }).contains(StringUtils.lowerCase(file.getFormat()))) {
			return false;
		}
		MetaData metaData = getVideoMetaData(file);
		if (metaData == null) {
			return true;
		}
		if ((!metaData.getVideoTracks().isEmpty()) && (!((Track)metaData.getVideoTracks().get(0)).isStreamable())) {
			return false;
		}
		if ((!metaData.getAudioTracks().isEmpty()) && (!((Track)metaData.getAudioTracks().get(0)).isStreamable())) {
			return false;
		}
		return true;
	}

	private List<VideoConversion> deleteIfFileMissing(List<VideoConversion> conversions) {
		List<VideoConversion> result = new ArrayList();
		for (VideoConversion conversion : conversions) {
			if ((conversion.getStatus() == VideoConversion.Status.COMPLETED) && (
			(conversion.getTargetFile() == null) || (!new File(conversion.getTargetFile()).exists()))) {
				deleteVideoConversion(conversion);
			} else {
				result.add(conversion);
			}
		}
		return result;
	}

    public void setVideoConversionDao(VideoConversionDao videoConversionDao) {
        this.videoConversionDao = videoConversionDao;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }

    public void setMetaDataParserFactory(MetaDataParserFactory metaDataParserFactory) {
        this.metaDataParserFactory = metaDataParserFactory;
    }
	
	public void setSettingsService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}
	
    private class VideoConverter extends Thread{

        private VideoConversion conversion;
        private Process process;
        private MediaFile mediaFile;

        private VideoConverter() {
            setDaemon(true);
        }

        @Override
		public void run() {
			while (true) {
				Util.sleep(3000L);
				List<VideoConversion> conversions = videoConversionDao.getVideoConversionsByStatus(VideoConversion.Status.NEW);
				if (!conversions.isEmpty())	{
					conversion = ((VideoConversion)conversions.get(0));
					convert();
				}
			}
		}

        private void cancel(VideoConversion conversion) {
            if (process != null && this.conversion != null && this.conversion.getId().equals(conversion.getId())) {
                LOG.info("Killing conversion process for " + mediaFile);
                process.destroy();
            }
        }

        private void convert() {
            videoConversionDao.updateStatus(conversion.getId(), VideoConversion.Status.IN_PROGRESS);

            mediaFile = mediaFileService.getMediaFile(conversion.getMediaFileId());
            try {
				checkDiskLimit();
				
                LOG.info("Starting video conversion of " + mediaFile);

				File logFile = new File(conversion.getLogFile());
				File targetFile = new File(conversion.getTargetFile());

				if (!targetFile.getParentFile().canWrite()) {
					throw new Exception("Write access denied to " + targetFile);
				}
				
				List<String> command = buildFFmpegCommand(targetFile);
				LOG.info("Starting video converter: " + Joiner.on(" ").join(command));

                process = new ProcessBuilder(command).redirectErrorStream(true)
                                                     .start();

                new ProcessReaderThread(process, conversion, logFile).start();
                int retval = process.waitFor();
                LOG.info("ffmpeg exit value: " + retval);

                boolean success =
                        videoConversionDao.getVideoConversionById(conversion.getId()) != null  // conversion was canceled (i.e., removed)
                        && targetFile.exists()
                        && targetFile.length() > 0;

                if (success) {
                    LOG.info("Completed video conversion of " + mediaFile);
                    videoConversionDao.updateStatus(conversion.getId(), VideoConversion.Status.COMPLETED);
                } else {
                    LOG.error("An error occurred while converting video " + mediaFile + ". See log file " + logFile.getAbsolutePath());
                    videoConversionDao.updateStatus(conversion.getId(), VideoConversion.Status.ERROR);
                }

            } catch (Exception x) {
                LOG.error("An error occurred while converting video " + mediaFile + ": " + x, x);
                videoConversionDao.updateStatus(conversion.getId(), VideoConversion.Status.ERROR);
            }
        }

		private void checkDiskLimit() {
			int limitInGB = settingsService.getVideoConversionDiskLimit();
			if (limitInGB == 0) {
				return;
			}
			long limitInBytes = limitInGB * 1073741824L;
			
			File dir = new File(settingsService.getVideoConversionDirectory());
			if ((!dir.canRead()) || (!dir.isDirectory())) {
				return;
			}
			
			List<File> files = new ArrayList();
			long usedBytes = 0L;
			for (File file : dir.listFiles((java.io.FileFilter)FileFileFilter.FILE)) {
				files.add(file);
				usedBytes += file.length();
			}
			
			if (usedBytes < limitInBytes) {
				return;
			}
			
			
			Collections.sort(files, new Comparator()
			{
				public int compare(Object a, Object b) {
					long lastModifiedA = ((File)a).lastModified();
					long lastModifiedB = ((File)b).lastModified();
					if (lastModifiedA < lastModifiedB) {
						return -1;
					}
					if (lastModifiedA > lastModifiedB) {
						return 1;
					}
					return 0;
				}
			});
			
			
			while ((usedBytes > limitInBytes) && (!files.isEmpty())) {
				File victim = (File)files.remove(0);
				usedBytes -= victim.length();
				victim.delete();
				VideoConversionService.LOG.info("Deleted converted video file " + victim);
			}
		}
			
		private List<String> buildFFmpegCommand(File targetFile) {
			List<String> command = new ArrayList<String>();

            command.add(transcodingService.getTranscodeDirectory() + File.separator + "ffmpeg");
            command.add("-i");
			command.add(mediaFile.getFile().getAbsolutePath());
            command.add("-ac");
            command.add("2");
            command.add("-f");
            command.add("mp4");
            command.add("-preset");
            command.add("superfast");
			command.add("-strict");
			command.add("-2");
            command.add("-y");
			
			Integer bitRate = conversion.getBitRate();
			if (bitRate != null) {
				command.add("-b:v");
				command.add(bitRate + "k");
			
				if (bitRate.intValue() < 2000) {
					Dimension dim = Util.getSuitableVideoSize(mediaFile.getWidth(), mediaFile.getHeight(), bitRate);
					command.add("-s");
					command.add(dim.width + "x" + dim.height);
				}
			}
			
            // Look for video track with streamable codec. If found, copy it.
            MetaData metaData = getVideoMetaData(mediaFile);
            Track videoTrack = null;
            List<Track> videoTracks = metaData.getVideoTracks();
			if (bitRate == null) {
				for (Track track : videoTracks) {
					if (track.isStreamable()) {
						command.add("-c:v");
						command.add("copy");
						videoTrack = track;
						break;
					}
				}
			}
            if (videoTrack == null && !videoTracks.isEmpty()) {
                videoTrack = videoTracks.get(0);
            }

            // Find audio track, taking user-preferred track into account.
            List<Track> audioTracks = metaData.getAudioTracks();
            Track audioTrack = null;
            if (conversion.getAudioTrackId() != null) {
                for (Track track : audioTracks) {
                    if (conversion.getAudioTrackId().equals(track.getId())) {
                        audioTrack = track;
                        break;
                    }
                }
            }
            if (audioTrack == null && !audioTracks.isEmpty()) {
                audioTrack = audioTracks.get(0);
            }

            // Copy audio track if streamable.
            if (audioTrack != null && audioTrack.isStreamable()) {
                command.add("-c:a");
                command.add("copy");
            }

            // If container has multiple audio or video tracks, add "-map" options to specify which
            // tracks to include.
            if (videoTracks.size() > 1 || audioTracks.size() > 1) {
                if (videoTrack != null) {
                    command.add("-map");
                    command.add("0:" + videoTrack.getId());
                }
                if (audioTrack != null) {
                    command.add("-map");
                    command.add("0:" + audioTrack.getId());
                }
            }

            command.add(targetFile.getAbsolutePath());
            return command;
        }
    }

    private class ProcessReaderThread extends Thread {
        private final Process process;
        private final VideoConversion conversion;
        private final File logFile;

        public ProcessReaderThread(Process process, VideoConversion conversion, File logFile) {
            this.process = process;
            this.conversion = conversion;
            this.logFile = logFile;
            setDaemon(true);
        }

        @Override
        public void run() {

            // frame= 2558 fps=150 q=24.0 size=   82720kB time=00:01:47.47 bitrate=6305.3kbits/s dup=1 drop=0
            Pattern pattern = Pattern.compile("^frame=.*time=(\\d+):(\\d+):(\\d+).(\\d+).*");
            BufferedReader reader = null;
            BufferedWriter logWriter = null;
            try {
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                logWriter = new BufferedWriter(new FileWriter(logFile));

                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    logWriter.append(line);
                    logWriter.newLine();
                    logWriter.flush();

                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        int hours = Integer.parseInt(matcher.group(1));
                        int minutes = Integer.parseInt(matcher.group(2));
                        int seconds = Integer.parseInt(matcher.group(3));

                        int progress = hours * 3600 + minutes * 60 + seconds;
                        videoConversionDao.updateProgress(conversion.getId(), progress);
                    }
                }
            } catch (IOException e) {
                LOG.warn("Error when reading output from video converter.", e);
            } finally {
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(logWriter);
            }
        }
    }
}

