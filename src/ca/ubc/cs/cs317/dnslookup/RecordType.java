package ca.ubc.cs.cs317.dnslookup;

import java.util.Arrays;

/**
 * Record types supported by the application. Includes a few common record types that are not
 * fully supported by this application, but that are sometimes returned by nameservers for regular DNS queries.
 */
public enum RecordType {
    A(1), NS(2), CNAME(5), SOA(6), MX(15), AAAA(28), OTHER(0);

    private final int code;

    RecordType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Returns the record type associated to a particular code, or OTHER if no known record
     * type is linked to that code.
     *
     * @param code The record type code to be searched.
     * @return A record type that uses the specified code, or OTHER if no record type uses the code.
     */
    public static RecordType getByCode(int code) {
        return Arrays.stream(values()).filter(v -> v.code == code).findFirst().orElse(OTHER);
    }
}
