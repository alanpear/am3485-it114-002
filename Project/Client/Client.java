package Project.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import Project.Common.ConnectionPayload;
import Project.Common.Constants;
import Project.Common.FlipPayload;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.Phase;
import Project.Common.ReadyPayload;
import Project.Common.RollPayload;
import Project.Common.RoomResultsPayload;
import Project.Common.TextFX;
import Project.Common.TurnStatusPayload;
import Project.Common.TextFX.Color;


public enum Client {
    INSTANCE;

    Socket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    final String ipAddressPattern = "/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})";
    final String localhostPattern = "/connect\\s+(localhost:\\d{3,5})";
    boolean isRunning = false;
    private Thread inputThread;
    private Thread fromServerThread;
    private String clientName = "";

    private static final String CREATE_ROOM = "/createroom";
    private static final String JOIN_ROOM = "/joinroom";
    private static final String LIST_ROOMS = "/listrooms";
    private static final String LIST_USERS = "/users";
    private static final String DISCONNECT = "/disconnect";
    private static final String READY_CHECK = "/ready";
    private static final String SIMULATE_TURN = "/turn";

    private static final String ROLL = "/roll";
    private static final String FLIP = "/flip";

    // client id, is the key, client name is the value
    // private ConcurrentHashMap<Long, String> clientsInRoom = new
    // ConcurrentHashMap<Long, String>();
    private ConcurrentHashMap<Long, ClientPlayer> clientsInRoom = new ConcurrentHashMap<Long, ClientPlayer>();
    private long myClientId = Constants.DEFAULT_CLIENT_ID;
    private Logger logger = Logger.getLogger(Client.class.getName());
    private Phase currentPhase = Phase.READY;

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine
        // if the server had a problem
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    @Deprecated
    private boolean connect(String address, int port) {
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            listenForServerPayload();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @param username
     * @param callback (for triggering UI events)
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        clientName = username;
        Client.events = callback;
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            listenForServerPayload();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    /**
     * <p>
     * Check if the string contains the <i>connect</i> command
     * followed by an ip address and port or localhost and port.
     * </p>
     * <p>
     * Example format: 123.123.123:3000
     * </p>
     * <p>
     * Example format: localhost:3000
     * </p>
     * https://www.w3schools.com/java/java_regex.asp
     * 
     * @param text
     * @return
     */
    private boolean isConnection(String text) {
        // https://www.w3schools.com/java/java_regex.asp
        return text.matches(ipAddressPattern)
                || text.matches(localhostPattern);
    }

    private boolean isQuit(String text) {
        return text.equalsIgnoreCase("/quit");
    }

    // callback that updates the UI
    private static IClientEvents events;

    private boolean isName(String text) {
        if (text.startsWith("/name")) {
            String[] parts = text.split(" ");
            if (parts.length >= 2) {
                clientName = parts[1].trim();
                logger.info("Name set to " + clientName);
            }
            return true;
        }
        return false;
    }

    /**
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if a text was a command or triggered a command
     */
    private boolean processClientCommand(String text) {
        if (isConnection(text)) {
            if (clientName.isBlank()) {
                logger.warning("You must set your name before you can connect via: /name your_name");
                return true;
            }
            // replaces multiple spaces with single space
            // splits on the space after connect (gives us host and port)
            // splits on : to get host as index 0 and port as index 1
            String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
            connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            return true;
        } else if (isQuit(text)) {
            isRunning = false;
            return true;
        } else if (isName(text)) {
            return true;
        } else if (text.startsWith(CREATE_ROOM)) {

            try {
                String roomName = text.replace(CREATE_ROOM, "").trim();
                sendCreateRoom(roomName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.startsWith(JOIN_ROOM)) {

            try {
                String roomName = text.replace(JOIN_ROOM, "").trim();
                sendJoinRoom(roomName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.startsWith(LIST_ROOMS)) {

            try {
                String searchQuery = text.replace(LIST_ROOMS, "").trim();
                sendListRooms(searchQuery);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.equalsIgnoreCase(LIST_USERS)) {
            System.out.println(TextFX.colorize("Users in Room: ", Color.CYAN));
            clientsInRoom.forEach(((clientId, u) -> {
                System.out.println(TextFX.colorize((String.format("%s - %s [%s] %s %s",
                        clientId,
                        u.getClientName(),
                        u.isReady(),
                        u.didTakeTurn() ? "*" : "",
                        u.isMyTurn() ? "<--" : "")),

                        Color.CYAN));
            }));
            return true;
        } else if (text.equalsIgnoreCase(DISCONNECT)) {
            try {
                sendDisconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.equalsIgnoreCase(READY_CHECK)) {
            try {
                sendReadyCheck();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.equalsIgnoreCase(SIMULATE_TURN)) {
            try {
                sendTakeTurn();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.startsWith(ROLL)) {
            try { // am3485 4/1/2024
                String searchQuery = text.replace(ROLL, "").trim();
                sendRoll(searchQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.startsWith(FLIP)) {
            try { // am3485 4/1/2024
                sendFlip();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    // Send methods
    private void sendTakeTurn() throws IOException {
        // TurnStatusPayload (only if I need to actually send a boolean)
        Payload p = new Payload();
        p.setPayloadType(PayloadType.TURN);
        out.writeObject(p);
    }

    public void sendFlip() throws IOException {
        Random gen = new Random();
        FlipPayload p = new FlipPayload();
        int c = gen.nextInt(1000) % 2;
        if (c == 1) {// am3485 4/1/2024
            System.out.println("Heads");
            p.setResult("Heads");
        } else {
            System.out.println("Tails");
            p.setResult("Tails");
        }
        
        p.setPayloadType(PayloadType.FLIP);
        out.writeObject(p);

    }

    public void sendRoll(String text) throws IOException {
        Random gen = new Random();
        if (text.toLowerCase().contains("d")) {
            try {
                String[] diceNumbers = text.split("[dD]"); // 0 index is the amount and 1st index is max
                int counter = 0;
                for (int i = 0; i < Integer.parseInt(diceNumbers[0]); i++) {
                    counter += gen.nextInt(Integer.parseInt(diceNumbers[1])) + 1;
                }
                System.out.println(counter);
                RollPayload p = new RollPayload();
                p.setPayloadType(PayloadType.ROLL);
                p.setNumSides(Integer.parseInt(diceNumbers[1]));
                p.setNumDice(Integer.parseInt(diceNumbers[0]));
                p.setRollPrompt(text);//am3485 4/15/24
                p.setResults(counter);
                out.writeObject(p);
            } catch (Exception e) {
                System.out.println("invalid!!!!");
            }
            // am3485 4/1/2024
        } else if (!text.toLowerCase().contains("d") && text.matches("\\d+")) {// Checks if it doesn't contain a "d" AND
                                                                               // if there are only digits in the string
            // Parses the text to a number
            int maxRoll = Integer.parseInt(text);
            // Use gen object to make a random number
            int counter = gen.nextInt(maxRoll) + 1;
            System.out.println(counter);
            RollPayload p = new RollPayload();
            p.setPayloadType(PayloadType.ROLL);
            p.setNumSides(maxRoll);
            p.setResults(counter);
            p.setRollPrompt(text);//am3485 4/15/24
            out.writeObject(p);
        } else {
            // At this point it would be invalid so do something with it
            return;
        }
    }

    public void sendReadyCheck() throws IOException {
        ReadyPayload rp = new ReadyPayload();
        out.writeObject(rp);
    }

    public void sendDisconnect() throws IOException {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.DISCONNECT);
        out.writeObject(cp);
    }

    public void sendCreateRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    public void sendJoinRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    public void sendListRooms(String searchString) throws IOException {
        // Updated after video to use RoomResultsPayload so we can (later) use a limit
        // value
        RoomResultsPayload p = new RoomResultsPayload();
        p.setMessage(searchString);
        p.setLimit(10);
        out.writeObject(p);
    }

    private void sendConnect() throws IOException {
        ConnectionPayload p = new ConnectionPayload(true);

        p.setClientName(clientName);
        out.writeObject(p);
    }

    public void sendMessage(String message) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        // no need to send an identifier, because the server knows who we are
        // p.setClientName(clientName);
        out.writeObject(p);
    }

    // end send methods
    private void listenForKeyboard() {
        inputThread = new Thread() {
            @Override
            public void run() {
                logger.info("Listening for input");
                try (Scanner si = new Scanner(System.in);) {
                    String line = "";
                    isRunning = true;
                    while (isRunning) {
                        try {
                            logger.info("Waiting for input");
                            line = si.nextLine();
                            if (!processClientCommand(line)) {
                                if (isConnected()) {
                                    if (line != null && line.trim().length() > 0) {
                                        sendMessage(line);
                                    }

                                } else {
                                    logger.warning("Not connected to server");
                                }
                            }
                        } catch (Exception e) {
                            logger.severe("Connection dropped");
                            break;
                        }
                    }
                    logger.info("Exited loop");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }
        };
        inputThread.start();
    }

    private void listenForServerPayload() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;

                    // while we're connected, listen for strings from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        logger.info("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    logger.info("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        logger.severe("Server closed connection");
                    } else {
                        logger.severe("Connection closed");
                    }
                } finally {
                    close();
                    logger.info("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    private void listenForServerMessage() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;

                    // while we're connected, listen for strings from server
                    while (!server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        logger.info("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    logger.info("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        logger.severe("Server closed connection");
                    } else {
                        logger.severe("Connection closed");
                    }
                } finally {
                    close();
                    logger.info("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    private void addClientReference(long id, String name) {
        if (!clientsInRoom.containsKey(id)) {
            ClientPlayer cp = new ClientPlayer();
            cp.setClientId(id);
            cp.setClientName(name);
            clientsInRoom.put(id, cp);
        }
    }

    private void removeClientReference(long id) {
        if (clientsInRoom.containsKey(id)) {
            clientsInRoom.remove(id);
        }
    }

    public String getClientNameFromId(long id) {
        if (clientsInRoom.containsKey(id)) {
            return clientsInRoom.get(id).getClientName();
        }
        if (id == Constants.DEFAULT_CLIENT_ID) {
            return "[Room]";
        }
        return "[name not found]";
    }

    /**
     * Used to process payloads from the server-side and handle their data
     * 
     * @param p
     */
    private void processPayload(Payload p) {
        String message;
        switch (p.getPayloadType()) {
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) {
                    myClientId = p.getClientId();
                    addClientReference(myClientId, ((ConnectionPayload) p).getClientName());
                    logger.info(TextFX.colorize("My Client Id is " + myClientId, Color.GREEN));
                } else {
                    logger.info(TextFX.colorize("Setting client id to default", Color.RED));
                }
                events.onReceiveClientId(p.getClientId());
                break;
            case CONNECT:// for now connect,disconnect are all the same

            case DISCONNECT:
                ConnectionPayload cp = (ConnectionPayload) p;
                message = TextFX.colorize(String.format("*%s %s*",
                        cp.getClientName(),
                        cp.getMessage()), Color.YELLOW);
                logger.info(message);
            case SYNC_CLIENT:
                ConnectionPayload cp2 = (ConnectionPayload) p;
                if (cp2.getPayloadType() == PayloadType.CONNECT || cp2.getPayloadType() == PayloadType.SYNC_CLIENT) {
                    addClientReference(cp2.getClientId(), cp2.getClientName());

                } else if (cp2.getPayloadType() == PayloadType.DISCONNECT) {
                    removeClientReference(cp2.getClientId());
                }
                // TODO refactor this to avoid all these messy if condition (resulted from poor
                // planning ahead)
                if (cp2.getPayloadType() == PayloadType.CONNECT) {
                    events.onClientConnect(p.getClientId(), cp2.getClientName(), p.getMessage());
                } else if (cp2.getPayloadType() == PayloadType.DISCONNECT) {
                    events.onClientDisconnect(p.getClientId(), cp2.getClientName(), p.getMessage());
                } else if (cp2.getPayloadType() == PayloadType.SYNC_CLIENT) {
                    events.onSyncClient(p.getClientId(), cp2.getClientName());
                }

                break;
            case JOIN_ROOM:
                clientsInRoom.clear();// we changed a room so likely need to clear the list
                events.onResetUserList();
                break;
            case MESSAGE:

                message = TextFX.colorize(String.format("%s: %s",
                        getClientNameFromId(p.getClientId()),
                        p.getMessage()), Color.BLUE);
                System.out.println(message);
                events.onMessageReceive(p.getClientId(), p.getMessage());
                break;
            case LIST_ROOMS:
                try {
                    RoomResultsPayload rp = (RoomResultsPayload) p;
                    // if there's a message, print it
                    if (rp.getMessage() != null && !rp.getMessage().isBlank()) {
                        message = TextFX.colorize(rp.getMessage(), Color.RED);
                        logger.info(message);
                    }
                    // print room names found
                    List<String> rooms = rp.getRooms();
                    System.out.println(TextFX.colorize("Room Results", Color.CYAN));
                    for (int i = 0; i < rooms.size(); i++) {
                        String msg = String.format("%s %s", (i + 1), rooms.get(i));
                        System.out.println(TextFX.colorize(msg, Color.CYAN));
                    }
                    events.onReceiveRoomList(rp.getRooms(), rp.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case READY:
                try {
                    ReadyPayload rp = (ReadyPayload) p;
                    if (clientsInRoom.containsKey(rp.getClientId())) {
                        clientsInRoom.get(rp.getClientId()).setReady(rp.isReady());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PHASE:
                try {
                    currentPhase = Enum.valueOf(Phase.class, p.getMessage());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case TURN:
                try {
                    TurnStatusPayload tsp = (TurnStatusPayload) p;
                    if (clientsInRoom.containsKey(tsp.getClientId())) {
                        clientsInRoom.get(tsp.getClientId()).setTakenTurn(tsp.didTakeTurn());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case RESET_TURNS:
                clientsInRoom.values().stream().forEach(c -> {
                    c.setTakenTurn(false);
                    c.setMyTurn(false);
                });
                break;
            case RESET_READY:
                clientsInRoom.values().stream().forEach(c -> c.setReady(false));
                break;
            case CURRENT_TURN:
                /*
                 * if (clientsInRoom.containsKey(p.getClientId())) {
                 * clientsInRoom.get(p.getClientId()).setMyTurn(true);
                 * }
                 */
                clientsInRoom.values().stream().forEach(c -> {
                    boolean isMyTurn = c.getClientId() == p.getClientId();
                    c.setMyTurn(isMyTurn);
                    if (isMyTurn) {
                        System.out.println(
                                TextFX.colorize(String.format("It's %s's turn", c.getClientName()), Color.PURPLE));
                    }
                });
                break;
            case FLIP:
                try {
                    FlipPayload fp = (FlipPayload) p;
                    fp.toString();

                    
                    break;
                } catch (Exception e) {

                }
                break;
                case ROLL:
                try {
                    RollPayload rp = (RollPayload) p;
                    rp.toString();

                    
                    break;
                } catch (Exception e) {

                }
                break;
            // case END_SESSION: //clearing all local player data
            default:
                break;

        }
    }

    public void start() throws IOException {
        listenForKeyboard();
    }

    private void close() {
        myClientId = Constants.DEFAULT_CLIENT_ID;
        clientsInRoom.clear();
        try {
            inputThread.interrupt();
        } catch (Exception e) {
            logger.severe("Error interrupting input");
            e.printStackTrace();
        }
        try {
            fromServerThread.interrupt();
        } catch (Exception e) {
            logger.severe("Error interrupting listener");
            e.printStackTrace();
        }
        try {
            logger.info("Closing output stream");
            out.close();
        } catch (NullPointerException ne) {
            logger.severe("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logger.info("Closing input stream");
            in.close();
        } catch (NullPointerException ne) {
            logger.severe("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logger.info("Closing connection");
            server.close();
            logger.severe("Closed socket");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            logger.warning("Server was never opened so this exception is ok");
        }
    }

    public static void main(String[] args) {
        try {
            File myObj = new File("ChatHistory.html");
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        Client client = Client.INSTANCE; // new Client();

        try {
            // if start is private, it's valid here since this main is part of the class
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}