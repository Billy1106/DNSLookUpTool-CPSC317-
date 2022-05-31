package ca.ubc.cs.cs317.dnslookup;

import java.net.InetAddress;

public interface DNSVerbosePrinter {

    void printQueryToSend(DNSQuestion question, InetAddress server, int transactionID);

    void printResponseHeaderInfo(int receivedTransactionId, boolean authoritative, int errorCode);

    void printAnswersHeader(int num_answers);
    void printNameserversHeader(int num_nameservers);
    void printAdditionalInfoHeader(int num_additional);

    void printIndividualResourceRecord(ResourceRecord record, int typeCode, int classCode);
}
