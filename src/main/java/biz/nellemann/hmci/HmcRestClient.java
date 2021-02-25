/*
 *    Copyright 2020 Mark Nellemann <mark.nellemann@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package biz.nellemann.hmci;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class HmcRestClient {

    private final static Logger log = LoggerFactory.getLogger(HmcRestClient.class);

    private final MediaType MEDIA_TYPE_IBM_XML_LOGIN = MediaType.parse("application/vnd.ibm.powervm.web+xml; type=LogonRequest");

    protected Integer responseErrors = 0;
    protected String authToken;
    private final OkHttpClient client;

    // OkHttpClient timeouts
    private final static int CONNECT_TIMEOUT = 30;
    private final static int WRITE_TIMEOUT = 30;
    private final static int READ_TIMEOUT = 30;

    private final String baseUrl;
    private final String username;
    private final String password;


    HmcRestClient(String url, String username, String password, Boolean unsafe) {

        this.baseUrl = url;
        this.username = username;
        this.password = password;

        if(unsafe) {
            this.client = getUnsafeOkHttpClient();
        } else {
            this.client = getSafeOkHttpClient();
        }

    }


    @Override
    public String toString() {
        return baseUrl;
    }


    /**
     * Logon to the HMC and get an authentication token for further requests.
     */
    synchronized void login() throws Exception {

        log.debug("Connecting to HMC - " + baseUrl);

        StringBuilder payload = new StringBuilder();
        payload.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
        payload.append("<LogonRequest xmlns='http://www.ibm.com/xmlns/systems/power/firmware/web/mc/2012_10/' schemaVersion='V1_0'>");
        payload.append("<UserID>").append(username).append("</UserID>");
        payload.append("<Password>").append(password).append("</Password>");
        payload.append("</LogonRequest>");

        try {
            URL url = new URL(String.format("%s/rest/api/web/Logon", baseUrl));
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.ibm.powervm.web+xml; type=LogonResponse")
                .addHeader("X-Audit-Memento", "hmci")
                .put(RequestBody.create(payload.toString(), MEDIA_TYPE_IBM_XML_LOGIN))
                .build();

            Response response = client.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                log.warn("login() - Unexpected response: " + response.code());
                throw new IOException("Unexpected code: " + response);
            }

            Document doc = Jsoup.parse(responseBody);
            authToken = doc.select("X-API-Session").text();
            log.debug("login() - Auth Token: " + authToken);
        } catch (MalformedURLException e) {
            log.error("login() - URL Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("login() - Error: " + e.getMessage());
            throw e;
        }

    }



    /**
     * Logoff from the HMC and remove any session
     *
     */
    synchronized void logoff() throws IOException {

        if(authToken == null) {
            return;
        }

        URL absUrl = new URL(String.format("%s/rest/api/web/Logon", baseUrl));
        Request request = new Request.Builder()
                .url(absUrl)
                .addHeader("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonRequest")
                .addHeader("X-API-Session", authToken)
                .delete()
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            log.warn("logoff() error: " + e.getMessage());
        } finally {
            authToken = null;
        }

    }



    /**
     * Return Map of ManagedSystems seen by this HMC
     *
     * @return Map of system-id and ManagedSystem
     */
    Map<String, ManagedSystem> getManagedSystems() throws Exception {

        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem", baseUrl));
        String responseBody = sendGetRequest(url);
        Map<String,ManagedSystem> managedSystemsMap = new HashMap<>();

        // Do not try to parse empty response
        if(responseBody == null || responseBody.isEmpty() || responseBody.length() <= 1) {
            responseErrors++;
            return managedSystemsMap;
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Elements managedSystems = doc.select("ManagedSystem|ManagedSystem");    //  doc.select("img[src$=.png]");
            for(Element el : managedSystems) {
                ManagedSystem system = new ManagedSystem(
                    el.select("Metadata > Atom > AtomID").text(),
                    el.select("SystemName").text(),
                    el.select("MachineTypeModelAndSerialNumber > MachineType").text(),
                    el.select("MachineTypeModelAndSerialNumber > Model").text(),
                    el.select("MachineTypeModelAndSerialNumber > SerialNumber").text()
                );
                managedSystemsMap.put(system.id, system);
                log.debug("getManagedSystems() - Found system: " + system);
            }

        } catch(Exception e) {
            log.warn("getManagedSystems() - xml parse error", e);
        }

        return managedSystemsMap;
    }



    /**
     * Return Map of LogicalPartitions seen by a ManagedSystem on this HMC
     * @param system a valid ManagedSystem
     * @return Map of partition-id and LogicalPartition
     */
    Map<String, LogicalPartition> getLogicalPartitionsForManagedSystem(ManagedSystem system) throws Exception {
        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem/%s/LogicalPartition", baseUrl, system.id));
        String responseBody = sendGetRequest(url);
        Map<String, LogicalPartition> partitionMap = new HashMap<>();

        // Do not try to parse empty response
        if(responseBody == null || responseBody.isEmpty() || responseBody.length() <= 1) {
            responseErrors++;
            return partitionMap;
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Elements logicalPartitions = doc.select("LogicalPartition|LogicalPartition");    //  doc.select("img[src$=.png]");
            for(Element el : logicalPartitions) {
                LogicalPartition logicalPartition = new LogicalPartition(
                    el.select("PartitionUUID").text(),
                    el.select("PartitionName").text(),
                    el.select("PartitionType").text(),
                    system
                );
                partitionMap.put(logicalPartition.id, logicalPartition);
                log.debug("getLogicalPartitionsForManagedSystem() - Found partition: " + logicalPartition);
            }

        } catch(Exception e) {
            log.warn("getLogicalPartitionsForManagedSystem() - xml parse error", e);
        }

        return partitionMap;
    }


    /**
     * Parse XML feed to get PCM Data in JSON format
     * @param system a valid ManagedSystem
     * @return JSON string with PCM data for this ManagedSystem
     */
    String getPcmDataForManagedSystem(ManagedSystem system) throws Exception {

        log.trace("getPcmDataForManagedSystem() - " + system.id);
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, system.id));
        String responseBody = sendGetRequest(url);
        String jsonBody = null;

        // Do not try to parse empty response
        if(responseBody == null || responseBody.isEmpty() || responseBody.length() <= 1) {
            responseErrors++;
            log.warn("getPcmDataForManagedSystem() - empty response");
            return null;
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Element entry = doc.select("feed > entry").first();
            Element link = entry.select("link[href]").first();

            if(link.attr("type").equals("application/json")) {
                String href = link.attr("href");
                log.trace("getPcmDataForManagedSystem() - json url: " + href);
                jsonBody = sendGetRequest(new URL(href));
            }

        } catch(Exception e) {
            log.warn("getPcmDataForManagedSystem() - xml parse error", e);
        }

        return jsonBody;
    }


    /**
     * Parse XML feed to get PCM Data in JSON format
     * @param partition a valid LogicalPartition
     * @return JSON string with PCM data for this LogicalPartition
     */
    String getPcmDataForLogicalPartition(LogicalPartition partition) throws Exception {

        log.trace(String.format("getPcmDataForLogicalPartition() - %s @ %s", partition.id, partition.system.id));
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/LogicalPartition/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, partition.system.id, partition.id));
        String responseBody = sendGetRequest(url);
        String jsonBody = null;

        // Do not try to parse empty response
        if(responseBody == null || responseBody.isEmpty() || responseBody.length() <= 1) {
            responseErrors++;
            log.warn("getPcmDataForLogicalPartition() - empty response");
            return null;
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Element entry = doc.select("feed > entry").first();
            Element link = entry.select("link[href]").first();

            if(link.attr("type").equals("application/json")) {
                String href = link.attr("href");
                log.trace("getPcmDataForLogicalPartition() - json url: " + href);
                jsonBody = sendGetRequest(new URL(href));
            }

        } catch(Exception e) {
            log.warn("getPcmDataForLogicalPartition() - xml parse error", e);
        }

        return jsonBody;
    }


    /**
     * Parse XML feed to get PCM Data in JSON format.
     * Does not work for older HMC (pre v9) and older Power server (pre Power 8).
     * @param systemEnergy a valid SystemEnergy
     * @return JSON string with PCM data for this SystemEnergy
     */
    String getPcmDataForEnergy(SystemEnergy systemEnergy) throws Exception {

        log.trace("getPcmDataForEnergy() - " + systemEnergy.system.id);
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?Type=Energy&NoOfSamples=1", baseUrl, systemEnergy.system.id));
        String responseBody = sendGetRequest(url);
        String jsonBody = null;
        //log.info(responseBody);

        // Do not try to parse empty response
        if(responseBody == null || responseBody.isEmpty() || responseBody.length() <= 1) {
            responseErrors++;
            log.trace("getPcmDataForEnergy() - empty response");
            return null;
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Element entry = doc.select("feed > entry").first();
            Element link = entry.select("link[href]").first();

            if(link.attr("type").equals("application/json")) {
                String href = link.attr("href");
                log.trace("getPcmDataForEnergy() - json url: " + href);
                jsonBody = sendGetRequest(new URL(href));
            }

        } catch(Exception e) {
            log.warn("getPcmDataForEnergy() - xml parse error", e);
        }

        return jsonBody;
    }


    /**
     * Set EnergyMonitorEnabled preference to true, if possible.
     * @param system
     */
    void enableEnergyMonitoring(ManagedSystem system) {

        log.trace("enableEnergyMonitoring() - " + system.id);
        try {
            URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/preferences", baseUrl, system.id));
            String responseBody = sendGetRequest(url);
            String jsonBody = null;

            // Do not try to parse empty response
            if(responseBody == null || responseBody.isEmpty() || responseBody.length() <= 1) {
                responseErrors++;
                log.warn("enableEnergyMonitoring() - empty response");
                return;
            }

            Document doc = Jsoup.parse(responseBody, "", Parser.xmlParser());
            doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
            doc.outputSettings().prettyPrint(false);
            doc.outputSettings().charset("US-ASCII");
            Element entry = doc.select("feed > entry").first();
            Element link1 = entry.select("EnergyMonitoringCapable").first();
            Element link2 = entry.select("EnergyMonitorEnabled").first();

            if(link1.text().equals("true")) {
                log.debug("enableEnergyMonitoring() - EnergyMonitoringCapable == true");
                if(link2.text().equals("false")) {
                    //log.warn("enableEnergyMonitoring() - EnergyMonitorEnabled == false");
                    link2.text("true");

                    Document content = Jsoup.parse(doc.select("Content").first().html(), "", Parser.xmlParser());
                    content.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
                    content.outputSettings().prettyPrint(false);
                    content.outputSettings().charset("UTF-8");
                    String updateXml = content.outerHtml();

                    sendPostRequest(url, updateXml);
                }
            } else {
                log.warn("enableEnergyMonitoring() - EnergyMonitoringCapable == false");
            }

        } catch (Exception e) {
            log.warn("enableEnergyMonitoring() - Exception: " + e.getMessage());
        }
    }



    /**
     * Return a Response from the HMC
     * @param url to get Response from
     * @return Response body string
     */
    private String sendGetRequest(URL url) throws Exception {

        log.trace("getResponse() - " + url.toString());
        if(authToken == null) {
            return null;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("X-API-Session", authToken)
                .get().build();

        Response response = client.newCall(request).execute();
        String body = Objects.requireNonNull(response.body()).string();

        if (!response.isSuccessful()) {

            response.close();

            if(response.code() == 401) {
                log.warn("getResponse() - 401 - login and retry.");
                authToken = null;
                login();
                return null;
            }

            log.error("getResponse() - Unexpected response: " + response.code());
            throw new IOException("getResponse() - Unexpected response: " + response.code());
        }

        return body;
    }


    /**
     * Send a POST request with a payload (can be null) to the HMC
     * @param url
     * @param payload
     * @return
     * @throws Exception
     */
    public String sendPostRequest(URL url, String payload) throws Exception {

        log.trace("sendPostRequest() - " + url.toString());
        if(authToken == null) {
            return null;
        }

        RequestBody requestBody;
        if(payload != null) {
            //log.debug("sendPostRequest() - payload: " + payload);
            requestBody = RequestBody.create(payload, MediaType.get("application/xml"));
        } else {
            requestBody = RequestBody.create("", null);
        }


        Request request = new Request.Builder()
            .url(url)
            //.addHeader("Content-Type", "application/xml")
            .addHeader("content-type", "application/xml")
            .addHeader("X-API-Session", authToken)
            .post(requestBody).build();

        Response response = client.newCall(request).execute();
        String body = Objects.requireNonNull(response.body()).string();

        if (!response.isSuccessful()) {
            response.close();
            log.warn(body);
            log.error("sendPostRequest() - Unexpected response: " + response.code());
            throw new IOException("sendPostRequest() - Unexpected response: " + response.code());
        }

        return body;
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

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
            builder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
            builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

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
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        return builder.build();
    }

}
