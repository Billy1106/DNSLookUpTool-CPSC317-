package ca.ubc.cs.cs317.dnslookup;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
public class DNSMessage {
    public static final int MAX_DNS_MESSAGE_LENGTH = 512;
    /**
     * TODO:  You will add additional constants and fields
     */
    private static int lenPosition = 0;
    private static int size = 0;
    private static int qNum = 0;
    public static final int QUERY = 0;

    private final Map<String, Integer> nameToPosition = new HashMap<>();
    private final Map<Integer, String> positionToName = new HashMap<>();
    private final ByteBuffer buffer;


    /**
     * Initializes an empty DNSMessage with the given id.
     *
     * @param id The id of the message.
     */
    public DNSMessage(short id) {
        this.buffer = ByteBuffer.allocate(MAX_DNS_MESSAGE_LENGTH);
        // TODO: Complete this method
        //Headers
        this.size = MAX_DNS_MESSAGE_LENGTH;
        namePositionInitializer();

        setID(id);
        setQR(false);
        setOpcode(0);
        setAA(false);
        setTC(false);
        setRD(false);
        setRA(false);
        setRcode(0);
        setQDCount(0);
        setARCount(0);
        buffer.position(12);


    }

    /**
     * Initializes a DNSMessage with the first length bytes of the given byte array.
     *
     * @param recvd The byte array containing the received message
     * @param length The length of the data in the array
     */
    public DNSMessage(byte[] recvd, int length) {
        buffer = ByteBuffer.wrap(recvd, 0, length);
        // TODO: Complete this method
        this.size = length;
        namePositionInitializer();
        if(getQR()){
            //resource
            buffer.position(12);
            getQuestion();
        }else{
            //question
            buffer.position(12);
        }
    }
    public void namePositionInitializer(){
        nameToPosition.put("ID",0);
        nameToPosition.put("QR",16);
        nameToPosition.put("Opcode",17);
        nameToPosition.put("AA",21);
        nameToPosition.put("TC",22);
        nameToPosition.put("RD",23);
        nameToPosition.put("RA",24);
        nameToPosition.put("Z",25);
        nameToPosition.put("RCODE",28);
        nameToPosition.put("QDCOUNT",32);
        nameToPosition.put("ANCOUNT",48);
        nameToPosition.put("NSCOUNT",64);
        nameToPosition.put("ARCOUNT",80);
        nameToPosition.put("QNAME",96);
    }

    /**
     * Getters and setters for the various fixed size and fixed location fields of a DNSMessage
     * TODO:  They are all to be completed
     */

    public int getID() {
        int pos = buffer.position();
        buffer.position(nameToPosition.get("ID"));//initial set
        int id = buffer.getShort()&0x0000FFFF;
        buffer.position(pos);
        return id;
    }

    public void setID(int id) {
        int pos = buffer.position();
        buffer.position(nameToPosition.get("ID"));
        buffer.putShort((short)id);
        buffer.position(pos);
    }

    public boolean getQR() {
        int pos = buffer.position();
        int qrPos = nameToPosition.get("QR")/8;

        buffer.position(qrPos);
        boolean qr= (byte)(buffer.get()|0x7F)==(byte)0xFF;
       buffer.position(pos);
        return qr;
    }

    public void setQR(boolean qr) {
        int pos = buffer.position();
        int qrPos = nameToPosition.get("QR")/8;
        buffer.position(qrPos);
        if(qr)
            buffer.put(qrPos,(byte)(buffer.get()|0x80));
        else
            buffer.put(qrPos,(byte)(buffer.get()&0x7F));
        buffer.position(pos);
    }

    public boolean getAA() {
        int current = buffer.position();
        int pos = nameToPosition.get("AA")/8;
        buffer.position(pos);
        boolean aa = (byte)((buffer.get()<<nameToPosition.get("AA")%8)|0x7F)==(byte)0xFF;
        buffer.position(current);
        return aa;

    }

    public void setAA(boolean aa) {
        int current = buffer.position();
        int rdPos = nameToPosition.get("AA")/8;
        buffer.position(rdPos);
        if(aa)
            buffer.put(rdPos,(byte)((buffer.get()|0x80>>nameToPosition.get("AA")%8)));
        else
            buffer.put(rdPos,(byte)(buffer.get()&0xFB));
        buffer.position(current);
    }

    public int getOpcode() {
        int current = buffer.position();
        buffer.position(nameToPosition.get("Opcode")/8);
        int opcode = (((buffer.get()>>3)&0x0F));
        buffer.position(current);
        return opcode;
    }

