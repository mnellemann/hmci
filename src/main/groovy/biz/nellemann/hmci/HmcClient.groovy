/**
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
package biz.nellemann.hmci

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate;

@Slf4j
class HmcClient {

    private final MediaType MEDIA_TYPE_IBM_XML_LOGIN = MediaType.parse("application/vnd.ibm.powervm.web+xml; type=LogonRequest");

    private final String hmcId
    private final String baseUrl
    private final String username
    private final String password
    private final Boolean unsafe

    protected String authToken
    private final OkHttpClient client

    HmcClient(String hmcId, String baseUrl, String username, String password, Boolean unsafe = false) {
        this.hmcId = hmcId
        this.baseUrl = baseUrl
        this.username = username
        this.password = password
        this.unsafe = unsafe

        if(unsafe) {
            this.client = getUnsafeOkHttpClient()
        } else {
            this.client = new OkHttpClient()
        }
    }



    /**
     * Logon to the HMC and get an authentication token for further requests.
     *
     * @throws IOException
     */
    void login(Boolean force = false) throws IOException {

        if(authToken && !force) {
            return
        }

        String payload = """\
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<LogonRequest xmlns="http://www.ibm.com/xmlns/systems/power/firmware/web/mc/2012_10/" schemaVersion="V1_0">
  <UserID>${username}</UserID>
  <Password>${password}</Password>
</LogonRequest>"""

        URL url = new URL(String.format("%s/rest/api/web/Logon", baseUrl))
        Request request = new Request.Builder()
                .url(url)
                //.addHeader("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonRequest")
                .addHeader("Accept", "application/vnd.ibm.powervm.web+xml; type=LogonResponse")
                .addHeader("X-Audit-Memento", "hmci")
                .put(RequestBody.create(payload, MEDIA_TYPE_IBM_XML_LOGIN))
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body and parse
            String responseBody = response.body.string()
            response.body().close()

            def xml = new XmlSlurper().parseText(responseBody)
            authToken = xml.toString()

            log.debug("login() - Auth Token: " + authToken)
        } catch(Exception e) {
            log.error(e.message)
            throw new Exception(e)
        }

    }



    /**
     * Logoff from the HMC and remove any session
     *
     */
    void logoff() {

        if(!authToken) {
            return
        }

        URL absUrl = new URL(String.format("%s/rest/api/web/Logon", baseUrl))
        Request request = new Request.Builder()
                .url(absUrl)
                .addHeader("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonRequest")
                .addHeader("X-API-Session", authToken)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        authToken = null
        log.debug("logoff()")
    }



    /**
     * Return Map of ManagedSystems seen by this HMC
     *
     * @return
     */
    Map<String, ManagedSystem> getManagedSystems() {
        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem", baseUrl))
        Response response = getResponse(url)
        String responseBody = response.body.string()
        Map<String,ManagedSystem> managedSystemsMap = new HashMap<String, ManagedSystem>()

        // Do not try to parse empty response
        if(responseBody.empty || responseBody.size() < 1) {
            return managedSystemsMap
        }

        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            entry.content.each { content ->
                content.ManagedSystem.each { system ->
                    ManagedSystem managedSystem = new ManagedSystem(
                            hmcId,
                            entry.id as String,
                            system.SystemName as String,
                            system.MachineTypeModelAndSerialNumber?.MachineType as String,
                            system.MachineTypeModelAndSerialNumber?.Model as String,
                            system.MachineTypeModelAndSerialNumber?.SerialNumber as String
                    )
                    managedSystemsMap.put(managedSystem.id, managedSystem)
                    log.debug("getManagedSystems() - Found system: " + managedSystem.toString())
                }
            }
        }

        return managedSystemsMap
    }



    /**
     * Return Map of LogicalPartitions seen by a ManagedSystem on this HMC

     * @param UUID of managed system
     * @return
     */
    Map<String, LogicalPartition> getLogicalPartitionsForManagedSystem(ManagedSystem system) {
        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem/%s/LogicalPartition", baseUrl, system.id))
        Response response = getResponse(url)
        String responseBody = response.body.string()
        Map<String, LogicalPartition> partitionMap = new HashMap<String, LogicalPartition>() {}

        // Do not try to parse empty response
        if(responseBody.empty || responseBody.size() < 1) {
            return partitionMap
        }

        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            //log.debug("Entry")
            entry.content.each { content ->
                //log.debug("Content")
                content.LogicalPartition.each { partition ->
                    LogicalPartition logicalPartition = new LogicalPartition(
                            partition.PartitionUUID as String,
                            partition.PartitionName as String,
                            partition.PartitionType as String,
                            system
                    )
                    partitionMap.put(logicalPartition.id, logicalPartition)
                    log.debug("getLogicalPartitionsForManagedSystem() - Found partition: " + logicalPartition.toString())
                }
            }
        }

        return partitionMap
    }



    /**
     * Parse XML feed to get PCM Data in JSON format
     * @param systemId
     * @return
     */
    String getPcmDataForManagedSystem(ManagedSystem system) {
        log.debug("getPcmDataForManagedSystem() - " + system.id)
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, system.id))
        Response response = getResponse(url)
        String responseBody = response.body.string()

        String jsonBody

        // Parse XML and fetch JSON link
        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            String link = entry.link["@href"]
            if(entry.category["@term"] == "ManagedSystem") {
                jsonBody = getResponseBody(new URL(link))
            }
        }

        return jsonBody
    }


    /**
     * Parse XML feed to get PCM Data in JSON format
     * @param systemId
     * @param partitionId
     * @return
     */
    String getPcmDataForLogicalPartition(LogicalPartition partition) {

        log.debug(String.format("getPcmDataForLogicalPartition() - %s @ %s", partition.id, partition.system.id))
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/LogicalPartition/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, partition.system.id, partition.id))
        Response response = getResponse(url)
        String responseBody = response.body.string()

        //log.debug(responseBody)
        String jsonBody

        // Parse XML and fetch JSON link
        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            String link = entry.link["@href"]
            if(entry.category["@term"] == "LogicalPartition") {
                jsonBody = getResponseBody(new URL(link))
            }
        }

        return jsonBody
    }


    /**
     * Return body text from a HTTP response from the HMC
     *
     * @param url
     * @return
     */
    protected String getResponseBody(URL url) {
        Response response = getResponse(url)
        String body = response.body().string()
        response.body().close()
        return body
    }



    /**
     * Return a Response from the HMC
     *
     * @param url
     * @return
     */
    private Response getResponse(URL url, Integer retry = 0) {

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("X-API-Session", authToken)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            response.body().close()

            if(response.code == 401) {
                login(true)
                return getResponse(url, retry++)
            }

            if(retry < 2) {
                log.warn("getResponse() - Retrying due to unexpected response: " + response.code)
                return getResponse(url, retry++)
            }

            log.error("getResponse() - Unexpected response: " + response.code)
            throw new IOException("getResponse() - Unexpected response: " + response.code)
        };

        return response
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
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
