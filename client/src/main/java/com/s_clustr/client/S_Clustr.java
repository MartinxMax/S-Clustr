package com.s_clustr.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;



public class S_Clustr {
    private String version;
    private Map<String, Option> options;
    private String currentNode; // Current node MAC address
    private Map<String, Map<String, Object>> nodeServerInfo; // Stores getinfo information
    private Deque<String> nodeHistory; // Node history records

    public S_Clustr(String version) {
        this.version = version;
        this.options = new HashMap<>();
        this.nodeServerInfo = new HashMap<>();
        this.nodeHistory = new ArrayDeque<>();
        initializeOptions();
    }

    private void initializeOptions() {
        this.options.put("node-key", new Option("", "Node key"));
        this.options.put("ring-key", new Option("", "Ring Network key"));
        this.options.put("root-host", new Option("", "Root Server ip"));
        this.options.put("root-port", new Option("10090", "Root Server port"));
        this.options.put("id", new Option("0", "Device ID [0-n/0 represents specifying all]"));
        this.options.put("pwr", new Option("3", "Device behavior (run[1]/stop[2]/Query device status[3])"));
        this.options.put("type", new Option("ALL", "Control type (ALL: control all devices / specify manually)"));
        this.currentNode = "ROOT";
    }

    public void cmdLoop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to S-Clustr console. Type 'help' or '?' to list commands.");

        boolean initialized = false; // Flag to track initialization

