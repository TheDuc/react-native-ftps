
package com.reactlibrary;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.util.TrustManagerUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RNFtpModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private String ip_address;
  private int port;
  private FTPSClient client;
  private boolean is_tls;

  public RNFtpModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @ReactMethod
  public void setup(String ip_address, int port, boolean is_tls){
    this.is_tls = is_tls;
    this.ip_address = ip_address;
    this.port = port;

    if(this.is_tls == true)
    {
      this.client = new FTPSClient("TLS", true); //Will force the use of TLS - implicit
    }
    else
    {
      this.client = new FTPSClient(); //Will use explicit
    }
  }

  @ReactMethod
  public void client_status(final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        if(client == null)
        promise.resolve("disconnected");
        else
        promise.resolve("connected");
      }
    }).start();
  }

  @ReactMethod
  public void login(final String username, final String password, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          
          client.setRemoteVerificationEnabled(false);
          client.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager()); 
          
          //client.setControlEncoding("UTF-8")
          client.connect(this.ip_address,this.port);
          client.setSoTimeout(2000);

          if ( ! FTPReply.isPositiveCompletion(client.getReplyCode()) ) {

            client.disconnect();
            throw new Exception("FTP Haven't replied");
          }

          client.enterLocalPassiveMode();
          
          if(client.login(username, password))
          {
            // logger.debug("Logged successfully on FTP");
            promise.resolve(true);
          }
          else
          {
            // logger.debug("Can't login to FTP");
            promise.reject("ERROR", "Can't login to FTP");
          }
          
        } catch (Exception e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }

  @ReactMethod
  public void list(final String path, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        FTPFile[] files = new FTPFile[0];
        try {
          files = client.listFiles(path);
          JSONObject json = new JSONObject();
          JSONArray arrfiles = new JSONArray();
          for (FTPFile file : files) {
            JSONObject tmp = new JSONObject();
            tmp.put("name",file.getName());
            tmp.put("size",file.getSize());
            tmp.put("timestamp",file.getTimestamp());
            arrfiles.put(tmp);
          }
          json.put("results",arrfiles);
          promise.resolve(json.toString());
        } catch (Exception e) {
          promise.reject("ERROR", e.getMessage());
        }
      }
    }).start();
  }

  @ReactMethod
  public void makedir(final String path, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.makeDirectory(path);
          promise.resolve(true);
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }

  @ReactMethod
  public void removedir(final String path, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.removeDirectory(path);
          promise.resolve(true);
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }
  @ReactMethod
  public void removeFile(final String path, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.deleteFile(path);
          promise.resolve(true);
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }

  @ReactMethod
  public void changeDirectory(final String path, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.changeWorkingDirectory(path);
          promise.resolve(true);
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }


  @ReactMethod
  public void logout(final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {         
          client.logout();
          client.disconnect();
          promise.resolve(true);
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }

  @ReactMethod
  public void force_disconnect(final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try
        {
          client.disconnect();
          promise.resolve(true);
        }
        catch(IOException e)
        {
          promise.reject("ERROR", e.getMessage());
        }
      }
    }).start();
  }


  @ReactMethod
  public void uploadFile(final String path,final String remoteDestinationDir, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.setFileType(FTP.BINARY_FILE_TYPE);
          File firstLocalFile = new File(path);

          String firstRemoteFile = remoteDestinationDir+"/"+firstLocalFile.getName();
          InputStream inputStream = new FileInputStream(firstLocalFile);

          System.out.println("Start uploading first file");
          boolean done = client.storeFile(firstRemoteFile, inputStream);
          inputStream.close();
          if (done) {
            promise.resolve(true);
          }else{
            promise.reject("FAILED",firstLocalFile.getName()+" is not uploaded successfully.");
          }
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }


  @ReactMethod
  public void downloadFile(final String remoteFile1,final String localDestinationDir, final Promise promise){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          client.setFileType(FTP.BINARY_FILE_TYPE);
          File remoteFile = new File(remoteFile1);
          File downloadFile1 = new File(localDestinationDir+"/"+remoteFile.getName());
          OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
          boolean success = client.retrieveFile(remoteFile1, outputStream1);
          outputStream1.close();

          if (success) {
            promise.resolve(true);
          }else{
            promise.reject("FAILED",remoteFile.getName()+" is not downloaded successfully.");
          }
        } catch (IOException e) {
          promise.reject("ERROR",e.getMessage());
        }
      }
    }).start();
  }


  @Override
  public String getName() {
    return "FTP";
  }
}
