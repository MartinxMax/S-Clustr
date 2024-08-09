package com.s_clustr.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.fusesource.jansi.AnsiConsole;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;



public class ServerMain {
    private static JsonObject configJson;
    private static String s_key = generatePassword(12); // Default value, can be generated randomly
    private static String r_key = generatePassword(12); // Default value, can be generated randomly
    private static String data_push_ip = ""; // Default value
    private static int data_push_port = 10089; // Default value
    private static int device_port = 10000; // Default value
    private static int anonymous_port = 10090; // Default value
    // Concurrent collections for thread-safe operations
    private static final Map<String, JsonObject> nodeDataMap = new ConcurrentHashMap<>();
    private static final Map<String, ClientInfo> DeviceSockets = new HashMap<>();
    private static final Map<String, Boolean> ClientIdAvailability = new HashMap<>();

    // Scheduled thread pool for cleanup tasks
    private static final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

    // Gson instance for JSON serialization/deserialization
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        LoggerUtil.printColoredAsciiArt();
        parseCommandLineArguments(args);
        try {
            loadConfig();
            initializeClientIdAvailability();
            // Reload config file every 6 seconds
            ScheduledExecutorService configReloadScheduler = Executors.newSingleThreadScheduledExecutor();
            configReloadScheduler.scheduleAtFixedRate(() -> {
                try {
                    loadConfig();
                } catch (Exception e) {
                    LoggerUtil.printError("Failed to reload config file: " + e.getMessage());
                }
            }, 0, 6, TimeUnit.SECONDS);

            LoggerUtil.printInfo("Node Key: [" + s_key + "]");
            LoggerUtil.printInfo("Ring Network Key: [" + r_key + "]");
            LoggerUtil.printInfo("Max Devices: [" + configJson.getAsJsonObject("SERVER").get("MAX_DEV").getAsLong() + "]");
            // Start TCP server for device service
            new Thread(ServerMain::startTcpServer10000).start();
            LoggerUtil.printDevice("Device service listening on port [0.0.0.0:" + device_port + "] (TCP)");

            // Start UDP server for ring network data push
            new Thread(ServerMain::startUdpServer).start();
            LoggerUtil.printInfo("Ring network service listening on port [0.0.0.0:" + data_push_port + "] (UDP)");

            // Start TCP server for anonymous service
            new Thread(ServerMain::startTcpServer10090).start();
            LoggerUtil.printAnonymous("Anonymous service listening on port [0.0.0.0:" + anonymous_port + "] (TCP)");

            // Send heartbeat every 30 seconds
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(ServerMain::sendHeartbeat, 0, 30, TimeUnit.SECONDS);
            LoggerUtil.printInfo("Core [Heartbeat] loaded successfully");

            // Clean up stale entries every 10 seconds
            cleanupScheduler.scheduleAtFixedRate(ServerMain::cleanupStaleEntries, 10, 10, TimeUnit.SECONDS);
            LoggerUtil.printInfo("Core [S-Clustr] loaded successfully");

            // Upload device information via Ring network if configured
            if (!data_push_ip.equals("") && data_push_port != -1) {
                LoggerUtil.printInfo("Device information will be uploaded via Ring network to [" + data_push_ip + ":" + data_push_port + "]");
                startUdpClient();
            }
        } catch (Exception e) {
            LoggerUtil.printError("An error occurred: " + e.getMessage());
        }
    }


    public static String generatePassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }


    private static void parseCommandLineArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-nkey":
                    if (i + 1 < args.length) {
                        s_key = args[i + 1];
                        if (s_key.length() < 8) {
                            LoggerUtil.printError("Length of -nkey must be at least 8 characters.");
                            System.exit(1);
                        }
                        i++; // Move to the next argument
                    } else {
                        LoggerUtil.printError("Missing value for -nkey");
                        System.exit(1);
                    }
                    break;
                case "-rkey":
                    if (i + 1 < args.length) {
                        r_key = args[i + 1];
                        if (r_key.length() < 8) {
                            LoggerUtil.printError("Length of -rkey must be at least 8 characters.");
                            System.exit(1);
                        }
                        i++; // Move to the next argument
                    } else {
                        LoggerUtil.printError("Missing value for -rkey");
                        System.exit(1);
                    }
                    break;
                case "-rootip":
                    if (i + 1 < args.length) {
                        data_push_ip = args[i + 1];
                        i++; // Move to the next argument
                    } else {
                        LoggerUtil.printError("Missing value for -rootip");
                        System.exit(1);
                    }
                    break;
                case "-rootport":
                    if (i + 1 < args.length) {
                        try {
                            data_push_port = Integer.parseInt(args[i + 1]);
                            i++; // Move to the next argument
                        } catch (NumberFormatException e) {
                            LoggerUtil.printError("Invalid value for -rootport: " + args[i + 1]);
                            System.exit(1);
                        }
                    } else {
                        LoggerUtil.printError("Missing value for -rootport");
                        System.exit(1);
                    }
                    break;
                case "-deviceport":
                    if (i + 1 < args.length) {
                        try {
                            device_port = Integer.parseInt(args[i + 1]);
                            i++; // Move to the next argument
                        } catch (NumberFormatException e) {
                            LoggerUtil.printError("Invalid value for -deviceport: " + args[i + 1]);
                            System.exit(1);
                        }
                    } else {
                        LoggerUtil.printError("Missing value for -deviceport");
                        System.exit(1);
                    }
                    break;
                case "-anonymousport":
                    if (i + 1 < args.length) {
                        try {
                            anonymous_port = Integer.parseInt(args[i + 1]);
                            i++; // Move to the next argument
                        } catch (NumberFormatException e) {
                            LoggerUtil.printError("Invalid value for -anonymousport: " + args[i + 1]);
                            System.exit(1);
                        }
                    } else {
                        LoggerUtil.printError("Missing value for -anonymousport");
                        System.exit(1);
                    }
                    break;
                case "-h":
                    displayHelp();
                    System.exit(0);
                    break;
                default:
                    LoggerUtil.printError("Unknown argument: " + args[i]);
                    System.exit(1);
            }
        }
    }



    private static void displayHelp() {
        System.out.println("Usage: java ServerMain [-skey <value>] [-rkey <value>] [-rootip <value>] [-rootport <value>] [-deviceport <value>] [-anonymousport <value>] [-h]");
        System.out.println("Options:");
        System.out.println("  -nkey <value>         Set local node key (default: Random:8)");
        System.out.println("  -rkey <value>         Set local ring network key (default: Random:8)");
        System.out.println("  -rootip <value>       Set Upload data to target ip");
        System.out.println("  -rootport <value>     Set Upload data to target port (default: 10089)");
        System.out.println("  -deviceport <value>   Set local port listener on device service (default: 10000)");
        System.out.println("  -anonymousport <value>Set local port listener on anonymous service (default: 10090)");
        System.out.println("  -h                    Display this help message");
    }



    private static void loadConfig() throws Exception {
        Reader reader = new FileReader("./config/server.json");
        JsonObject newConfigJson = JsonParser.parseReader(reader).getAsJsonObject();
        // If parsing succeeds, update the configuration
        configJson = newConfigJson;
    }

    private static void initializeClientIdAvailability() {
        for (int i = 1; i <= configJson.getAsJsonObject("SERVER").get("MAX_DEV").getAsLong(); i++) {
            String clientId = String.valueOf(i);
            ClientIdAvailability.put(clientId, true);
        }
    }

    private static void startTcpServer10000() {
        try (ServerSocket serverSocket = new ServerSocket(device_port)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(2000);
                    LoggerUtil.printDevice("New device is attempting authentication");

                    try {
                        // Read data from client
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                        String encryptedText = null;

                        try {
                            encryptedText = in.readLine();
                        } catch (IOException e) {
                            LoggerUtil.printDevice("Error reading from client input stream: " + e.getMessage());
                            closeSocket(clientSocket);
                            continue;
                        }

                        if (encryptedText == null) {
                            LoggerUtil.printDevice("Received null data from client. Dropping packet.");
                            closeSocket(clientSocket);
                            continue;
                        }

                        // Try to parse received data as JSON
                        JsonObject jsonObject = null;
                        try {
                            jsonObject = JsonParser.parseString(encryptedText).getAsJsonObject();
                        } catch (JsonSyntaxException e) {
                            LoggerUtil.printDevice("Received data is not valid JSON. Dropping packet.");
                            closeSocket(clientSocket);
                            continue;
                        } catch (IllegalStateException e) {
                            // Handle case where the data is not a JSON object
                            LoggerUtil.printDevice("Received data is not a JSON object. Dropping packet.");
                            closeSocket(clientSocket);
                            continue;
                        }

                        // Check if JSON object contains TYPE field
                        if (!jsonObject.has("TYPE")) {
                            LoggerUtil.printDevice("Received JSON does not contain 'TYPE' field. Dropping packet.");
                            closeSocket(clientSocket);
                            continue;
                        }

                        // Retrieve the TYPE field value
                        String type = jsonObject.get("TYPE").getAsString();
                        String foundKey = findKeyByType(configJson, type);

                        if (foundKey != null) {
                            String clientId = allocateClientId();
                            if (clientId != null) {
                                ClientInfo clientInfo = new ClientInfo(clientSocket, type, clientId);
                                DeviceSockets.put(clientId, clientInfo);
                                LoggerUtil.printDevice("Device authentication successful [" + clientId + ":" + type + "]");
                            } else {
                                LoggerUtil.printDevice("No available client IDs. Connection rejected.");
                                closeSocket(clientSocket);
                            }
                        } else {
                            LoggerUtil.printDevice("Unsupported device type received: " + type);
                            closeSocket(clientSocket);
                        }
                    } catch (IOException e) {
                        LoggerUtil.printDevice("Error handling client connection: " + e.getMessage());
                        closeSocket(clientSocket);
                    }
                } catch (IOException e) {
                    LoggerUtil.printDevice("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LoggerUtil.printDevice("An error occurred in TCP Server 10000: " + e.getMessage());
        }
    }


    private static void closeSocket(Socket socket) {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            LoggerUtil.printError("Error closing socket: " + ex.getMessage());
        }
    }
    private static String findKeyByType(JsonObject configJson, String searchString) {
        if (configJson == null || !configJson.has("Device")) {
            return null;
        }

        JsonObject device = configJson.getAsJsonObject("Device");
        for (String key : device.keySet()) {
            JsonObject deviceInfo = device.getAsJsonObject(key);
            if (deviceInfo.has("TYPE")) {
                String type = deviceInfo.get("TYPE").getAsString();
                if (type.equals(searchString)) {
                    return key; // Return the found key
                }
            }
        }

        return null; // Return null if the corresponding type is not found
    }

    private static String allocateClientId() {
        for (int i = 1; i <= configJson.getAsJsonObject("SERVER").get("MAX_DEV").getAsLong(); i++) {
            String clientId = String.valueOf(i);
            if (!ClientIdAvailability.containsKey(clientId) || ClientIdAvailability.get(clientId)) {
                ClientIdAvailability.put(clientId, false);
                return clientId;
            }
        }
        return null;
    }

    private static void sendHeartbeat() {
        Iterator<Map.Entry<String, ClientInfo>> iterator = DeviceSockets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ClientInfo> entry = iterator.next();
            ClientInfo clientInfo = entry.getValue();
            try {
                clientInfo.getSocket().getOutputStream().write('H'); // Sending 'H' as heartbeat
                clientInfo.getSocket().getOutputStream().flush();
            } catch (IOException e) {
                LoggerUtil.printError("Failed to send heartbeat to client " + entry.getKey() + ": " + e.getMessage());
                ClientIdAvailability.put(entry.getKey(), true);
                iterator.remove();
            }
        }
    }

