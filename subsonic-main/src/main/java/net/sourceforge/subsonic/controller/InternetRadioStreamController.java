package net.sourceforge.subsonic.controller;

import com.google.common.base.Joiner;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.domain.InternetRadio;
import net.sourceforge.subsonic.io.InputStreamReaderThread;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.TranscodingService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class InternetRadioStreamController
  extends ParameterizableViewController
{
  private static final Logger LOG = Logger.getLogger(InternetRadioStreamController.class);
  
  private final List<String> CONTENT_TYPES_PLS = Arrays.asList(new String[] { "audio/x-scpls" });
  private final List<String> CONTENT_TYPES_XSPF = Arrays.asList(new String[] { "application/xspf+xml" });
  private final List<String> CONTENT_TYPES_M3U = Arrays.asList(new String[] { "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl" });
  
  private SettingsService settingsService;
  
  private TranscodingService transcodingService;
  

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
    InternetRadio radio = settingsService.getInternetRadioById(Integer.valueOf(id));
    
    String url = radio.getStreamUrl();
    String streamUrl = findStreamUrl(url);
    
    List<String> command = buildFFmpegCommand(streamUrl);
    Process process = new ProcessBuilder(command).start();
    

    new InputStreamReaderThread(process.getErrorStream(), (String)command.get(0), true).start();
    try
    {
      response.setContentType("audio/mpeg");
      IOUtils.copy(process.getInputStream(), response.getOutputStream());
    } catch (Exception e) {
      process.destroy();
    }
    return null;
  }
  
  private String findStreamUrl(String url)
    throws IOException
  {
    URI uri = URI.create(url);
    String suffix = StringUtils.substringAfterLast(uri.getPath(), ".");
    if ("pls".equals(suffix)) {
      return findStreamUrlFromPLS(url);
    }
    if ("m3u".equals(suffix)) {
      return findStreamUrlFromM3U(url);
    }
    if ("xspf".equals(suffix)) {
      return findStreamUrlFromXSPF(url);
    }
    

    HttpClient client = createHttpClient();
    Header header;
    try {
      HttpResponse response = client.execute(new HttpHead(url));
      header = response.getFirstHeader("Content-Type");
      String contentType = header == null ? null : header.getValue();
      String str1; if (contentType == null) {
        return url;
      }
      contentType = StringUtils.substringBefore(contentType, ";");
      if (CONTENT_TYPES_PLS.contains(contentType.toLowerCase())) {
        return findStreamUrlFromPLS(url);
      }
      if (CONTENT_TYPES_XSPF.contains(contentType.toLowerCase())) {
        return findStreamUrlFromXSPF(url);
      }
      if (CONTENT_TYPES_M3U.contains(contentType.toLowerCase()))
        return findStreamUrlFromM3U(url);
    } catch (Exception x) {
      return url;
    } finally {
      client.getConnectionManager().shutdown();
    }
    return url;
  }
  
  private String findStreamUrlFromPLS(String url) throws IOException {
    HttpClient client = createHttpClient();
    BufferedReader reader = null;
    try {
      InputStream in = client.execute(new HttpGet(url)).getEntity().getContent();
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      String str1; while (line != null) {
        if (line.startsWith("File1=")) {
          return line.replaceFirst("File1=", "");
        }
        line = reader.readLine();
      }
      return url;
    } finally {
      IOUtils.closeQuietly(reader);
      client.getConnectionManager().shutdown();
    }
  }
  
  private String findStreamUrlFromXSPF(String url) throws IOException {
    HttpClient client = createHttpClient();
    BufferedReader reader = null;
    try {
      Pattern pattern = Pattern.compile("<location>(.+)</location>");
      InputStream in = client.execute(new HttpGet(url)).getEntity().getContent();
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      Matcher matcher; while (line != null) {
        matcher = pattern.matcher(line);
        if (matcher.find()) {
          return matcher.group(1);
        }
        line = reader.readLine();
      }
      return url;
    } finally {
      IOUtils.closeQuietly(reader);
      client.getConnectionManager().shutdown();
    }
  }
  
  private String findStreamUrlFromM3U(String url) throws IOException {
    HttpClient client = createHttpClient();
    BufferedReader reader = null;
    try {
      InputStream in = client.execute(new HttpGet(url)).getEntity().getContent();
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      String str1; while (line != null) {
        if (line.startsWith("http")) {
          return line;
        }
        line = reader.readLine();
      }
      return url;
    } finally {
      IOUtils.closeQuietly(reader);
      client.getConnectionManager().shutdown();
    }
  }
  
  private HttpClient createHttpClient() {
    HttpClient client = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
    HttpConnectionParams.setSoTimeout(client.getParams(), 10000);
    return client;
  }
  
  private List<String> buildFFmpegCommand(String url) {
    List<String> command = new ArrayList();
    
    command.add(transcodingService.getTranscodeDirectory() + File.separator + "ffmpeg");
    command.add("-i");
    command.add(url);
    command.add("-f");
    command.add("mp3");
    command.add("-v");
    command.add("0");
    command.add("-");
    
    LOG.info("Starting converter for radio: " + Joiner.on(" ").join(command));
    return command;
  }
  
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
  
  public void setTranscodingService(TranscodingService transcodingService) {
    this.transcodingService = transcodingService;
  }
}

