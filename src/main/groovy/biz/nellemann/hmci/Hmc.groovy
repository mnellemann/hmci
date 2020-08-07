package biz.nellemann.hmci


import groovy.json.JsonSlurper
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
class Hmc {

    private static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain; charset=utf-8");
    private static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml; charset=utf-8");
    private static final MediaType MEDIA_TYPE_IBM_XML_LOGIN = MediaType.parse("application/vnd.ibm.powervm.web+xml; type=LogonRequest");

    private final String baseUrl
    private final String username
    private final String password

    protected Map<String,ManagedSystem> managedSystems = new HashMap<String, ManagedSystem>()
    protected String authToken
    private final OkHttpClient client


    Hmc(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl
        this.username = username
        this.password = password

        //this.client = new OkHttpClient()
        this.client = getUnsafeOkHttpClient()
    }


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


    void getManagedSystems() {

        log.debug("getManagedSystems()")

        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem", baseUrl))
        Response response = getResponse(url)
        String responseBody = response.body.string()

        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            //log.debug("Entry")
            entry.content.each { content ->
                //log.debug("Content")
                content.ManagedSystem.each { system ->
                    ManagedSystem managedSystem = new ManagedSystem(entry.id as String)
                    managedSystem.name  = system.SystemName
                    managedSystem.model = system.MachineTypeModelAndSerialNumber.Model
                    managedSystem.type = system.MachineTypeModelAndSerialNumber.MachineType
                    managedSystem.serialNumber = system.MachineTypeModelAndSerialNumber.SerialNumber
                    managedSystems.put(managedSystem.id, managedSystem)
                    log.debug("getManagedSystems() " + managedSystem.toString())
                }
            }
        }
    }


    void getLogicalPartitions() {
        log.debug("getLogicalPartitions()")
        managedSystems.each {
            getLogicalPartitionsForManagedSystem(it.getValue())
        }
    }


    void getLogicalPartitionsForManagedSystem(ManagedSystem system) {
        log.debug("getLogicalPartitionsForManagedSystem() - " + system.name)

        URL url = new URL(String.format("%s/rest/api/uom/ManagedSystem/%s/LogicalPartition", baseUrl, system.id))
        Response response = getResponse(url)
        String responseBody = response.body.string()
        //log.debug(responseBody)

        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            //log.debug("Entry")
            entry.content.each { content ->
                //log.debug("Content")
                content.LogicalPartition.each { partition ->
                    LogicalPartition logicalPartition = new LogicalPartition(partition.PartitionUUID as String)
                    logicalPartition.name  = partition.PartitionName
                    logicalPartition.type  = partition.PartitionType
                    system.partitions.put(logicalPartition.id, logicalPartition)
                    log.debug("getLogicalPartitionsForManagedSystem() " + logicalPartition.toString())
                }
            }
        }


    }


    void getProcessedMetrics() {
        managedSystems.each {
            getProcessedMetricsForManagedSystem(it.getValue())
        }
    }


    void getProcessedMetricsForManagedSystem(ManagedSystem system) {
        log.debug("getProcessedMetricsForManagedSystem() " - system.name)
        URL url = new URL(String.format("%s/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=1", baseUrl, system.id))
        Response response = getResponse(url)
        String responseBody = response.body.string()
        //log.debug(responseBody)

        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            String link = entry.link["@href"]
            //linksList.add(link)
            switch (entry.category["@term"]) {
                case "ManagedSystem":
                    processPcmJsonForManagedSystem(getPcmJsonForManagedSystem(link))
                    break
                case "LogicalPartition":
                    //processPcmJsonForLogicalPartition(getPcmJsonForLogicalPartition(getProcessedMetricsForLogicalPartition(link)))
                    break
                default:
                    log.warn("Unknown category: " + entry.category["@term"])
                    break
            }
        }
    }


    /**
     * Parse XML to get JSON Link
     * @param pcmUrl
     */
    String getProcessedMetricsForLogicalPartition(String pcmUrl) {
        log.debug("getProcessedMetricsForLogicalPartition() - " + pcmUrl)
        URL url = new URL(pcmUrl)
        Response response = getResponse(url)
        String responseBody = response.body.string()

        String link
        def feed = new XmlSlurper().parseText(responseBody)
        feed?.entry?.each { entry ->
            link = entry.link["@href"]
        }

        return link
    }


    String getPcmJsonForManagedSystem(String jsonUrl) {
        log.debug("getPcmJsonForManagedSystem() - " + jsonUrl)
        URL url = new URL(jsonUrl)
        Response response = getResponse(url)
        return response.body.string()
    }

    String getPcmJsonForLogicalPartition(String jsonUrl) {
        log.debug("getPcmJsonForLogicalPartition() - " + jsonUrl)
        URL url = new URL(jsonUrl)
        Response response = getResponse(url)
        return response.body.string()
    }


    void processPcmJsonForManagedSystem(String json) {
        log.debug("processPcmJsonForManagedSystem()")
        def jsonObject = new JsonSlurper().parseText(json)
        String systemUuid = (String)jsonObject?.systemUtil?.utilInfo?.uuid
        if(systemUuid && managedSystems.containsKey(systemUuid)) {
            log.debug("processPcmJsonForManagedSystem() - Found UUID for ManagedSystem: " + systemUuid)
            ManagedSystem system = managedSystems.get(systemUuid)
            // TODO: Store metrics
            system.processMetrics()
        }
    }

    void processPcmJsonForLogicalPartition(String json) {
        log.debug("processPcmJsonForLogicalPartition()")

        def jsonObject = new JsonSlurper().parseText(json)
        String systemUuid = (String)jsonObject?.utilInfo?.uuid

        if(systemUuid && managedSystems.containsKey(systemUuid)) {

            log.debug("processPcmJsonForLogicalPartition() - Found UUID for ManagedSystem: " + systemUuid)
            ManagedSystem system = managedSystems.get(systemUuid)
            String lparUuid = (String)jsonObject?.utilSamples?.lparsUtil[0][0]?.uuid

            if(lparUuid && system.partitions.containsKey(lparUuid)) {

                log.debug("processPcmJsonForLogicalPartition() - Found UUID for LogicalPartition: " + lparUuid)
                LogicalPartition lpar = system.partitions.get(lparUuid)
                // TODO: Store metrics
                lpar.processMetrics()

            }

        }

    }


    private Response getResponse(URL url) {
        //log.debug("getResponse() - " + url.toString())

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("X-API-Session", authToken)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        // TODO: Better error detection

        return response
    }


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