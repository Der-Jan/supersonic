package net.sourceforge.subsonic.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.VideoConversion;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.VideoConversionService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class VideoConversionSettingsController
  extends ParameterizableViewController
{
  private VideoConversionService videoConversionService;
  private SettingsService settingsService;
  private MediaFileService mediaFileService;
  
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    Map<String, Object> map = new HashMap();
    
    if (isFormSubmission(request)) {
      handleParameters(request);
      map.put("toast", Boolean.valueOf(true));
    }
    
    ModelAndView result = super.handleRequestInternal(request, response);
    
    map.put("conversionInfos", getVideoConversionInfo());
    map.put("directory", this.settingsService.getVideoConversionDirectory());
    map.put("diskLimit", Integer.valueOf(this.settingsService.getVideoConversionDiskLimit()));
    map.put("bytesUsed", Long.valueOf(getDiskUsage()));
    map.put("licenseInfo", this.settingsService.getLicenseInfo());
    
    result.addObject("model", map);
    return result;
  }
  
  private long getDiskUsage() {
    File dir = new File(this.settingsService.getVideoConversionDirectory());
    if ((dir.canRead()) && (dir.isDirectory())) {
      return FileUtils.sizeOfDirectory(dir);
    }
    return 0L;
  }
  
  private List<VideoConversionInfo> getVideoConversionInfo() {
    List<VideoConversionInfo> result = new ArrayList();
    for (VideoConversion conversion : this.videoConversionService.getAllVideoConversions()) {
      File file = new File(conversion.getTargetFile());
      Long size = null;
      if (file.exists()) {
        size = Long.valueOf(file.length());
      }
      result.add(new VideoConversionInfo(conversion, this.mediaFileService.getMediaFile(conversion.getMediaFileId()), size));
    }
    return result;
  }
  





  private boolean isFormSubmission(HttpServletRequest request)
  {
    return "POST".equals(request.getMethod());
  }
  
  private void handleParameters(HttpServletRequest request) throws ServletRequestBindingException {
    for (VideoConversion conversion : this.videoConversionService.getAllVideoConversions()) {
      boolean delete = getParameter(request, "delete", conversion.getId().intValue()) != null;
      if (delete) {
        this.videoConversionService.deleteVideoConversion(conversion);
      }
    }
    
    String directory = StringUtils.trimToNull(request.getParameter("directory"));
    if (directory != null) {
      this.settingsService.setVideoConversionDirectory(directory);
    }
    int limit = ServletRequestUtils.getRequiredIntParameter(request, "diskLimit");
    this.settingsService.setVideoConversionDiskLimit(limit);
    this.settingsService.save();
  }
  
  private String getParameter(HttpServletRequest request, String name, int id) {
    return StringUtils.trimToNull(request.getParameter(name + "[" + id + "]"));
  }
  
  public void setMediaFileService(MediaFileService mediaFileService) {
    this.mediaFileService = mediaFileService;
  }
  
  public void setVideoConversionService(VideoConversionService videoConversionService) {
    this.videoConversionService = videoConversionService;
  }
  
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
  
  public static class VideoConversionInfo {
    private final VideoConversion conversion;
    private final MediaFile video;
    private final Long size;
    
    public VideoConversionInfo(VideoConversion conversion, MediaFile video, Long size) {
      this.conversion = conversion;
      this.video = video;
      this.size = size;
    }
    
    public VideoConversion getConversion() {
      return this.conversion;
    }
    
    public MediaFile getVideo() {
      return this.video;
    }
    
    public Long getSize() {
      return this.size;
    }
  }
}
