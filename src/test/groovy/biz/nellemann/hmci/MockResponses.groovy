package biz.nellemann.hmci

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType

class MockResponses {


    static void prepareClientResponseForLogin(ClientAndServer mockServer) {

        File responseFile = new File("src/test/resources/hmc-logon-response.xml")
        //def responseFile = new File(getClass().getResource('/hmc-logon-response.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("PUT")
            .withPath("/rest/api/web/Logon")

        def res = HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/vnd.ibm.powervm.web+xml; type=LogonResponse"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)
    }


    static void prepareClientResponseForManagementConsole(ClientAndServer mockServer) {
        File responseFile = new File("src/test/resources/1-hmc.xml")
        //def responseFile = new File(getClass().getResource('/1-hmc.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("GET")
            .withPath("/rest/api/uom/ManagementConsole")

        def res = HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/atom+xml; charset=UTF-8"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)
    }


    static void prepareClientResponseForManagedSystem(ClientAndServer mockServer) {
        File responseFile = new File("src/test/resources/2-managed-system.xml")
        //def responseFile = new File(getClass().getResource('/2-managed-system.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("GET")
            .withPath("/rest/api/uom/ManagementConsole/[0-9a-z-]+/ManagedSystem/.*")

        def res = HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/atom+xml; charset=UTF-8"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)
    }


    static void prepareClientResponseForLogicalPartition(ClientAndServer mockServer) {
        File responseFile = new File("src/test/resources/3-lpar.xml")
        //def responseFile = new File(getClass().getResource('/3-lpar.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("GET")
            .withPath("/rest/api/uom/ManagedSystem/[0-9a-z-]+/LogicalPartition/.*")

        def res = HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/atom+xml; charset=UTF-8"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)
    }


    static void prepareClientResponseForVirtualIOServer(ClientAndServer mockServer) {
        File responseFile = new File("src/test/resources/2-vios.xml")
        //def responseFile = new File(getClass().getResource('/2-vios.xml').toURI())
        def req = HttpRequest.request()
            .withMethod("GET")
            .withPath("/rest/api/uom/ManagedSystem/[0-9a-z-]+/VirtualIOServer/.*")

        def res = HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(
                new Header("Content-Type", "application/atom+xml; charset=UTF-8"),
            )
            .withBody(responseFile.getText('UTF-8'), MediaType.XML_UTF_8)

        mockServer.when(req).respond(res)
    }


}