//    private static void printDeviceStatus() {
//        StringBuilder sb = new StringBuilder("\n===== Current Device Status =====\n");
//        for (Map.Entry<String, ClientInfo> entry : DeviceSockets.entrySet()) {
//            String clientId = entry.getKey();
//            ClientInfo clientInfo = entry.getValue();
//            sb.append("Device ID: ").append(clientId).append(", Type: ").append(clientInfo.getType()).append("\n");
//        }
//        sb.append("=================================\n");
//        LoggerUtil.printInfo(sb.toString());
//    }

    private static void startTcpServer10090() {
        try (ServerSocket serverSocket = new ServerSocket(anonymous_port)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(2000); // Set read timeout to 2 seconds

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String source;

                    try {
                        source = in.readLine();
                    } catch (SocketTimeoutException e) {
                        LoggerUtil.printAnonymous("Client connection timed out due to inactivity. Closing connection.");
                        clientSocket.close();
                        continue;
                    }

                    // Drop malformed or empty data packets
                    if (source == null || source.isEmpty() || source.length() < 10) {
                        LoggerUtil.printAnonymous("Received malformed or empty data packet. Dropping packet.");
                        clientSocket.close();
                        continue;
                    }

                    // Decode the received data
                    String[] tag = CORE.decode_payloadBlock2(r_key, source);

                    if (tag.length < 4) {
                        LoggerUtil.printAnonymous("Malformed tag format. Dropping packet.");
                        clientSocket.close();
                        continue;
                    }

                    long timestamp = CORE.hexToTimestamp(tag[1]);
                    if (!validateTimestamp(timestamp)) {
                        LoggerUtil.printAnonymous("Ring Timestamp validation failed. Timeout.");
                        continue;
                    }
                    if (tag[2].equals("CCCCCCCCCCCC")) {
                        sendAllNodeStatusJson(clientSocket);
                    } else if (tag[2].equals("CCRCCCOCCOCT")) {
                        sendRootNodeId(clientSocket);
                    } else {
                        String mac = CORE.getFirstRealMacAddress();
                        if (tag[2].equals(mac) && !tag[3].equals("30")) {// ins't forwarded module
                            //30 Forward Module
                            // query module
                            if (tag[3].equals("33")) {//Root node sinfo
                                // Respond with device data if tag is equal to MAC and type is 30
                                JsonObject jsonData = new JsonObject();
                                String macAddress = CORE.getFirstRealMacAddress();
                                jsonData.add(macAddress, createDeviceJson());
                                jsonData.addProperty("timestamp", System.currentTimeMillis());
                                String jsonStr = jsonData.toString();

                                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                out.println(CORE.anonymousEncode(r_key, jsonStr));
                            }
                            else if (tag[3].equals("32")){//Child node info
                                    JsonObject nodeData = nodeDataMap.get(tag[2]);
                                    String responseData = gson.toJson(nodeData);
                                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                    out.println(CORE.anonymousEncode(r_key, responseData));
                            }else{// control module
                            String deData;

                            deData = CORE.decode_payloadBlock1(s_key, tag[0]);
                            if (deData.equals("False")) {
                                LoggerUtil.printAnonymous("The provided key is incorrect.");
                                clientSocket.close();
                                continue;
                            }
                            JsonObject jsonObject = gson.fromJson(deData, JsonObject.class);
                            if (validateTimestamp(jsonObject.get("timestamp").getAsLong())) {
                                LoggerUtil.printAnonymous("Timestamp validation successful.");
                                String id = jsonObject.get("id").getAsString();
                                String stat = jsonObject.get("stat").getAsString();
                                String type = jsonObject.get("type").getAsString();
                                if (!controlDevice(id, stat, type, clientSocket)) {
                                    LoggerUtil.printAnonymous("Failed to send command to device.");
                                }
                            } else {
                                LoggerUtil.printAnonymous("Timestamp validation failed. Timeout.");
                            }
                            }
                        } else {
                            if (tag[3].equals("32")){//Child node info
                                JsonObject nodeData = nodeDataMap.get(tag[2]);
                                String responseData = gson.toJson(nodeData);
                                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                out.println(CORE.anonymousEncode(r_key, responseData));
                            }else
                            // Check if tag[1] exists in nodeDataMap keys
                            if (nodeDataMap.containsKey(tag[2])&& tag[3].equals("30")) {
                                // Send node data to client if control type is 30
                                    forwardDataToMac(source, tag[2]);
                            }
                        }
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    LoggerUtil.printAnonymous("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LoggerUtil.printAnonymous("An error occurred in TCP Server 10090: " + e.getMessage());
        }

    }




    public static void sendRootNodeId(Socket clientSocket) {
        try {
            // Create JSON object with "ROOT" key and CORE.getFirstRealMacAddress() value
            JsonObject rootNodeJson = new JsonObject();
            rootNodeJson.addProperty("ROOT", CORE.getFirstRealMacAddress());

            // Convert JSON to string
            String response = rootNodeJson.toString();

            // Encode response and send to client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(CORE.anonymousEncode(r_key, response));
            out.flush();
        } catch (IOException e) {
            LoggerUtil.printAnonymous("Error sending ROOT node ID JSON: " + e.getMessage());
        }
    }

    private static void sendAllNodeStatusJson(Socket clientSocket) {
        try {
            // Prepare JSON data representing node status
            JsonObject allNodeStatus = new JsonObject();

            // Populate allNodeStatus with data from nodeDataMap
            for (String mac : nodeDataMap.keySet()) {
                JsonObject nodeInfo = nodeDataMap.get(mac);
                allNodeStatus.add(mac, nodeInfo.getAsJsonObject(mac));
            }

            // Convert JSON to string
            String response = allNodeStatus.toString();

            // Encode response and send to client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(CORE.anonymousEncode(r_key, response));
            out.flush();
        } catch (IOException e) {
            LoggerUtil.printAnonymous("Error sending all node status JSON: " + e.getMessage());
        }
    }

    private static void forwardDataToMac(String data, String macAddress) {
        try {
            // Check if the macAddress exists in nodeDataMap
            if (nodeDataMap.containsKey(macAddress)) {
                JsonObject nodeInfo = nodeDataMap.get(macAddress);
                String ipPort = nodeInfo.getAsJsonObject(macAddress).get("IP").getAsString();
                String[]  pack= CORE.decode_payloadBlock2(r_key,data);

                // Extract IP and port from IP:port format
                String newSource = CORE.payloadBlock2(r_key,pack[0],pack[2],"1");
                String[] ipPortArray = ipPort.split(":");

                if (ipPortArray.length == 2) {
                    String ip = ipPortArray[0];
                    int port = Integer.parseInt(ipPortArray[1]);

                    // Create a socket and connect to the IP and port
                    try (Socket socket = new Socket(ip, port);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                        // Send data to the specified IP and port
                        out.println(newSource);
                        out.flush();
                    } catch (IOException e) {
                        LoggerUtil.printForward("Error communicating with " + ip + ":" + port + ": " + e.getMessage());
                    }
                    LoggerUtil.printForward("Incoming packets have been forwarded to ["+ip+":"+port+"]");
                } else {
                    LoggerUtil.printForward("Malformed IP:port format for MAC address " + macAddress);
                }
            } else {
                LoggerUtil.printForward("MAC address " + macAddress + " not found in nodeDataMap");
            }
        } catch (Exception e) {
            LoggerUtil.printForward("Error forwarding data: " + e.getMessage());
        }
    }

    public static void startUdpServer() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(10089);

            while (true) {
                try {
                    byte[] receiveBuffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);

                    // Convert received data to string using UTF-8 encoding
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    // Decode the received message
                    String decodedMessage;
                    try {
                        decodedMessage = CORE.anonymousDecode(r_key, message);
                    } catch (Exception e) {
                        LoggerUtil.printDevice("Error decoding message: " + e.getMessage());
                        continue; // Skip this packet and move to the next one
                    }

                    // Parse JSON data
                    JsonObject jsonObject;
                    try {
                        jsonObject = JsonParser.parseString(decodedMessage).getAsJsonObject();
                    } catch (JsonSyntaxException e) {
                        LoggerUtil.printDevice("Received data is not valid JSON. Dropping packet.");
                        continue; // Skip this packet and move to the next one
                    } catch (IllegalStateException e) {
                        LoggerUtil.printDevice("Received data is not a JSON object. Dropping packet.");
                        continue; // Skip this packet and move to the next one
                    }

                    // Ensure JSON object is not empty
                    if (jsonObject == null || jsonObject.keySet().isEmpty()) {
                        LoggerUtil.printDevice("Parsed JSON object is empty or null. Dropping packet.");
                        continue; // Skip this packet and move to the next one
                    }

                    // Get the dynamic key
                    String customKey = jsonObject.keySet().iterator().next();

                    // Update nodeDataMap and record the update timestamp
                    jsonObject.addProperty("timestamp", System.currentTimeMillis());
                    updateNodeDataMap(customKey, jsonObject);

                    // Echo back the decoded message to the client
                    byte[] sendData = decodedMessage.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    socket.send(sendPacket);

                } catch (IOException e) {
                    LoggerUtil.printDevice("Error during UDP packet processing: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LoggerUtil.printDevice("An error occurred in UDP server: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }


    private static void updateNodeDataMap(String customKey, JsonObject jsonObject) {
        // Ensure thread safety with synchronized block
        synchronized (nodeDataMap) {
            nodeDataMap.put(customKey, jsonObject);
        }
    }


    private static void cleanupStaleEntries() {
        long currentTimestamp = System.currentTimeMillis();
        long timeout = 10000; // Set timeout to 10 seconds

        // Iterate over nodeDataMap and remove stale entries
        synchronized (nodeDataMap) {
            nodeDataMap.entrySet().removeIf(entry -> {
                JsonObject deviceInfo = entry.getValue();
                long lastUpdateTime = deviceInfo.get("timestamp").getAsLong();

                // Remove the entry if the last update time is more than 10 seconds ago
                return (currentTimestamp - lastUpdateTime) > timeout;
            });
        }
    }


//    private static void printNodeDataMap() {
//        System.out.println("\n===== Current nodeDataMap Contents =====");
//        for (Map.Entry<String, JsonObject> entry : nodeDataMap.entrySet()) {
//            String key = entry.getKey();
//            JsonObject value = entry.getValue();
//            System.out.println("Key: " + key + ", Value: " + value.toString());
//        }
//        System.out.println("=======================================\n");
//    }

    public static boolean controlDevice(String id, String stat, String type, Socket clientSocket) {
        LoggerUtil.printAnonymous("Received command for device ID: " + id + ", stat: " + stat + ", type: " + type);

        String typeKeyId = findKeyByType(configJson, type); // Get the ID from the configuration file based on the specified type

        if (id.equals("0")) { // Group control mode
            if (stat.equals("1")) {
                controlAllDevices("RUN", typeKeyId == null ? null : type);
            } else if (stat.equals("2")) {
                controlAllDevices("STOP", typeKeyId == null ? null : type);
            } else if (stat.equals("3")) {
                sendAllDeviceStatusJson(clientSocket);
            } else {
                LoggerUtil.printAnonymous("Unsupported stat command: " + stat);
                return false;
            }
        } else { // Single device control mode
            ClientInfo clientInfo = DeviceSockets.get(id);
            if (clientInfo == null) {
                LoggerUtil.printAnonymous("Client with ID " + id + " not found.");
                return false;
            }

            if (stat.equals("1")) {
                controlSingleDevice(id, "RUN");
            } else if (stat.equals("2")) {
                controlSingleDevice(id, "STOP");
            } else if (stat.equals("3")) {
                sendSpecificDeviceStatusJson(id, clientSocket);
            } else {
                LoggerUtil.printAnonymous("Unsupported stat command: " + stat);
                return false;
            }
        }
        return true;
    }


    private static void controlAllDevices(String command, String useType) {
        LoggerUtil.printAnonymous("Performing '" + command + "' on all devices.");

        for (Map.Entry<String, ClientInfo> entry : DeviceSockets.entrySet()) {
            String clientId = entry.getKey();
            ClientInfo clientInfo = entry.getValue();
            String ctype = clientInfo.getType();

            if (useType == null || useType.equals(ctype)) {
                String typeKeyId = findKeyByType(configJson, ctype);
                if (typeKeyId == null) {
                    LoggerUtil.printError("Configuration key not found for device type: " + ctype);
                    continue;
                }

                String cmd = command.equals("RUN") ?
                        configJson.getAsJsonObject("Device").getAsJsonObject(typeKeyId).get("RUN").getAsString() :
                        configJson.getAsJsonObject("Device").getAsJsonObject(typeKeyId).get("STOP").getAsString();

                sendStringToDevice(clientId, cmd);
            }
        }
    }


    private static void controlSingleDevice(String id, String command) {
        ClientInfo clientInfo = DeviceSockets.get(id);
        if (clientInfo == null) {
            LoggerUtil.printAnonymous("Client with ID " + id + " not found.");
            return;
        }

        String ctype = clientInfo.getType();
        String typeKeyId = findKeyByType(configJson, ctype);
        if (typeKeyId == null) {
            LoggerUtil.printAnonymous("Configuration key not found for device type: " + ctype);
            return;
        }

        String cmd = command.equals("RUN") ?
                configJson.getAsJsonObject("Device").getAsJsonObject(typeKeyId).get("RUN").getAsString() :
                configJson.getAsJsonObject("Device").getAsJsonObject(typeKeyId).get("STOP").getAsString();

        sendStringToDevice(id, cmd);
    }


    private static void sendAllDeviceStatusJson(Socket clientSocket) {
        String jsonResponse = getAllDeviceStatusJson();
        try (OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream())) {
            out.write(CORE.anonymousEncode(s_key, jsonResponse));
            out.flush();
            LoggerUtil.printAnonymous("Sent JSON response to client for all devices.");
        } catch (IOException e) {
            LoggerUtil.printAnonymous("Failed to send JSON response to client: " + e.getMessage());
        }
    }


    private static void sendSpecificDeviceStatusJson(String id, Socket clientSocket) {
        String jsonResponse = getSpecificDeviceStatusJson(id);
        try (OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream())) {
            out.write(CORE.anonymousEncode(s_key, jsonResponse));
            out.flush();
            LoggerUtil.printAnonymous("Sent JSON response to client for device ID: " + id);
        } catch (IOException e) {
            LoggerUtil.printAnonymous("Failed to send JSON response to client for device ID " + id + ": " + e.getMessage());
        }
    }



    private static String getAllDeviceStatusJson() {
        JsonObject responseJson = new JsonObject();
        JsonArray deviceArray = new JsonArray();

        synchronized (DeviceSockets) {
            DeviceSockets.forEach((clientId, clientInfo) -> {
                JsonObject deviceJson = new JsonObject();
                deviceJson.addProperty("DeviceID", clientId);
                deviceJson.addProperty("DeviceType", clientInfo.getType());
                deviceArray.add(deviceJson);
            });
        }

        responseJson.add("Devices", deviceArray);

        return gson.toJson(responseJson);
    }


    private static String getSpecificDeviceStatusJson(String deviceId) {
        JsonObject responseJson = new JsonObject();

        synchronized (DeviceSockets) {
            ClientInfo clientInfo = DeviceSockets.get(deviceId);
            if (clientInfo != null) {
                responseJson.addProperty("DeviceID", deviceId);
                responseJson.addProperty("DeviceType", clientInfo.getType());
            } else {
                responseJson.addProperty("error", "Device with ID " + deviceId + " not found.");
            }
        }

        return gson.toJson(responseJson);
    }


    private static boolean sendStringToDevice(String clientId, String message) {
        ClientInfo clientInfo;
        synchronized (DeviceSockets) {
            clientInfo = DeviceSockets.get(clientId);
        }

        if (clientInfo == null) {
            LoggerUtil.printAnonymous("Client with ID " + clientId + " not found.");
            return false;
        }

        try {
            OutputStream outputStream = clientInfo.getSocket().getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();
            LoggerUtil.printAnonymous("Sent message to client with ID: " + clientId);
            return true;
        } catch (IOException e) {
            LoggerUtil.printAnonymous("Failed to send message to client " + clientId + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean validateTimestamp(long clientTimestamp) {
        long serverTimestamp = System.currentTimeMillis();
        long timeout = configJson.getAsJsonObject("SERVER").get("NODE_TIMEOUT").getAsLong();

        long timeDifference = serverTimestamp - clientTimestamp;

        boolean isValid = timeDifference < timeout;
        if (isValid) {
            return true;
        } else {
            return false;
        }
    }

    private static void startUdpClient() {
        ScheduledExecutorService udpClientScheduler = Executors.newSingleThreadScheduledExecutor();
        udpClientScheduler.scheduleAtFixedRate(() -> {
            try {
                // Construct JSON data
                JsonObject jsonData = new JsonObject();
                String macAddress = CORE.getFirstRealMacAddress();
                jsonData.add(macAddress, createDeviceJson());
                String jsonStr = jsonData.toString();

                // Prepare UDP packet
                InetAddress serverAddress = InetAddress.getByName(data_push_ip);
                int serverPort = data_push_port;
                byte[] sendData = CORE.anonymousEncode(r_key, jsonStr).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);

                // Send UDP packet
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    LoggerUtil.printError("Error sending UDP packet: " + e.getMessage());
                }
            } catch (Exception e) {
                LoggerUtil.printError("Error preparing UDP packet: " + e.getMessage());
            }
        }, 0, 6, TimeUnit.SECONDS);
    }


    private static JsonObject createDeviceJson() {
        JsonObject deviceJson = new JsonObject();
        JsonArray devicesArray = new JsonArray();

        // Populate devices array
        for (Map.Entry<String, ClientInfo> entry : DeviceSockets.entrySet()) {
            String deviceId = entry.getKey();
            ClientInfo clientInfo = entry.getValue();

            JsonObject deviceInfo = new JsonObject();
            deviceInfo.addProperty("DeviceID", deviceId);
            deviceInfo.addProperty("DeviceType", clientInfo.getType());

            devicesArray.add(deviceInfo);
        }

        // Add IP and devices array to deviceJson
        try {
            String macAddress = CORE.getFirstRealMacAddress();
            String ip;
            try {
                ip = getIpForMac(macAddress) + ":" + anonymous_port; // Assuming this method is implemented
            } catch (UnknownHostException e) {
                LoggerUtil.printError("Error retrieving IP for MAC address: " + e.getMessage());
                ip = "unknown"; // Default value in case of exception
            }
            deviceJson.addProperty("IP", ip);
        } catch (SocketException e) {
            LoggerUtil.printError("Error retrieving MAC address: " + e.getMessage());
            deviceJson.addProperty("IP", "unknown"); // Example default value
        }

        deviceJson.add("Devices", devicesArray);

        return deviceJson;
    }



    private static String getIpForMac(String macAddress) throws UnknownHostException {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null && mac.length == 6) { // Ensure MAC address length is valid
                    StringBuilder macAddressBuilder = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macAddressBuilder.append(String.format("%02X", mac[i]));
                    }
                    String currentMac = macAddressBuilder.toString();
                    if (currentMac.equalsIgnoreCase(macAddress)) {
                        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress address = addresses.nextElement();
                            if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address instanceof Inet4Address) {
                                return address.getHostAddress();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace(); // Handle exception as per your requirement
        }
        throw new UnknownHostException("MAC address not found or could not resolve to IP");
    }
}