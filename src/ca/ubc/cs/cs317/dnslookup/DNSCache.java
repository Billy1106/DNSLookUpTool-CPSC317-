package ca.ubc.cs.cs317.dnslookup;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles a cache of DNS results. It is based on a map that links nodes (queries) to a set of resource
 * records (results). Cached results are only maintained for the duration of the TTL (time-to-live) returned by the
 * server, and are deleted before being returned to the user.
 */
public class DNSCache {

    public static final DNSQuestion rootQuestion = new DNSQuestion("", RecordType.NS, RecordClass.IN);
    private static final Set<ResourceRecord> rootNameServersMap = Stream.of("198.41.0.4",
                    "199.9.14.201", "192.33.4.12", "199.7.91.13", "192.203.230.10", "192.5.5.241", "192.112.36.4",
                    "198.97.190.53", "192.36.148.17", "192.58.128.30", "193.0.14.129", "199.7.83.42", "202.12.27.33")
            .map(s -> new ResourceRecord(rootQuestion, Integer.MAX_VALUE, s))
            .collect(Collectors.toSet());

    private static final DNSCache instance = new DNSCache();

    private final Map<DNSQuestion, Set<ResourceRecord>> cachedResults = new TreeMap<>();

    public DNSCache() {
        reset();
    }

//    public Map<DNSQuestion, Set<ResourceRecord>> getCachedResults(){
//        return cachedResults;
//    }

    /**
     * Singleton retrieval method. Only one instance of the DNS cache can be created. This method returns the single DNS
     * cache instance.
     *
     * @return Instance of a DNS cache.
     */
    public static DNSCache getInstance() {
        return instance;
    }

    /**
     * Resets the cache to its initial value, containing only root nameservers.
     */
    public void reset() {
        this.cachedResults.clear();
        this.cachedResults.put(rootQuestion, rootNameServersMap);
    }

    /**
     * Returns a set of resource records already cached for a particular query. If no results are cached for the
     * specified query, returns an empty set. Expired results are removed from the cache before being returned. This
     * method does not perform the query itself, it only returns previously cached results. Results are returned in
     * random order.
     *
     * @param question     DNS query (host name/type/class) for the results to be obtained.
     * @param includeCname Set to true to indicate that records for the same FQDN but with a CNAME record type should be
     *                     included in the result. Set to false to return only results for the direct query. Has no
     *                     effect if the question itself is a request for CNAME records.
     * @return A potentially empty set of resources associated to the query.
     */
    public List<ResourceRecord> getCachedResults(DNSQuestion question, boolean includeCname) {
        List<ResourceRecord> returningList = new ArrayList<>();
        Set<ResourceRecord> results = cachedResults.get(question);
        if (results != null) {
            results.removeIf(ResourceRecord::isExpired);
            returningList.addAll(results);
        }

        if (includeCname && question.getRecordType() != RecordType.CNAME) {
            results = cachedResults.get(new DNSQuestion(question.getHostName(), RecordType.CNAME, question.getRecordClass()));
            if (results != null) {
                results.removeIf(ResourceRecord::isExpired);
                returningList.addAll(results);
            }
        }

        Collections.shuffle(returningList);
        return returningList;
    }

    /**
     * Adds a specific resource record to the DNS cache. If the cache already has an equivalent resource record, the
     * existing record is updated if the new one expires after the existing record.
     *
     * @param record Resource record, possibly obtained from a DNS server, containing the result of a DNS query.
     */
    public void addResult(ResourceRecord record) {

        if (record.isExpired()) return;

        Set<ResourceRecord> results = cachedResults.computeIfAbsent(record.getQuestion(), q -> new HashSet<>());

        // Find a record for the same question containing the same result
        ResourceRecord oldRecord = results.stream().filter(record::equals).findFirst().orElse(null);

        if (oldRecord == null)
            results.add(record);
        else
            oldRecord.update(record);
    }

    /**
     * Perform a specific action for each query and its set of cached records. This action can be specified using a
     * lambda expression or method name. Expired records are removed before the action is performed.
     *
     * @param consumer Action to be performed for each query and set of records.
     */
    public void forEachQuestion(BiConsumer<DNSQuestion, Collection<ResourceRecord>> consumer) {
        cachedResults.forEach((question, records) -> {
            records.removeIf(ResourceRecord::isExpired);
            if (!records.isEmpty())
                consumer.accept(question, records);
        });
    }

    /**
     * Perform a specific action for each query and individual record. This action can be specified using a lambda
     * expression or method name. Expired records are removed before the action is performed.
     *
     * @param consumer Action to be performed for each query and record.
     */
    public void forEachRecord(BiConsumer<DNSQuestion, ResourceRecord> consumer) {
        forEachQuestion((question, records) -> records.forEach(record -> consumer.accept(question, record)));
    }

}
