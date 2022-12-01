package biz.nellemann.hmci;

import org.mockserver.integration.ClientAndServer
import org.mockserver.logging.MockServerLogger
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.mockserver.socket.PortFactory
import org.mockserver.socket.tls.KeyStoreFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.net.ssl.HttpsURLConnection
import java.util.concurrent.TimeUnit

@Stepwise
class RestClientTest extends Specification {

    @Shared
    private static ClientAndServer mockServer;

    @Shared
    private RestClient serviceClient


    def setupSpec() {
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
        mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
        serviceClient = new RestClient(String.format("http://localhost:%d", mockServer.getPort()), "user", "password", true)
    }

    def cleanupSpec() {
        mockServer.stop()
    }

    def setup() {
        mockServer.reset()
    }


    def "Test GET Request"() {
        setup:
        def req = HttpRequest.request()
            .withMethod("GET")
            .withPath("/test/get")
        def res = HttpResponse.response()
            .withDelay(TimeUnit.SECONDS, 1)
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "text/plain"),
            )
            .withBody("myTestData", MediaType.TEXT_PLAIN)
        mockServer.when(req).respond(res)

        when:
        String response = serviceClient.getRequest("/test/get")

        then:
        response == "myTestData"
    }



    def "Test POST Request"() {
        setup:
        def req = HttpRequest.request()
            .withMethod("POST")
            .withPath("/test/post")
        def res = HttpResponse.response()
            .withDelay(TimeUnit.SECONDS, 1)
            .withStatusCode(202)
            .withHeaders(
                new Header("Content-Type", "text/plain; charset=UTF-8"),
            )
            .withBody("Created, OK.", MediaType.TEXT_PLAIN)
        mockServer.when(req).respond(res)

        when:
        String response = serviceClient.postRequest("/test/post", null)

        then:
        response == "Created, OK."
    }



    def "Test HMC Login"() {
        setup:
        def responseFile = new File(getClass().getResource('/hmc-logon-response.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("PUT")
            .withPath("/rest/api/web/Logon")

        def res = HttpResponse.response()
            .withDelay(TimeUnit.SECONDS, 1)
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonResponse"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)

        when:
        serviceClient.login()

        then:
        serviceClient.authToken == "tKVhm4YD0bS0qjaYuXI8b2ZbOvh8MlGV1Inivvwd7L3VTUyP0J3j6pLcaaW-Ek3xPWLuF2-kLZDabjUvukWyyM69ytjz6LGZRK4VI_qsz5KEfwg6weIp6olXId-S6ioNP_CcKfjwkL6HZMMWyLSmMrSA0cz7QVkvoUeBB6mAFK_LjI1SBfCXupnVEsKdFpH9FdSS4_s-LrDend__MC1Xqm9_7xq-GN_J5tiE1zwSXvY="
    }



    def "Test HMC Request"() {
        setup:
        def responseFile = new File(getClass().getResource('/1-hmc.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("GET")
            .withPath("/rest/api/uom/ManagementConsole")

        def res = HttpResponse.response()
            .withDelay(TimeUnit.SECONDS, 1)
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/atom+xml; charset=UTF-8"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)

        when:
        def xml = serviceClient.getRequest("/rest/api/uom/ManagementConsole")

        then:
        xml == responseFile.getText('UTF-8')
    }


    def "Test HMC Logoff"() {
        when:
        serviceClient.logoff()

        then:
        serviceClient.authToken == null
    }

}