    public void setOpcode(int opcode) {
        int current = buffer.position();
        int pos = nameToPosition.get("Opcode")/8;//2
        buffer.position(pos);

        buffer.put(pos,(byte)((buffer.get()|(byte)opcode<<3)));//not working
        buffer.position(current);

    }

    public boolean getTC() {
        int current = buffer.position();
        int rdPos = nameToPosition.get("TC")/8;
        buffer.position(rdPos);
        boolean rd = (byte)((buffer.get()<<nameToPosition.get("TC")%8)|0x7F)==(byte)0xFF;
        buffer.position(current);
        return rd;

    }

    public void setTC(boolean tc) {
        int current = buffer.position();
        int pos = nameToPosition.get("TC")/8;
        buffer.position(pos);

        if(tc)
            buffer.put(pos,(byte)((buffer.get()|0x80>>nameToPosition.get("TC")%8)));
        else
            buffer.put(pos,(byte)(buffer.get()&0xED));

        buffer.position(current);
    }

    public boolean getRD() {
        int current = buffer.position();
        int rdPos = nameToPosition.get("RD")/8;
        buffer.position(rdPos);
        boolean rd = (byte)((buffer.get()<<nameToPosition.get("RD")%8)|0x7F)==(byte)0xFF;
        buffer.position(current);
        return rd;

    }

    public void setRD(boolean rd) {
        int current = buffer.position();
        int rdPos = nameToPosition.get("RD")/8;
        buffer.position(rdPos);
        if(rd)
            buffer.put(rdPos,(byte)((buffer.get()|0x80>>nameToPosition.get("RD")%8)));
        else
            buffer.put(rdPos,(byte)(buffer.get()&0xFE));
        buffer.position(current);
    }

    public boolean getRA() {
        int current = buffer.position();
        int pos = nameToPosition.get("RA")/8;
        buffer.position(pos);
        boolean ra = (byte)((buffer.get()<<nameToPosition.get("RA")%8)|0x7F)==(byte)0xFF;
        buffer.position(current);
        return ra;

    }

    public void setRA(boolean ra) {
        int current = buffer.position();
        int pos = nameToPosition.get("RA")/8;
        buffer.position(pos);
        if(ra)
            buffer.put(pos,(byte)((buffer.get()|0x80>>nameToPosition.get("RA")%8)));
        else
            buffer.put(pos,(byte)(buffer.get()&0x7F));
        buffer.position(current);
    }

    public int getRcode() {
        int current = buffer.position();
        buffer.position(nameToPosition.get("RCODE")/8);
        byte x = (buffer.get());
        int rcode = (x&0x0F);
        buffer.position(current);
        return rcode;

    }

    public void setRcode(int rcode) {
        int current = buffer.position();
        int pos = nameToPosition.get("RCODE")/8;
        buffer.position(pos);
        buffer.put(pos,(byte)(((buffer.get()&11110000)|(byte)rcode)));
        buffer.position(current);

    }

    public int getQDCount() {
        int current = buffer.position();
        buffer.position(nameToPosition.get("QDCOUNT")/8);

        int qdcount = buffer.getShort()&0x0000FFFF;
        buffer.position(current);

        return qdcount;
    }

    public void setQDCount(int count) {
        int current = buffer.position();
        int pos = nameToPosition.get("QDCOUNT")/8;
        buffer.position(pos);
        buffer.putShort((short)count);
        buffer.position(current);


    }

    public int getANCount() {
        int current = buffer.position();
        buffer.position(nameToPosition.get("ANCOUNT")/8);
        int ancount = buffer.getShort()&0x0000FFFF;
        buffer.position(current);
        return ancount;
    }

    public int getNSCount() {
        int current = buffer.position();
        buffer.position(nameToPosition.get("NSCOUNT")/8);

        int nscount = buffer.getShort()&0x0000FFFF;

//
        buffer.position(current);
        return nscount;
    }

    public int getARCount() {
        int current = buffer.position();
        buffer.position(nameToPosition.get("ARCOUNT")/8);

        int racount = buffer.getShort()&0x0000FFFF;
      buffer.position(current);
        return racount;
    }

    public void setARCount(int count) {
        int current = buffer.position();
        int pos = nameToPosition.get("ARCOUNT")/8;
        buffer.position(pos);
        buffer.putShort((short)count);
        buffer.position(current);
    }
//original implemented
    public void setANCount(int count) {
        int current = buffer.position();
        int pos = nameToPosition.get("ANCOUNT")/8;
        buffer.position(pos);
        buffer.putShort((short)count);
        buffer.position(current);
    }

