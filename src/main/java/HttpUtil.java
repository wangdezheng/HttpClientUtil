import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClient封装的工具类
 */
public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final CloseableHttpClient httpClient;
    private static final String CHARSET = "UTF-8";

    // 静态代码块，初始化时间配置，根据配置生成默认的httpClient
    static {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(4000).setSocketTimeout(4000).build();
        //访问一些特定网站需要设置http代理
        HttpHost proxy = new HttpHost("10.0.254.7", 3128);
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setProxy(proxy).build();

    }

    /**
     * 对外访问接口, Http Get请求
     *
     * @param url    请求的url, ? 之前的地址
     * @param params 需要传递的参数，格式key-value pair
     * @return 返回页面内容的String
     */
    public static String doGet(String url, Map<String, String> params) {
        return doGet(url, params, CHARSET);
    }

    /**
     * 对外访问接口, Http Post请求
     *
     * @param url    请求的url, ? 之前的地址
     * @param params 需要传递的参数，格式key-value pair
     * @return 返回页面内容的String
     * @throws IOException
     */
    public static String doPost(String url, Map<String, String> params) throws IOException {
        return doPost(url, params, CHARSET);
    }

    /**
     * doGet请求实现方法
     *
     * @param url     请求的url, ? 之前的地址
     * @param params  需要传递的参数，格式key-value pair
     * @param charset 设置编码
     * @return 返回页面内容的String
     */
    private static String doGet(String url, Map<String, String> params, String charset) {
        if (url == null || url.length() == 0) {
            return null;
        }
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
                // 如果请求参数不为空, 将请求参数和url拼接
                if (pairs.size() > 0) {
                    url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
                }
            }
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // status 返回状态码
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                httpGet.abort();
                throw new RuntimeException("HttpClient get, error status code: " + status);
            }
            // 获取响应的实体
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            logger.error("网络连接出错或EntityUtils转换String出错: ", e.getMessage(), e);
        }
        return null;
    }

    /**
     * doPost请求实现方法
     *
     * @param url     请求的url, ? 之前的地址
     * @param params  需要传递的参数，格式key-value pair
     * @param charset 设置编码
     * @return 返回页面内容的String
     * @throws IOException 抛出异常
     */
    private static String doPost(String url, Map<String, String> params, String charset) throws IOException {
        if (url == null || url.length() == 0) {
            return null;
        }
        List<NameValuePair> pairs = null;
        if (params != null && !params.isEmpty()) {
            pairs = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
        }
        HttpPost httpPost = new HttpPost(url);
        if (pairs != null && pairs.size() > 0) {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            // status 返回状态码
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                httpPost.abort();
                throw new RuntimeException("HttpClient get, error status code: " + status);
            }
            // 获取响应的实体
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, charset);
            }
            EntityUtils.consume(entity);
            return result;
        } catch (ParseException e) {
            logger.error("网络连接出错或EntityUtils转换String出错: ", e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }
}
