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

    private final String baseUrl
    private final String username
    private final String password

    //protected Map<String,ManagedSystem> managedSystems = new HashMap<String, ManagedSystem>()
    protected String authToken
    private final OkHttpClient client


    HmcClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl
        this.username = username
        this.password = password

        //this.client = new OkHttpClient() // OR Unsafe (ignore SSL errors) below
        this.client = getUnsafeOkHttpClient()
    }



    /**
     * Logon to the HMC and get an authentication token for further requests.
     *
     * @throws IOException
     */
    void login() throws IOException {

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

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        // Get response body and parse
        String responseBody = response.body.string()

        def xml = new XmlSlurper().parseText(responseBody)
        authToken = xml.toString()

        log.debug("login() - Auth Token: " + authToken)
    }



    /**
     * Logoff from the HMC and remove any session
     *
     */
    void logoff() {
        URL absUrl = new URL(String.format("%s/rest/api/web/Logon", baseUrl))
        Request request = new Request.Builder()
                .url(absUrl)
                .addHeader("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonRequest")
                .addHeader("X-API-Session", authToken)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        this.authToken = null
        log.debug("logoff()")

    }



    /**
     * Return Map of ManagedSystems seen by this HMC
     *
     * @return
     */
    Map<String, ManagedSystem> getManagedSystems() {

        log.debug("getManagedSystems()")

        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem", baseUrl))
        Response response = getResponse(url)
        String responseBody = response.body.string()
        Map<String,ManagedSystem> managedSystemsMap = new HashMap<String, ManagedSystem>()

        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            entry.content.each { content ->
                content.ManagedSystem.each { system ->
                    ManagedSystem managedSystem = new ManagedSystem(entry.id as String)
                    managedSystem.name  = system.SystemName
                    managedSystem.model = system.MachineTypeModelAndSerialNumber.Model
                    managedSystem.type = system.MachineTypeModelAndSerialNumber.MachineType
                    managedSystem.serialNumber = system.MachineTypeModelAndSerialNumber.SerialNumber
                    managedSystemsMap.put(managedSystem.id, managedSystem)
                    log.debug("getManagedSystems() " + managedSystem.toString())
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
    Map<String, LogicalPartition> getLogicalPartitionsForManagedSystemWithId(String systemId) {
        log.debug("getLogicalPartitionsForManagedSystem() - systemId: " + systemId)

        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem/%s/LogicalPartition", baseUrl, systemId))
        Response response = getResponse(url)
        String responseBody = response.body.string()

        Map<String, LogicalPartition> partitionMap = new HashMap<String, LogicalPartition>() {}
        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            //log.debug("Entry")
            entry.content.each { content ->
                //log.debug("Content")
                content.LogicalPartition.each { partition ->
                    LogicalPartition logicalPartition = new LogicalPartition(partition.PartitionUUID as String, systemId)
                    logicalPartition.name  = partition.PartitionName
                    logicalPartition.type  = partition.PartitionType
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
    String getPcmDataForManagedSystem(String systemId) {
        log.debug("getPcmDataForManagedSystem() - " + systemId)
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, systemId))
        Response response = getResponse(url)
        String responseBody = response.body.string()

        String jsonBody

        // Parse XML and fetch JSON link
        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            String link = entry.link["@href"]
            if(entry.category["@term"] == "ManagedSystem") {
                jsonBody = getReponseBody(new URL(link))
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
    String getPcmDataForLogicalPartition(String systemId, String partitionId) {

        log.debug(String.format("getPcmDataForLogicalPartition() - %s @ %s", partitionId, systemId))
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/LogicalPartition/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, systemId, partitionId))
        Response response = getResponse(url)
        String responseBody = response.body.string()

        //log.debug(responseBody)
        String jsonBody

        // Parse XML and fetch JSON link
        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            String link = entry.link["@href"]
            if(entry.category["@term"] == "LogicalPartition") {
                jsonBody = getReponseBody(new URL(link))
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
    protected String getReponseBody(URL url) {
        //log.debug("getBody() - " + url.toString())
        Response response = getResponse(url)
        return response.body.string()
    }



    /**
     * Return a Response from the HMC
     *
     * @param url
     * @return
     */
    private Response getResponse(URL url) {

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("X-API-Session", authToken)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

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