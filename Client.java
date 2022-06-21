
// Java program to illustrate Client side
// Implementation using DatagramSocket and DatagramPacket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.net.SocketException;
import java.util.TimerTask;
import java.util.Date;

public class Client {

    static DatagramPacket DpReceive;
    static DatagramPacket DpSend;

    static InetAddress IP;
    static DatagramSocket socket;

    static boolean handshaked;
    static boolean handSYN;
    static boolean handSYNACK;
    static boolean handACK;

    static byte[] receive;
    static byte[] send;

    static TimerTask task_synack;
    static Timer timer_synack;
    static Integer timeout_synack;

    static String seq;

    public static void main(String args[]) throws IOException, SocketException {
        try
        {
            socket = new DatagramSocket();

            seq = "0";

            timer_synack = new Timer();
            timeout_synack = 20 * 1000;

            IP = InetAddress.getLocalHost();

            // Intializing the byte arrays
            receive = new byte[65535];
            send = new byte[65535];

            // Flags to validate handshake process
            handshaked = false;
            handACK = false;
            handSYN = false;
            handSYNACK = false;

            // Send SYN
            send("SYN");

            try{
                while (true) {
                    // Receive incoming data
                    rec();

                    if (!handshaked) { // Hand Shake process
                        handshake_check();
                        //Right after handshake, sent request msg.
                        send("REQ");
                    } else if (data(receive).toString().equals("FIN")) {
                        System.out.println("FIN Rec");
                        send("ACK");
                        break;
                    } else {
                        // Get the data in chunks
                        rec_chunks();
                    }

                    // Reset the receive
                    receive = new byte[65535];
                }
            } catch(SocketException e) {System.out.println("Socket Error");}
            System.out.println("Closing");
            socket.close();
        }catch(IOException e){System.out.println("Error with I/O");}
    }

    //Method to receive data chunks from server
    static void rec_chunks() throws IOException {
        // Receive Packet
        String rec_seq = get_seq();
        //Print data was received, showing seq number followed by message
        System.out.println("Seq: "+ rec_seq+ ", Rec:" +
         data(receive).toString().substring(1,data(receive).toString().length()));

        if (rec_seq.equals(seq)) {
            System.out.println("Seq " + seq + " sent back.");
            send(seq);
            flip_seq();
        } else {

        }

    }

    //Method to get seq number that was received from server
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

    // Method for 3 way handshake using flags
    static void handshake_check() throws IOException {
        // Getting the message
        String incmoing_msg = data(receive).toString();

        if (!handSYN) {
            handSYN = true;
            send("ACK");

            task_synack = new TimerTask() {
                @Override
                public void run()  {
                    if (!handSYNACK)
                        handSYN = true;
                }
            };

            // Set the timer for when the SYNACK is sent but ACK is not received
            timer_synack.scheduleAtFixedRate(task_synack, new Date(), timeout_synack);
        }

        // SYN sent, received SYNACK and not sent ACK yet
         if (handSYN && incmoing_msg.equals("SYNACK")) {
            System.out.println("Client: SYNACK received.");
            handSYNACK = handACK = true;

            // Sending the SYN
            send("ACK");
        }

        if (handACK && handSYN && handSYNACK) {
            System.out.println("Threeway handshake is done!");
            handshaked = true;
            timer_synack.cancel();
        }

    }

    //Method to create and sent data packet
    public static void send(String msg) throws IOException {
        System.out.println("Client: " + msg + " sent");
        send = msg.getBytes();
        DpSend = new DatagramPacket(send, send.length, IP, 1234);
        socket.send(DpSend);
        send = new byte[65535];
    }
    
     //Method to receive data packet from server.
    public static void rec() throws IOException {
        DpReceive = new DatagramPacket(receive, receive.length);
        socket.receive(DpReceive);
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