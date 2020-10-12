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

import biz.nellemann.hmci.Configuration.HmcObject;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class HmcClient {

    private final static Logger log = LoggerFactory.getLogger(HmcClient.class);

    private final MediaType MEDIA_TYPE_IBM_XML_LOGIN = MediaType.parse("application/vnd.ibm.powervm.web+xml; type=LogonRequest");

    private final String hmcId;
    private final String baseUrl;
    private final String username;
    private final String password;

    protected Integer responseErrors = 0;
    protected String authToken;
    private final OkHttpClient client;


    HmcClient(HmcObject configHmc) {

        this.hmcId = configHmc.name;
        this.baseUrl = configHmc.url;
        this.username = configHmc.username;
        this.password = configHmc.password;
        Boolean unsafe = configHmc.unsafe;

        if(unsafe) {
            this.client = getUnsafeOkHttpClient();
        } else {
            this.client = new OkHttpClient();
        }

    }


    /**
     * Logon to the HMC and get an authentication token for further requests.
     */
    void login() throws Exception {
        this.login(false);
    }


    /**
     * Logon to the HMC and get an authentication token for further requests.
     * @param force
     */
    void login(Boolean force) throws Exception {

        if(authToken != null && !force) {
            return;
        }

        log.info("Connecting to HMC - " + baseUrl);

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
                //.addHeader("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonRequest")
                .addHeader("Accept", "application/vnd.ibm.powervm.web+xml; type=LogonResponse")
                .addHeader("X-Audit-Memento", "hmci")
                .put(RequestBody.create(payload.toString(), MEDIA_TYPE_IBM_XML_LOGIN))
                .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body and parse
            String responseBody = Objects.requireNonNull(response.body()).string();
            Objects.requireNonNull(response.body()).close();

            Document doc = Jsoup.parse(responseBody);
            authToken = doc.select("X-API-Session").text();

            log.debug("login() - Auth Token: " + authToken);
        } catch (MalformedURLException e) {
            log.error("login() - url error", e);
            throw new Exception(new Throwable("Login URL Error: " + e.getMessage()));
        } catch(Exception e) {
            log.error("login() - general error", e);
            throw new Exception(new Throwable("Login General Error: " + e.getMessage()));
        }

    }



    /**
     * Logoff from the HMC and remove any session
     *
     */
    void logoff() throws IOException {

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

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        authToken = null;
        log.debug("logoff()");
    }



    /**
     * Return Map of ManagedSystems seen by this HMC
     *
     * @return Map of system-id and ManagedSystem
     */
    Map<String, ManagedSystem> getManagedSystems() throws Exception {

        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem", baseUrl));
        Response response = getResponse(url);
        String responseBody = Objects.requireNonNull(response.body()).string();
        Map<String,ManagedSystem> managedSystemsMap = new HashMap<>();

        // Do not try to parse empty response
        if(responseBody.isEmpty() || responseBody.length() <= 1) {
            responseErrors++;
            return managedSystemsMap;
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Elements managedSystems = doc.select("ManagedSystem|ManagedSystem");    //  doc.select("img[src$=.png]");
            for(Element el : managedSystems) {
                ManagedSystem system = new ManagedSystem(
                    hmcId,
                    el.select("Metadata > Atom > AtomID").text(),
                    el.select("SystemName").text(),
                    el.select("MachineTypeModelAndSerialNumber > MachineType").text(),
                    el.select("MachineTypeModelAndSerialNumber > Model").text(),
                    el.select("MachineTypeModelAndSerialNumber > SerialNumber").text()
                );
                managedSystemsMap.put(system.id, system);
                log.debug("getManagedSystems() - Found system: " + system.toString());
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
        Response response = getResponse(url);
        String responseBody = Objects.requireNonNull(response.body()).string();
        Map<String, LogicalPartition> partitionMap = new HashMap<String, LogicalPartition>() {};

        // Do not try to parse empty response
        if(responseBody.isEmpty() || responseBody.length() <= 1) {
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
                log.debug("getLogicalPartitionsForManagedSystem() - Found partition: " + logicalPartition.toString());
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

        log.debug("getPcmDataForManagedSystem() - " + system.id);
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, system.id));
        Response response = getResponse(url);
        String responseBody = Objects.requireNonNull(response.body()).string();
        String jsonBody = null;

        // Do not try to parse empty response
        if(responseBody.isEmpty() || responseBody.length() <= 1) {
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
                log.debug("getPcmDataForManagedSystem() - json url: " + href);
                jsonBody = getResponseBody(new URL(href));
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

        log.debug(String.format("getPcmDataForLogicalPartition() - %s @ %s", partition.id, partition.system.id));
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/LogicalPartition/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, partition.system.id, partition.id));
        Response response = getResponse(url);
        String responseBody = Objects.requireNonNull(response.body()).string();
        String jsonBody = null;

        // Do not try to parse empty response
        if(responseBody.isEmpty() || responseBody.length() <= 1) {
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
                log.debug("getPcmDataForLogicalPartition() - json url: " + href);
                jsonBody = getResponseBody(new URL(href));
            }

        } catch(Exception e) {
            log.warn("getPcmDataForLogicalPartition() - xml parse error", e);
        }

        return jsonBody;
    }


    /**
     * Return body text from a HTTP response from the HMC
     *
     * @param url URL to get response body as String
     * @return String with http reponse body
     */
    protected String getResponseBody(URL url) throws Exception {
        Response response = getResponse(url);
        String body = Objects.requireNonNull(response.body()).string();
        Objects.requireNonNull(response.body()).close();
        return body;
    }


    /**
     * Return a Response from the HMC
     * @param url to get Response from
     * @return Response object
     */
    private Response getResponse(URL url) throws Exception {
        return getResponse(url, 0);
    }


    /**
     * Return a Response from the HMC
     * @param url to get Response from
     * @param retry number of retries for this call
     * @return Response object
     */
    private Response getResponse(URL url, Integer retry) throws Exception {

        log.debug("getResponse() - " + url.toString());

        if(responseErrors > 2) {
            responseErrors = 0;
            login(true);
            return getResponse(url, retry++);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("X-API-Session", authToken)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            Objects.requireNonNull(response.body()).close();

            if(response.code() == 401) {
                login(true);
                return getResponse(url, retry++);
            }

            if(retry < 2) {
                log.warn("getResponse() - Retrying due to unexpected response: " + response.code());
                return getResponse(url, retry++);
            }

            log.error("getResponse() - Unexpected response: " + response.code());
            throw new IOException("getResponse() - Unexpected response: " + response.code());
        }

        return response;
    }



    /**
     * Provide an unsafe (ignoring SSL problems) OkHttpClient
     *
     * @return
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
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

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
