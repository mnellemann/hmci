package biz.nellemann.hmci;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import biz.nellemann.hmci.dto.xml.LogonResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {

    private final static Logger log = LoggerFactory.getLogger(RestClient.class);
    private final MediaType MEDIA_TYPE_IBM_XML_LOGIN = MediaType.parse("application/vnd.ibm.powervm.web+xml; type=LogonRequest");
    private final MediaType MEDIA_TYPE_IBM_XML_POST = MediaType.parse("application/xml, application/vnd.ibm.powervm.pcm.dita");


    protected OkHttpClient httpClient;

    // OkHttpClient timeouts
    private final static int CONNECT_TIMEOUT_SEC = 10;
    private final static int WRITE_TIMEOUT_SEC = 30;
    private static int READ_TIMEOUT_SEC = 180;

    protected String authToken;
    protected final String baseUrl;
    protected final String username;
    protected final String password;

    private final static int MAX_MINUTES_BETWEEN_AUTHENTICATION = 60; // TODO: Make configurable and match HMC timeout settings
    private Instant lastAuthenticationTimestamp;


    public RestClient(String baseUrl, String username, String password, Boolean trustAll, int timeout) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.READ_TIMEOUT_SEC = timeout;
        if (trustAll) {
            this.httpClient = getUnsafeOkHttpClient();
        } else {
            this.httpClient = getSafeOkHttpClient();
        }

        /*
        if(configuration.trace != null) {
            try {
                File traceDir = new File(configuration.trace);
                traceDir.mkdirs();
                if(traceDir.canWrite()) {
                    Boolean doTrace = true;
                } else {
                    log.warn("ManagementConsole() - can't write to trace dir: " + traceDir.toString());
                }
            } catch (Exception e) {
                log.error("ManagementConsole() - trace error: " + e.getMessage());
            }
        }*/
        Thread shutdownHook = new Thread(this::logoff);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }


    /**
     * Logon to the HMC and get an authentication token for further requests.
     */
    public synchronized void login() {
        if(authToken != null) {
            logoff();
        }

        log.info("Connecting to HMC - {} @ {}", username, baseUrl);
        StringBuilder payload = new StringBuilder();
        payload.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
        payload.append("<LogonRequest xmlns='http://www.ibm.com/xmlns/systems/power/firmware/web/mc/2012_10/' schemaVersion='V1_0'>");
        payload.append("<UserID>").append(username).append("</UserID>");
        payload.append("<Password>").append(password).append("</Password>");
        payload.append("</LogonRequest>");

        try {
            //httpClient.start();
            URL url = new URL(String.format("%s/rest/api/web/Logon", baseUrl));
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.ibm.powervm.web+xml; type=LogonResponse")
                .addHeader("X-Audit-Memento", "IBM Power HMC Insights")
                .put(RequestBody.create(payload.toString(), MEDIA_TYPE_IBM_XML_LOGIN))
                .build();

            String responseBody;
            try (Response response = httpClient.newCall(request).execute()) {
                responseBody = Objects.requireNonNull(response.body()).string();
                if (!response.isSuccessful()) {
                    log.warn("login() - Unexpected response: {}", response.code());
                    throw new IOException("Unexpected code: " + response);
                }
            }

            XmlMapper xmlMapper = new XmlMapper();
            LogonResponse logonResponse = xmlMapper.readValue(responseBody, LogonResponse.class);

            authToken = logonResponse.getToken();
            lastAuthenticationTimestamp = Instant.now();
            log.debug("logon() - auth token: {}", authToken);

        } catch (Exception e) {
            log.warn("logon() - error: {}", e.getMessage());
            lastAuthenticationTimestamp = null;
        }

    }


    /**
     * Logoff from the HMC and remove any session
     *
     */
    synchronized void logoff() {

        if(authToken == null) {
            return;
        }

        try {

            URL url = new URL(String.format("%s/rest/api/web/Logon", baseUrl));
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonRequest")
                .addHeader("X-API-Session", authToken)
                .delete()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
            } catch (IOException e) {
                log.warn("logoff() error: {}", e.getMessage());
            } finally {
                authToken = null;
                lastAuthenticationTimestamp = null;
            }

        } catch (MalformedURLException e) {
            log.warn("logoff() - error: {}", e.getMessage());
        }

    }


    public String getRequest(String urlPath) throws IOException {
        URL absUrl = new URL(String.format("%s%s", baseUrl, urlPath));
        return getRequest(absUrl);
    }

    public String postRequest(String urlPath, String payload) throws IOException {
        URL absUrl = new URL(String.format("%s%s", baseUrl, urlPath));
        return postRequest(absUrl, payload);
    }


    /**
     * Return a Response from the HMC
     * @param url to get Response from
     * @return Response body string
     * @throws IOException
     */
    public synchronized String getRequest(URL url) throws IOException {

        log.debug("getRequest() - URL: {}", url.toString());
        if (lastAuthenticationTimestamp == null || lastAuthenticationTimestamp.plus(MAX_MINUTES_BETWEEN_AUTHENTICATION, ChronoUnit.MINUTES).isBefore(Instant.now())) {
            login();
        }

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("X-API-Session", (authToken == null ? "" : authToken))
            .get().build();

        String responseBody;
        try (Response response = httpClient.newCall(request).execute()) {

            responseBody = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {

                // Auth. failure
                if(response.code() == 401) {
                    log.warn("getRequest() - 401 - login and retry.");

                    // Let's login again and retry
                    login();
                    return retryGetRequest(url);
                }

                log.error("getRequest() - Unexpected response: {} for URL {}", response.code(), url);
                return null;
            }

        }

        return responseBody;
    }


    private String retryGetRequest(URL url) throws IOException {

        log.debug("retryGetRequest() - URL: {}", url.toString());

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("X-API-Session", (authToken == null ? "" : authToken))
            .get().build();

        String responseBody = null;
        try (Response responseRetry = httpClient.newCall(request).execute()) {
            if(responseRetry.isSuccessful()) {
                responseBody = Objects.requireNonNull(responseRetry.body()).string();
            }
        }
        return responseBody;
    }


    /**
     * Send a POST request with a payload (can be null) to the HMC
     * @param url
     * @param payload
     * @return Response body string
     * @throws IOException
     */
    public synchronized String postRequest(URL url, String payload) throws IOException {

        log.debug("sendPostRequest() - URL: {}", url.toString());
        if (lastAuthenticationTimestamp == null || lastAuthenticationTimestamp.plus(MAX_MINUTES_BETWEEN_AUTHENTICATION, ChronoUnit.MINUTES).isBefore(Instant.now())) {
            login();
        }

        RequestBody requestBody;
        if(payload != null) {
            requestBody = RequestBody.create(payload, MEDIA_TYPE_IBM_XML_POST);
        } else {
            requestBody = RequestBody.create("", null);
        }

        Request request = new Request.Builder()
            .url(url)
            .addHeader("content-type", "application/xml")
            .addHeader("X-API-Session", (authToken == null ? "" : authToken) )
            .post(requestBody).build();

        String responseBody;
        try (Response response = httpClient.newCall(request).execute()) {
            responseBody = Objects.requireNonNull(response.body()).string();

            if (!response.isSuccessful()) {
                response.close();
                //log.warn(responseBody);
                log.error("sendPostRequest() - Unexpected response: {}", response.code());
                throw new IOException("sendPostRequest() - Unexpected response: " + response.code());
            }
        }

        return responseBody;
    }


    /**
     * Provide an unsafe (ignoring SSL problems) OkHttpClient
     *
     * @return OkHttpClient ignoring SSL/TLS errors
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {  }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create a ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            builder.connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);
            builder.writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);
            builder.readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS);

            return builder.build();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Get OkHttpClient with our preferred timeout values.
     * @return OkHttpClient
     */
    private static OkHttpClient getSafeOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS);
        return builder.build();
    }



    /*
    private void writeTraceFile(String id, String json) {

        String fileName = String.format("%s-%s.json", id, Instant.now().toString());
        try {
            log.debug("Writing trace file: " + fileName);
            File traceFile = new File(traceDir, fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            log.warn("writeTraceFile() - " + e.getMessage());
        }
    }
    */

}
