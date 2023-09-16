# App-Layer-Protocol-Demo
This project simulates the app-layer over udp.

<b>Code Design-Methods/Functions<b> </ br>

<u>Client.java<u> </ br>
● public static void main(String args[]) throws IOException, SocketException::Main method. It is used to call all other methods/functions and to close the socket.
● public static String get_seq(): Method to get seq number that was received from server 
● public static void flip_seq(): Method to alternate between seq numbers 0 and 1  
● public static void send(String msg) throws IOException: Method to create and sent data packet 
● public static void rec() throws IOException: Method to receive a data packet from the server.  
● public static StringBuilder data(byte[] a): Method to convert received bytes from the server into a string. </ br>
<u>Server.java<u> </ br>
● public static void main(String[] args) throws IOException, SocketException:Main method. It is used to call all other methods/functions and to close the socket.
● public static void fin_packet() throws IOException: Method when all data chunks are sent to make FIN packet message.
● public static void send_chunks() throws IOException: Method used to send data chunks to the Client
● public static String get_seq(): Method to get sequence number
● public static void flip_seq(): Method to alternate between seq numbers 0 and 1
● public static void rec() throws IOException: Method to receive message sent from the client.
● public static void send(String msg) throws IOException: Method to create message, which will be sent in chucks of packets to the client.
● public static String[] generate_chunks() 
● public static void handshake_check() throws IOException: Interact with server and verify if a handshake has been made.
● public static StringBuilder data(byte[] a): Method to convert received bytesfrom client into a string.



<b>Algorithm-Integration of Client and Server<b>
1) Threeway Handshake: The client and server establish a threeway handshake using the method handshake_check(), if they had not done so already. It sends boolean variables which were made for each type of component of the threeway handshake (SYN, ACK, SYNACK). During the client-server interaction, if each represented message had been sent/received between the client/server, these variables had been set to true. Once they all had been set to true, the threeway handshake was made. Otherwise, an exception was thrown.

2) Request From the client: Once the three-way handshake had been established, the client will send the “REQ” message to the server. The server will then verify if it had been received. If so, the process of sending the bytes will then begin. Otherwise, the appropriate exception was thrown.

3) Sending the Bytes: After the server received the sent “REQ” data packet from the client, it will begin the process of sending the message of the day in chunks of 16 bytes and was done using the method generate_chunks(). Since the message of the day must be unique every 24 hours, the SimpleDateFormat class was used to get the date(excluding time), which was then concatenated with another string to satisfy the requirement that the message is longer than 16 bytes to create the string. Afterward, the string was then separated into 16 bytes, are stored in an array. A loop was then used to send the packets to the client in chunks of 16 bytes each. To ensure a proper sending of data from the server to the client, sequence numbers were used. The sequence numbers were alternated between “0” and “1” which were concatenated with the message. The client and server would receive/send and expect the alternating received/sent sequence numbers. Otherwise, in the case the client/server received a non-expecting sequence number, the client/server would stop sending packets and the program would stop. 

Handling missing packets: In the case of retransmissions, it is implemented by counting the number of loops where the needed message is not received Using boolean variable SEND and integer variable SENDWAIT. Whenever a packet is sent from the client/server, a loop runs and the value of the SENDWAIT is incremented if the server/client had not received it. The packet is then resent to the client/server until the SENDWAIT had reached an upper bound..
Error handling

For this program, try and catch blocks were used to catch common errors which occur when the server and client communicate with each other.

The following exceptions used are:
IOException: Catches failed or interrupted I/O operations
SocketException: Thrown when there are failures in the client and server sockets.