    /**
     * Return the name at the current position() of the buffer.  This method is provided for you,
     * but you should ensure that you understand what it does and how it does it.
     *
     * The trick is to keep track of all the positions in the message that contain names, since
     * they can be the target of a pointer.  We do this by storing the mapping of position to
     * name in the positionToName map.
     *
     * @return The decoded name
     */
    public String getName() {
        // Remember the starting position for updating the name cache
        int start = buffer.position();
        int len = buffer.get() & 0xff;
        if (len == 0) return "";
        if ((len & 0xc0) == 0xc0) {  // This is a pointer
            int pointer = ((len & 0x3f) << 8) | (buffer.get() & 0xff);
            String suffix = positionToName.get(pointer);
            assert suffix != null;
            positionToName.put(start, suffix);
            return suffix;
        }
        byte[] bytes = new byte[len];
        buffer.get(bytes, 0, len);
        String label = new String(bytes, StandardCharsets.UTF_8);
        String suffix = getName();
        String answer = suffix.isEmpty() ? label : label + "." + suffix;
        positionToName.put(start, answer);
        return answer;
    }
    public String getResponse() {
        // Remember the starting position for updating the name cache
        int start = buffer.position();
        int len = buffer.get() & 0xff;

        if (len == 0) return "";

        byte[] bytes = new byte[len];
        buffer.get(bytes, 0, len);
        String label = new String(bytes, StandardCharsets.UTF_8);
        String suffix = getName();
        String answer = suffix.isEmpty() ? label : label + "." + suffix;
        return answer;
    }
    public String getResponse6(int len) {

        String hex = byteArrayToHexString(buffer.array());
        int pos = buffer.position()*2;
        String strPattern = "^0+(?!$)";
        String answer = hex.substring(pos,pos+len/4).replaceAll(strPattern, "");;
        pos = pos+len/4 ;
        for(int i=1;i<len/2;i++){
            String temp = hex.substring(pos,pos+len/4);
            //System.out.println(temp);
            temp = temp.replaceAll(strPattern, "");
            answer+= ":"+temp;
            pos = pos+len/4;
        }


//

        return answer;
    }



