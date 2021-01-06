package Main;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * This class contains the custom logic for the
 * websocket server.
 */
public class Server extends WebSocketServer {

    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given <var>port</var>.
     * @param port The port this server should listen on.
     * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
     */
    public Server(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given <var>address</var>.
     * @param address The address to listen to
     */
    public Server(InetSocketAddress address) {
        super(address);
    }

    /**
     * Creates a WebSocketServer that will attempt to bind/listen on the given <var>port</var>.
     * @param port The port this server should listen on.
     * @param draft  The versions of the WebSocket protocol that this server
     * 	 *            instance should comply to. Clients that use an other protocol version will be rejected.
     */
    public Server(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    /**
     * Starts the websocket server bound to a given port
     * @param args array of command line arguments
     * @throws InterruptedException Interrupt
     * @throws IOException Produced by failed or interrupted I/O operations
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 8080; // 843 flash policy port
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }

        Server s = new Server(port);
        s.start();

        System.out.println("Server started on port: " + s.getPort());

    }

    /**
     * Set a timeout in milliseconds used when requesting a connection from
     * the connection manager. A timeout value of zero is interpreted as an
     * infinite timeout.
     */
    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    /**
     * Called after an opening handshake has been performed and the given websocket is ready to be written on.
     * @param conn The <tt>WebSocket</tt> instance this event is occurring on.
     * @param handshake The handshake of the websocket instance
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        // send newly connected client initial timestamp
        notify(conn, 0);
    }

    /**
     * Callback for string messages received from the remote client
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @param message The UTF-8 decoded message that was received.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            // decode incoming message into an object
            JSONObject data = (JSONObject) new JSONParser().parse(message);

            // notify client with event for message with count "c"
            notify(conn, ((Long)data.get("c")).intValue());
        }catch (ParseException e){
            System.out.println(e.getMessage());
        }

    }

    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link #onClose(WebSocket, int, String, boolean)} will be called additionally.<br>
     * This method will be called primarily because of IO or protocol errors.<br>
     *  If the given exception is an RuntimeException that probably means that you encountered a bug.<br>
     * @param conn Can be null if there error does not belong to one specific websocket. For example if the servers port could not be bound.
     * @param ex The exception causing this error
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Called after the websocket connection has been closed.
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @param code The codes can be looked up here: {@link CloseFrame}
     * @param reason  Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    /**
     * Gets the current unix timestamp of the server
     * @return <tt>long</tt> the current unix timestamp
     */
    private long getTimestamp(){
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * reates a JSON string containing the message count and the current timestamp
     * @param c <tt>integer</tt> The message count
     * @return <tt>string</tt> A JSON string containing the message count and the current timestamp
     */
    private String getEvent(int c){

        //create an event array for the time that message "c" is received by the server
        JSONObject event = new JSONObject();
        event.put("c", c);
        event.put("ts", getTimestamp());

        return event.toString();
    }

    /**
     * Send a connected client an event JSON string
     * @param ws <tt>Websocket</tt> The client connection the outgoing message is for
     * @param c <tt>integer</tt> The message count
     */
    private void notify(WebSocket ws, int c){

        //send the given connection the event timestamp for message "c"
        ws.send(getEvent(c));
    }

}