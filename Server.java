
// Java program to illustrate Server side
// Implementation using DatagramSocket and DatagramPacket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    static DatagramPacket DpReceive;
    static DatagramPacket DpSend;

    static DatagramSocket socket;

    static byte[] receive;
    static byte[] send;

    static boolean handshaked;
    static boolean handSYN;
    static boolean handSYNACK;
    static boolean handACK;

    static InetAddress IP;

    static Timer timer_synack;
    static TimerTask task_synack;
    static Integer timeout_synack;

    static byte[] today_msg;

    static String[] chunks;

    static Integer index;
    static String seq;

    static boolean SEND;
    static Integer SENDWAIT;

    static boolean FIN;

    public static void main(String[] args) throws IOException, SocketException {
        //Create a socket to listen at port 1234
        socket = new DatagramSocket(1234);

        // variables to wait for sending and receiving from server & client(i.e retransmissions)
        SENDWAIT = 0;
        SEND = false;

        index = 0;

        FIN = false;

        timer_synack = new Timer();
        timeout_synack = 20 * 100000;

        // Intializing the byte arrays
        receive = new byte[65535];
        send = new byte[65535];

        // Flags to validate handshake process
        handshaked = false;
        handACK = false;
        handSYN = false;
        handSYNACK = false;

        seq = "0";
        try{
        IP = InetAddress.getLocalHost();
        // Properties for chunk
        chunks = generate_chunks();

        try{
            while (true) {
                if (!handshaked) { // Hand Shake process
                    // Receive incoming data
                    rec();
                    handshake_check();
                     //receive request message and after begin sending data chunks.
                    rec();
                    System.out.println("Server:" + data(receive)+ " received.");
                } else {
                   
                    // Get the data in chunks
                    if (index < chunks.length) {
                        send_chunks();
                    } else {
                        System.out.println("All Chunks sent!, FIN: " + FIN);
                        fin_packet();
                    }
                }

                if (FIN) {
                    System.out.println("Rec FINACK, closing");
                    break; 
                }

                // Reset the recieve
                receive = new byte[65535];
            
            }
        }catch(SocketException e) {System.out.println("Socket Error");}
    }catch(IOException e) {System.out.println("Error with I/O");}

    System.out.println("Closing");
    socket.close();
    
    }

    //Method when all data chunks are sent to make FIN packet message
    public static void fin_packet() throws IOException {
        // Send FIN
        send("FIN");

        // Receive
        rec();

        System.out.println("fin rec: " + data(receive));

        if (data(receive).toString().equals("ACK")) {
            FIN = true;
        }
    }

    //Method to send data chunks
    public static void send_chunks() throws IOException {
        // Send the data chunk to client
        if (!SEND) {
            System.out.println("Packet " + index + " Sent!");
            send(chunks[index]);
            SEND = false;
        } 
        // Used for retransmissions- if data sent during wait time, confirm it has been sent
        else 
        {
            if (SENDWAIT == 10) {
                SEND = true;
            } 
        }

        // Receive Approval
        rec();

        // Received Seq Number
        String rec_seq = get_seq();

        System.out.println("Rec seq: " + rec_seq + ", expecting: " + seq);
        
        // Checking the seq number
        if (rec_seq.equals(seq)) {
            index += 1;
            SENDWAIT = 0;
            SEND = false;
            flip_seq();
            System.out.println("Incremented Index: " + index + ", seq num: " + seq);
        } else {
            SEND = true;
            SENDWAIT += 1;
        }
    }
    // Method to get sequence number
    public static String get_seq() {
        String msg = data(receive).toString();
        return Character.toString(msg.charAt(0));
    }

    //Method to alternate between seq numbers 0 and 1
    public static void flip_seq() {
        if (seq == "0") {
            seq = "1";
        } else {
            seq = "0";
        }
    }

    //Method to receive message.
    public static void rec() throws IOException {
        DpReceive = new DatagramPacket(receive, receive.length);
        socket.receive(DpReceive);
    }

    //Method to create sending packet
    public static void send(String msg) throws IOException {
        send = msg.getBytes();
        DpSend = new DatagramPacket(send, send.length, IP, DpReceive.getPort());
        socket.send(DpSend);
        send = new byte[65535];
    }

    public static String[] generate_chunks() {
        //Message to be sent. Since it changes daily, it prints the date, followed
        //by a promotion message.
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMM dd, yyyy");
        String msg = "Today's date is " + dateFormat.format(new Date()) + 
        ". Amazon has some major deals right before boxing day!!! Do not miss out!!!";
        
        //Print message to be sent to client.
        System.out.println("Actual msg: " + msg);

        //Split message into chunks of length 16 with seq number.
        //When sent to the client, it will sent data packets of bytes with seq number.
        String newStr = msg.replaceAll("(.{16})", "$1|");
        String[] newStrings = newStr.split("\\|");

        // Since we alternate sequence numbers when sending and recieving, the sequence numbers
        // are alternated and contatenated when chunks are generated.
        for (int i = 0; i < newStrings.length; i++) {
            if (i % 2 == 0) {
                newStrings[i] = "0" + newStrings[i];
            } else {
                newStrings[i] = "1" + newStrings[i];
            }
        }
        //return array containing chunks to be sent.
        return newStrings;
    }
    // Interact with server and verify if a handshake has been made.
    public static void handshake_check() throws IOException {
        // Getting the message
        String incmoing_msg = data(receive).toString();

        // Client sends the initial message
        if (!handSYN && incmoing_msg.equals("SYN")) {
            System.out.println("Server: SYN received.");
            handSYN = true;
        }

        // If the SYN is already received and SYNACK not sent
        if (!handSYNACK && handSYN) {
            System.out.println("Server: SYNACK sent.");

            send("SYNACK");

            handSYNACK = true;

        }

        // Client Acknowledges the message from server
        if (!handACK && incmoing_msg.equals("ACK")) {
            System.out.println("Server: ACK received.");
            handACK = true;
        }

        // Checks to see if the handshake is done
        if (handACK && handSYN && handSYNACK) {
            System.out.println("Threeway handshake is done!");
            handshaked = true;
        }

    }

    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder b = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            b.append((char) a[i]);
            i++;
        }
        return b;
    }
}