    /**
     * The standard toString method that displays everything in a message.
     * @return The string representation of the message
     */
    public String toString() {
        // Remember the current position of the buffer so we can put it back
        // Since toString() can be called by the debugger, we want to be careful to not change
        // the position in the buffer.  We remember what it was and put it back when we are done.
        int end = buffer.position();
        final int DataOffset = 12;
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(getID()).append(' ');
            sb.append("QR: ").append(getQR()).append(' ');
            sb.append("OP: ").append(getOpcode()).append(' ');
            sb.append("AA: ").append(getAA()).append('\n');
            sb.append("TC: ").append(getTC()).append(' ');
            sb.append("RD: ").append(getRD()).append(' ');
            sb.append("RA: ").append(getRA()).append(' ');
            sb.append("RCODE: ").append(getRcode()).append(' ')
                    .append(dnsErrorMessage(getRcode())).append('\n');
            sb.append("QDCount: ").append(getQDCount()).append(' ');
            sb.append("ANCount: ").append(getANCount()).append(' ');
            sb.append("NSCount: ").append(getNSCount()).append(' ');
            sb.append("ARCount: ").append(getARCount()).append('\n');

            buffer.position(DataOffset);

            showQuestions(getQDCount(), sb);

            showRRs("Authoritative", getANCount(), sb);
            showRRs("Name servers", getNSCount(), sb);
            showRRs("Additional", getARCount(), sb);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "toString failed on DNSMessage";
        }
        finally {
            buffer.position(end);
        }
    }

    /**
     * Add the text representation of all the questions (there are nq of them) to the StringBuilder sb.
     *
     * @param nq Number of questions
     * @param sb Collects the string representations
     */
    private void showQuestions(int nq, StringBuilder sb) {

        sb.append("Question [").append(nq).append("]\n");
        for (int i = 0; i < nq; i++) {

            DNSQuestion question = getQuestion();
            sb.append('[').append(i).append(']').append(' ').append(question).append('\n');
        }
    }

    /**
     * Add the text representation of all the resource records (there are nrrs of them) to the StringBuilder sb.
     *
     * @param kind Label used to kind of resource record (which section are we looking at)
     * @param nrrs Number of resource records
     * @param sb Collects the string representations
     */
    private void showRRs(String kind, int nrrs, StringBuilder sb) {
        sb.append(kind).append(" [").append(nrrs).append("]\n");
        for (int i = 0; i < nrrs; i++) {
            ResourceRecord rr = getRR();
            sb.append('[').append(i).append(']').append(' ').append(rr).append('\n');
            //System.out.println(rr.toString());
        }
    }
    public int showPosition(){
        return buffer.position();
    }
    /**
     * Decode and return the question that appears next in the message.  The current position in the
     * buffer indicates where the question starts.
     *
     * @return The decoded question
     */
    public DNSQuestion getQuestion() {
        // TODO: Complete this method

        String dnsName = getName();
        //System.out.println("dns name " + dnsName);
        RecordType type = RecordType.getByCode(buffer.getShort()&0x0000FFFF);
        RecordClass classes = RecordClass.getByCode(buffer.getShort()&0x0000FFFF);
        DNSQuestion dnsQuestion = new DNSQuestion(dnsName,type,classes);

        return dnsQuestion;
    }
    public void showBuffer(){
        String str = byteArrayToHexString(buffer.array());


        //System.out.println(str);
    }

    /**
     * Decode and return the resource record that appears next in the message.  The current
     * position in the buffer indicates where the resource record starts.
     *
     * @return The decoded resource record
     */
    public ResourceRecord getRR() {
        // TODO: Complete this method


        String dnsName = getName();
        RecordType rt =  RecordType.getByCode(buffer.getShort());
        RecordClass rc =  RecordClass.getByCode(buffer.getShort());

        int ttl = buffer.getInt();

        String address = "";
        short len = buffer.getShort();

        int pos = buffer.position();

        if(rt.equals(RecordType.A) ){
            buffer.position(pos);
            address = ""+Byte.toUnsignedInt(buffer.get());
            for (int i = 1; i < len; i++) {
                address += "."+Byte.toUnsignedInt(buffer.get());
            }
        }else if(rt.equals(RecordType.AAAA)){
            address = getResponse6(len);
            buffer.getLong();
            buffer.getLong();
        }else if(rt.equals(RecordType.MX)) {
            buffer.getShort();
            address = getName();
            return new ResourceRecord(new DNSQuestion(dnsName,rt,rc),ttl,address);
        }else{
            //System.out.println("get at " + buffer.position());
            address = getName();
            return new ResourceRecord(new DNSQuestion(dnsName,rt,rc),ttl,address);
        }
        InetAddress iaddress = null;
        try {
            iaddress = InetAddress.getByName(address);
        }catch (Exception e){
           System.out.println(e.getMessage());
        }
        return new ResourceRecord(new DNSQuestion(dnsName,rt,rc),ttl,iaddress);
    }

    /**
     * Helper function that returns a hex string representation of a byte array. May be used to represent the result of
     * records that are returned by a server but are not supported by the application (e.g., SOA records).
     *
     * @param data a byte array containing the record data.
     * @return A string containing the hex value of every byte in the data.
     */
    private static String byteArrayToHexString(byte[] data) {
        return IntStream.range(0, data.length).mapToObj(i -> String.format("%02x", data[i])).reduce("", String::concat);
    }

    /**
     * Add an encoded name to the message. It is added at the current position and uses compression
     * as much as possible.  Compression is accomplished by remembering the position of every added
     * label.
     *
     * @param name The name to be added
     */
    public void addName(String name) {
        String label;
        while (name.length() > 0) {
            Integer offset = nameToPosition.get(name);
            if (offset != null) {
                int pointer = offset;
                pointer |= 0xc000;
                buffer.putShort((short)pointer);
                return;
            } else {
                nameToPosition.put(name, buffer.position());

                int dot = name.indexOf('.');
                label = (dot > 0) ? name.substring(0, dot) : name;
                buffer.put((byte)label.length());
                for (int j = 0; j < label.length(); j++) {

                    buffer.put((byte)label.charAt(j));
                }
                name = (dot > 0) ? name.substring(dot + 1) : "";
            }
        }
        buffer.put((byte)0);

    }
    public void addResource(String name) {
        String label;
        buffer.putShort((short)name.length());
//        System.out.println("before");
//        showBuffer();
        while (name.length() > 0) {
            //Integer offset = nameToPosition.get(name);
            int dot = name.indexOf('.');
            label = (dot > 0) ? name.substring(0, dot) : name;
            buffer.put((byte)label.length());
            for (int j = 0; j < label.length(); j++) {
                buffer.put((byte)label.charAt(j));
            }
            name = (dot > 0) ? name.substring(dot + 1) : "";
        }
        buffer.put((byte)0);

//        System.out.println("after");
//        showBuffer();
    }
    public void addResource6(String name) {
        String label;
        buffer.putShort((byte)0x0010);
        //System.out.println(name.length());
        while (name.length() > 0) {
            //Integer offset = nameToPosition.get(name);
            int dot = name.indexOf(':');
            label = (dot > 0) ? name.substring(0, dot) : name;
            //buffer.put((byte)label.length());
            String value = "";
            for (int j = 0; j < label.length(); j++) {
                value+=label.charAt(j);
            }
            buffer.putShort((short)Integer.parseInt(String.format("%04x",Integer.parseInt(value, 16)), 16));

            name = (dot > 0) ? name.substring(dot + 1) : "";
        }

//        System.out.println("after");
//        showBuffer();
    }
    public void addResource4(String name) {
        String label;
        buffer.putShort((byte)0x0004);
        while (name.length() > 0) {
            //Integer offset = nameToPosition.get(name);
            int dot = name.indexOf('.');
            label = (dot > 0) ? name.substring(0, dot) : name;
            //buffer.put((byte)label.length());
            String value = "";
            for (int j = 0; j < label.length(); j++) {
                value+=label.charAt(j);
            }
            buffer.put((byte)Integer.parseInt(value));
            //System.out.println(value);
            name = (dot > 0) ? name.substring(dot + 1) : "";
        }

//        System.out.println("after");
//        showBuffer();
    }

    /**
     * Add an encoded question to the message at the current position.
     * @param question The question to be added
     */
    public void addQuestion(DNSQuestion question) {
        // TODO: Complete this method

        addName(question.getHostName());
        addQType(question.getRecordType());
        addQClass(question.getRecordClass());
        int init = buffer.position();
        int qdcount = getQDCount()+1;
        setQDCount(qdcount);
        buffer.position(init);

    }

    /**
     * Add an encoded resource record to the message at the current position.
     * @param rr The resource record to be added
     */
    public void addResourceRecord(ResourceRecord rr,String section) {
        // TODO: Complete this method
        //System.out.println("set rr at" + buffer.position());
//        addName(rr.getQuestion().getHostName());
//        addQType(rr.getQuestion().getRecordType());
//        addQClass(rr.getQuestion().getRecordClass());

        addName(rr.getHostName());
        RecordType rt =rr.getRecordType();
        addQType(rt);
        RecordClass rc =rr.getRecordClass();
        addQClass(rc);
        buffer.putInt((int)rr.getRemainingTTL());

        String rs = rr.getTextResult();
        char dot = '.';
        if(rt.equals(RecordType.AAAA)) dot = ':';
        String temp = "";
        int count = 0;
        //System.out.println("before");
        //System.out.println(rs);
        showBuffer();
        if(rt.equals(RecordType.AAAA)){
            addResource6(rs);
        }else if(rt.equals(RecordType.A)){
            addResource4(rs);
        }else if (rt.equals(RecordType.MX)){
            buffer.getShort();
            addResource(rs);
        }else{
            //if(getARCount()>0) buffer.getShort();
            //System.out.println("add at" + buffer.position());
            addResource(rs);
        }



        int init = buffer.position();
        int arcount = getARCount()+1;
        setARCount(arcount);
        buffer.position(init);
        //System.out.println("after");
        showBuffer();

    }

    /**
     * Add an encoded type to the message at the current position.
     * @param recordType The type to be added
     */
    private void addQType(RecordType recordType) {
        // TODO: Complete this method
        // Assume that this method is only called from addQuestion and add RR
        buffer.putShort((short)recordType.getCode());
    }

    /**
     * Add an encoded class to the message at the current position.
     * @param recordClass The class to be added
     */
    private void addQClass(RecordClass recordClass) {
        // TODO: Complete this method
        // Assume that this method is only called from addQuestion

        buffer.putShort((short)recordClass.getCode());

    }

    /**
     * Return a byte array that contains all the data comprising this message.  The length of the
     * array will be exactly the same as the current position in the buffer.
     * @return A byte array containing this message's data
     */
    public byte[] getUsed() {
        // TODO: Complete this method

        byte[] ba = new byte[buffer.position()];
        System.arraycopy(buffer.array(),0,ba,0,ba.length);
//        System.out.println(Arrays.toString(buffer.array()));
//        System.out.println(Arrays.toString(ba));
        return ba;
    }

    /**
     * Returns a string representation of a DNS error code.
     *
     * @param error The error code received from the server.
     * @return A string representation of the error code.
     */
    public static String dnsErrorMessage(int error) {
        final String[] errors = new String[]{
                "No error", // 0
                "Format error", // 1
                "Server failure", // 2
                "Name error (name does not exist)", // 3
                "Not implemented (parameters not supported)", // 4
                "Refused" // 5
        };
        if (error >= 0 && error < errors.length)
            return errors[error];
        return "Invalid error message";
    }
}