        while (true) {
            try {
                updatePrompt(); // Update command prompt

                String input = scanner.nextLine().trim();

                if (input.equals("exit")) {
                    if (nodeHistory.size() > 0) {
                        nodeHistory.pop(); // Pop the top element when exiting
                    } else {
                        break;
                    }
                } else if (input.startsWith("set ")) {
                    handleSetCommand(input.substring(4).trim());
                } else if (input.equals("options")) {
                    showOptions();
                } else if (input.equals("run")) {
                    if (!initialized) {
                        System.out.println("[-] Please initialize by running 'init' command first.");
                        continue;
                    }
                    handleRunCommand("run");
                } else if (input.equals("getinfo")) {
                    if (!initialized) {
                        System.out.println("[-] Please initialize by running 'init' command first.");
                        continue;
                    }
                    handleRunCommand("getinfo");
                } else if (input.startsWith("goto ")) {
                    if (!initialized) {
                        System.out.println("[-] Please initialize by running 'init' command first.");
                        continue;
                    }
                    handleGotoCommand(input.substring(5).trim());
                } else if (input.equals("init")) {
                    handleInitCommand();
                    initialized = true; // Mark as initialized
                } else if (input.equals("help") || input.equals("?")) {
                    printHelp();
                } else {
                    System.out.println("Unknown command. Type 'help' or '?' to list commands.");
                }
            } catch (Exception e) {
                System.err.println("[-] Error handling command: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private void updatePrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("[S-H4CK13@S-Clustr]<%s>", version));

        if (!nodeHistory.isEmpty()) {
            prompt.append("@");
            Iterator<String> iterator = nodeHistory.iterator();
            while (iterator.hasNext()) {
                String node = iterator.next();
                prompt.append(node);
                if (iterator.hasNext()) {
                    prompt.append("-");
                }
            }
        }

        prompt.append("# ");
        this.currentNode = nodeHistory.peek();
        System.out.print(prompt.toString());
    }

    private void handleSetCommand(String arg) {
        String[] parts = arg.split("\\s+", 2);
        if (parts.length != 2) {
            System.out.println("[-] Invalid syntax. Usage: set <option> <value>");
            return;
        }

        String option = parts[0];
        String value = parts[1];
        if (options.containsKey(option)) {
            if (option.equals("pwr")) {
                // Ensure value is between "1", "2", or "3"
                if (value.equals("1") || value.equals("2") || value.equals("3")) {
                    options.get(option).setValue(value);
                    System.out.printf("[*] %s => %s%n", option, value);
                } else {
                    System.out.printf("[-] Invalid value for '%s'. Allowed values: 1, 2, 3.%n", option);
                }
            } else {
                options.get(option).setValue(value);
                System.out.printf("[*] %s => %s%n", option, value);
            }
        } else {
            System.out.printf("[-] Unknown variable: %s%n", option);
        }
    }

    private void showOptions() {
        System.out.println("| Name           | Current Setting | Required | Description");
        System.out.println("|:--------------:|:---------------:|:--------:|:-----------------");

        for (Map.Entry<String, Option> entry : options.entrySet()) {
            String name = String.format("%-14s", entry.getKey());
            String setting = String.format("%-15s", entry.getValue().getValue().isEmpty() ? " " : entry.getValue().getValue());
            String required = entry.getValue().getValue().isEmpty() ? "yes" : "no";
            String description = entry.getValue().getDescription();

            System.out.printf("| %s | %s | %-8s | %s%n", name, setting, required, description);
        }

        System.out.println("|:--------------:|:---------------:|:--------:|:-----------------");
    }

    private void handleRunCommand(String command) {
        String pwrValue = options.get("pwr").getValue();
        if (!pwrValue.equals("1") && !pwrValue.equals("2") && !pwrValue.equals("3")) {
            System.out.println("[-] 'pwr' must be set to '1', '2', or '3' to execute this command.");
            return;
        }

        String flag = (nodeHistory.size() >= 2) ? "0" : "1";//Forwarded or Not
        String flag2 = (nodeHistory.size() >= 2) ? "2" : "3";//Query Root or Query Child
        // Proceed with the command execution logic
        try {
            String target = options.get("root-host").getValue();
            int port = Integer.parseInt(options.get("root-port").getValue());

            System.out.println("[*] Sending payload to the server...");
            Socket socket = new Socket(target, port);

            if (command.equals("getinfo")) {
                String data = CORE.payloadBlock1(options.get("node-key").getValue(), "0.0.0.0", "0", "ARDU", "0");
                String data2 = CORE.payloadBlock2(options.get("ring-key").getValue(), data, "CCCCCCCCCCCC", "3");
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.println(data2);
                out.flush();

                // Receive server response
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                String responseEnc = CORE.anonymousDecode(options.get("ring-key").getValue(), response);
                Gson gson = new Gson();
                nodeServerInfo = gson.fromJson(responseEnc, new TypeToken<Map<String, Map<String, Object>>>() {
                }.getType());

                // Display the saved information
                parseAndDisplayNodeServer(nodeServerInfo);
            } else {
                if (pwrValue.equals("1")) {
                    String data = CORE.payloadBlock1(options.get("node-key").getValue(), options.get("root-host").getValue(), options.get("id").getValue(), options.get("type").getValue(), pwrValue);
                    String data2 = CORE.payloadBlock2(options.get("ring-key").getValue(), data, currentNode, flag);
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    out.println(data2);
                    out.flush();
                } else if (pwrValue.equals("2")) {
                    String data = CORE.payloadBlock1(options.get("node-key").getValue(), options.get("root-host").getValue(), options.get("id").getValue(), options.get("type").getValue(), pwrValue);
                    String data2 = CORE.payloadBlock2(options.get("ring-key").getValue(), data, currentNode, flag);
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    out.println(data2);
                    out.flush();
                } else if (pwrValue.equals("3")) { // Query node status
                    String data = CORE.payloadBlock1(options.get("node-key").getValue(), "0.0.0.0", options.get("id").getValue(), options.get("type").getValue(), pwrValue);
                    String data2 = CORE.payloadBlock2(options.get("ring-key").getValue(), data, currentNode, flag2);
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    out.println(data2);
                    out.flush();
                    // Receive server response
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    String responseEnc = CORE.anonymousDecode(options.get("ring-key").getValue(), response);
                    printNodeInfo(responseEnc);
                }
            }
            socket.close();
        } catch (Exception e) {
            System.err.println("[!] The server may not have any devices connected temporarily, or there may be another error");
        }
    }

    private void handleInitCommand() {
        try {
            String target = options.get("root-host").getValue();
            int port = Integer.parseInt(options.get("root-port").getValue());

            System.out.println("[*] Connecting to the server...");
            Socket socket = new Socket(target, port);

            System.out.println("[*] Sending init command to the server...");
            // Send init payload to the server
            String data = CORE.payloadBlock1(options.get("node-key").getValue(), "0.0.0.0", "0", "ARDU", "0");
            String data2 = CORE.payloadBlock2(options.get("ring-key").getValue(), data, "CCRCCCOCCOCT", "2");
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.println(data2);
            out.flush();

            // Receive server response
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            String responseEnc = CORE.anonymousDecode(options.get("ring-key").getValue(), response);

            // Parse JSON response and update currentNode
            Gson gson = new Gson();
            Map<String, String> responseMap = gson.fromJson(responseEnc, new TypeToken<Map<String, String>>() {
            }.getType());
            if (responseMap.containsKey("ROOT")) {
                currentNode = responseMap.get("ROOT");
                System.out.println("[*] Updated currentNode to: " + currentNode);
                nodeHistory.clear();
                nodeHistory.push(currentNode); // Add current node to history
            } else {
                System.out.println("[-] ROOT key not found in the response.");
            }

            socket.close();
        } catch (Exception e) {
            System.err.println("[-] Error during init command: " + e.getMessage());
        }
    }

    public static void printNodeInfo(String jsonData) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(jsonData, JsonElement.class);

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (!jsonObject.has("timestamp")) {
                System.out.println("[-] Currently Root node no devices online");
                return;
            }

            JsonElement timestampElement = jsonObject.get("timestamp");

            if (!timestampElement.isJsonPrimitive() || !timestampElement.getAsJsonPrimitive().isNumber()) {
                System.err.println("Invalid timestamp format: " + timestampElement.toString());
                return;
            }

            // Print timestamp
            System.out.println("===========================================");
            long timestamp = timestampElement.getAsLong();
            String readableTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
            System.out.println("Timestamp: " + readableTimestamp);
            System.out.println("===========================================");

            // Check for valid node information
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getKey().equals("timestamp")) {
                    continue; // Ignore the timestamp field
                }

                JsonElement nodeElement = entry.getValue();
                if (!nodeElement.isJsonObject()) {
                    System.err.println("Invalid node info format for NodeID: " + entry.getKey());
                    continue; // Skip this entry if it's not a JSON object
                }

                JsonObject nodeInfo = nodeElement.getAsJsonObject();

                if (!nodeInfo.has("IP")) {
                    System.err.println("Missing IP for NodeID: " + entry.getKey());
                    continue; // Skip this entry if IP is missing
                }

                String ip = nodeInfo.get("IP").getAsString();

                if (!nodeInfo.has("Devices")) {
                    System.err.println("No devices found for NodeID: " + entry.getKey());
                    continue; // Skip this entry if Devices array is missing
                }

                JsonElement devicesElement = nodeInfo.get("Devices");

                // Handle device information
                if (devicesElement.isJsonArray()) {
                    JsonArray devices = devicesElement.getAsJsonArray();

                    // Print NodeID and IP
                    System.out.printf("%-20s: %s\n", "NodeID", entry.getKey());
                    System.out.printf("%-20s: %s\n", "IP", ip);
                    System.out.printf("%-20s: %d\n", "Devices Online", devices.size());
                    System.out.println("-------------------------------------------");

                    // Print device information
                    if (devices.size() > 0) {
                        System.out.printf("%-10s %-20s\n", "DeviceID", "DeviceType");
                        System.out.println("-------------------------------------------");
                        for (JsonElement deviceElement : devices) {
                            if (!deviceElement.isJsonObject()) {
                                System.err.println("Invalid device format for NodeID: " + entry.getKey());
                                continue; // Skip this device if it's not a JSON object
                            }
                            JsonObject device = deviceElement.getAsJsonObject();
                            if (!device.has("DeviceID") || !device.has("DeviceType")) {
                                System.err.println("Invalid device structure for NodeID: " + entry.getKey());
                                continue; // Skip this device if it's missing required fields
                            }
                            String deviceId = device.get("DeviceID").getAsString();
                            String deviceType = device.get("DeviceType").getAsString();
                            System.out.printf("%-10s %-20s\n", deviceId, deviceType);
                        }
                    } else {
                        System.out.println("[-] Currently node no devices online.");
                    }

                    // Print separator
                    System.out.println("===========================================");
                } else {
                    System.err.println("Invalid devices format for NodeID: " + entry.getKey());
                }
            }
        } else {
            System.err.println("Invalid JSON data: Expected an object but found " + (jsonElement.isJsonNull() ? "null" : jsonElement.getClass().getSimpleName()));
        }
    }

    private void parseAndDisplayNodeServer(Map<String, Map<String, Object>> nodeServerMap) {
        try {
            // Print table headers
            System.out.println("| Node ID        | IP Address          | Device Online |");
            System.out.println("|:---------------|:--------------------|:-------------|");

            // Print table rows
            for (Map.Entry<String, Map<String, Object>> entry : nodeServerMap.entrySet()) {
                String nodeMacAddress = entry.getKey();
                String nodeIpAddress = (String) entry.getValue().get("IP");
                List<Map<String, String>> devices = (List<Map<String, String>>) entry.getValue().get("Devices");

                int deviceCount = devices != null ? devices.size() : 0;

                System.out.printf("| %-14s | %-18s | %-12d |%n", nodeMacAddress, nodeIpAddress, deviceCount);
            }

            System.out.println("|:---------------|:--------------------|:-------------|");
        } catch (Exception e) {
            System.err.println("[-] Error parsing and displaying NodeServer data: " + e.getMessage());
        }
    }

    private void handleGotoCommand(String macAddress) {
        try {
            if (macAddress.equals(currentNode)) {
                return;
            } else if (macAddress.equals(nodeHistory.peekLast())) {
                return;
            } else if (macAddress.length() != 12) {
                System.out.println("[!] The node is incorrect. Use the 'getinfo' command to retrieve node information");
                return;
            }
            this.currentNode = macAddress;

            if (nodeHistory.size() >= 2) {
                nodeHistory.pop(); // Remove top element
            }

            nodeHistory.push(currentNode); // Add current node to history
        } catch (Exception e) {
            System.err.println("[-] Error setting current node: " + e.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help or ?         - Show this help message");
        System.out.println("  set <option> <value> - Set the value of an option");
        System.out.println("  options           - Show current options");
        System.out.println("  run               - Execute the run command");
        System.out.println("  getinfo           - Execute the getinfo command");
        System.out.println("  init              - Initialize the connection and get ROOT node");
        System.out.println("  goto <MAC>        - Switch to specified node MAC address");
        System.out.println("  exit              - Exit the S-Clustr console");
    }

    public static void main(String[] args) {
        String version = "3.0";
        S_Clustr sClustr = new S_Clustr(version);
        sClustr.cmdLoop();
    }
}