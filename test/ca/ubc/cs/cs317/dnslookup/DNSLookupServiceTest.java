package ca.ubc.cs.cs317.dnslookup;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DNSLookupServiceTest {

    private DNSLookupService service;
    private Random random;
    private DNSCache cache;
    private DNSLookupCUI dnsLookupCUI;

    @BeforeEach
    public void startServer() throws SocketException, UnknownHostException {
        this.cache = DNSCache.getInstance();
        this.dnsLookupCUI = new DNSLookupCUI();
        this.service = new DNSLookupService("127.0.0.1", dnsLookupCUI);
        this.random = new Random();
    }


    private void buildQueryCommonTest(DNSQuestion question) {
        DNSMessage message = service.buildQuery(question);

        DNSMessage checkable = turnaround(message);
        checkQuery(question, checkable, message.getID());
    }

    private int checkQuery(DNSQuestion question, DNSMessage message, int transactionId) {
        // Reset message for checking
        int receivedTransactionId = message.getID();

        // Check transaction ID
        Assertions.assertEquals(transactionId, receivedTransactionId, "Returned transaction ID does not match ID used in query message");


        // Check if flags are set to zero (separate errors)
        Assertions.assertFalse(message.getQR(), "Message type (QR) not set to false (query)");
        Assertions.assertEquals(0, message.getOpcode(), "Operation code (OPCODE) not set to zero (standard query)");
        Assertions.assertFalse(message.getAA(), "Authoritative answer (AA) not set to false");
        Assertions.assertFalse(message.getRD(), "Recursive query requested (expected iterative query)");
        Assertions.assertFalse(message.getRA(), "Recursive query available not set to false");
        Assertions.assertEquals(0, message.getRcode(), "Rcode not set to zero");

        // Record counts
        Assertions.assertEquals(1, message.getQDCount(), "Incorrect number of queries");
        Assertions.assertEquals(0, message.getANCount(), "Incorrect number of answers");
        Assertions.assertEquals(0, message.getNSCount(), "Incorrect number of nameservers");
        Assertions.assertEquals(0, message.getARCount(), "Incorrect number of additional records");

        Assertions.assertEquals(question, message.getQuestion(), "Question not correctly encoded");

        return receivedTransactionId;
    }

    private void checkRRs(Collection<ResourceRecord> rrs) {
        for (ResourceRecord rr : rrs) {
            DNSQuestion question = rr.getQuestion();

            List<ResourceRecord> results = cache.getCachedResults(question, false);
            System.out.println(results);
            Assertions.assertTrue(results.contains(rr));
        }
    }

    private void processResponseCommonTest(boolean authoritative, int errorCode,
                                           Collection<DNSQuestion> questions,
                                           Collection<ResourceRecord> answers,
                                           Collection<ResourceRecord> nameservers,
                                           Collection<ResourceRecord> additional) {
        int responseID = random.nextInt(0x10000);
        DNSMessage response = buildResponse(responseID, authoritative, errorCode, questions, answers, nameservers, additional);

        Set<ResourceRecord> returnedNameservers = service.processResponse(turnaround(response));

        checkRRs(answers);
        checkRRs(nameservers);
        checkRRs(additional);
        Set<ResourceRecord> filteredNameservers = nameservers.stream().filter(r -> r.getRecordType() == RecordType.NS).collect(Collectors.toSet());

        Assertions.assertEquals(filteredNameservers, returnedNameservers,
                "Returned nameservers don't match the list of nameservers");
    }

    private DNSMessage turnaround(DNSMessage msg) {
        byte[] data = msg.getUsed();
        return new DNSMessage(data, data.length);
    }

    private DNSMessage buildResponse(int transactionID, boolean authoritative, int errorCode,
                                      Collection<DNSQuestion> questions,
                                      Collection<ResourceRecord> answers,
                                      Collection<ResourceRecord> nameservers,
                                      Collection<ResourceRecord> additional) {
        DNSMessage response = new DNSMessage((short)transactionID);
        response.setAA(authoritative);
        response.setRcode(errorCode);
        response.setQR(true);

        for (DNSQuestion question : questions)
            response.addQuestion(question);
        for (ResourceRecord record : answers)
            response.addResourceRecord(record, "answer");
        for (ResourceRecord record : nameservers)
            response.addResourceRecord(record, "nameserver");
        for (ResourceRecord record : additional)
            response.addResourceRecord(record, "additional");
        return response;
    }


    @Test
    public void testBuildQueryA() {
        buildQueryCommonTest(new DNSQuestion("ubc.ca", RecordType.A, RecordClass.IN));
    }

    @Test
    public void testProcessResponseSingleAnswer() throws UnknownHostException {
        DNSQuestion question = new DNSQuestion("www.cs.ubc.ca", RecordType.A, RecordClass.IN);
        processResponseCommonTest(true, 0,
                Collections.singleton(question),
                Collections.singleton(new ResourceRecord(question, 3600, InetAddress.getByName("35.24.11.129"))),
                Collections.emptySet(), Collections.emptySet());

        processResponseCommonTest(false, 0,
                Collections.singleton(question),
                Collections.singleton(new ResourceRecord(question, 16482, InetAddress.getByName("103.233.44.22"))),
                Collections.emptySet(), Collections.emptySet());
    }

    @Test
    public void testProcessResponse() throws UnknownHostException{
        DNSQuestion question = new DNSQuestion("www.cs.ubc.ca", RecordType.A, RecordClass.IN);
        processResponseCommonTest(true, 0,
                Collections.singleton(question),
                Collections.singleton(new ResourceRecord(question, 3600, InetAddress.getByName("35.24.11.129"))),
                Collections.emptySet(), Collections.emptySet());
        processResponseCommonTest(false, 0,
                Collections.singleton(question),
                Collections.singleton(new ResourceRecord(question, 16482, InetAddress.getByName("103.233.44.22"))),
                Collections.emptySet(), Collections.emptySet());

    }
    @Test
    public void testIndividualQuery() throws UnknownHostException{
        //Record type needs to be A to recieve the result, otw, error 8082
//l ca.prairielearn.com
        DNSQuestion question = new DNSQuestion("groups.yahoo.com", RecordType.A, RecordClass.IN);
        dnsLookupCUI.setVerbose(true);
        Collection<ResourceRecord> rrs = service.individualQueryProcess(question,InetAddress.getByName("199.7.91.13"));
        String[] ss ={"l",question.getHostName()};
       dnsLookupCUI.main(ss);


    }
}
