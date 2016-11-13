package httpexcutor;


import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.io.ChunkedInputStream;

import file_op.AnyFile_Op;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MediaDownloadRequestExecutor �����з���http post&get �࣬��Ҫ����΢���ϴ���ʽ���ļ���ȡ��
 * ΢���ϴ���ͼƬֻ�ṩapi�ӿڣ������ڳ�������΢�ŷ�������������ͼƬ������
 * @author zhgangximing
 * @version 1.0.0
 *
 */
public class MediaDownloadRequestExecutor {
	private File tmpDirFile;

	  public MediaDownloadRequestExecutor() {
	    super();
	  }

	  public MediaDownloadRequestExecutor(File tmpDirFile) {
	    super();
	    this.tmpDirFile = tmpDirFile;
	  }
	 
	  /**
	   * Excute_post ����http post����
	   * @param httpclient
	   * @param httpProxy
	   * @param uri  ΢��ͼƬ��api url
	   * @param filename
	   * @param savedir
	   * @return
	   * @throws ClientProtocolException
	   * @throws IOException
	   */
	  public String  Excute_post(CloseableHttpClient httpclient, HttpHost httpProxy, String uri, String filename,String savedir) throws ClientProtocolException, IOException{
	    
	    HttpPost httpPost = new HttpPost(uri);
	    if (httpProxy != null) {
	      RequestConfig config = RequestConfig.custom().setProxy(httpProxy).build();
	      httpPost.setConfig(config);
	    }

	      CloseableHttpResponse response = httpclient.execute(httpPost);

	      Header[] contentTypeHeader = response.getHeaders("Content-Type");
	      HttpEntity hEntity = response.getEntity();
	      InputStream inputStream =  hEntity.getContent();
	      
	      AnyFile_Op aOp= new AnyFile_Op();
	      aOp.CreateDir(savedir);
	      byte[] readbuf = new byte[1024 * 1024 * 7];
	      String newfilename = filename + ".jpg";
	      File file = new File(savedir + "/" + newfilename);
	      FileOutputStream fOutputStream = new FileOutputStream(file);
	      try {
	    	  int act_size = -1;
	    	  while((act_size = inputStream.read(readbuf)) != -1){
	    		  fOutputStream.write(readbuf, 0, act_size);
	    	  }
			
			System.out.println("��ȡ�����ֽ���" + act_size);
			inputStream.close();
			fOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      httpclient.close();
	      return newfilename;
	  }

	  protected String getFileName(CloseableHttpResponse response) {
	    Header[] contentDispositionHeader = response.getHeaders("Content-disposition");
	    Pattern p = Pattern.compile(".*filename=\"(.*)\"");
	    Matcher m = p.matcher(contentDispositionHeader[0].getValue());
	    m.matches();
	    String fileName = m.group(1);
	    return fileName;
	  }
}