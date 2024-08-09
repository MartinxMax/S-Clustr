package com.s_clustr.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;

public class LoggerUtil {
    private static final Logger logger = Logger.getLogger(LoggerUtil.class.getName());
    private static boolean os_windows = getOperatingSystem();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static {
        AnsiConsole.systemInstall();
        // Remove the default console handler
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Add a custom console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                return record.getMessage() + "\n";
            }
        });
        rootLogger.addHandler(consoleHandler);
    }
    private static String getTimestamp() {
        return LocalDateTime.now().format(formatter);
    }

    public static void printInfo(String message) {
        logger.info(Ansi.ansi().fg(Ansi.Color.GREEN).bold().a("[" + getTimestamp() + "] [INFO] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printError(String message) {
        logger.severe(Ansi.ansi().fg(Ansi.Color.RED).bold().a("[" + getTimestamp() + "] [ERROR] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printSuccess(String message) {
        logger.info(Ansi.ansi().fg(Ansi.Color.GREEN).bold().a("[" + getTimestamp() + "] [SUCCESS] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printWarning(String message) {
        logger.warning(Ansi.ansi().fg(Ansi.Color.YELLOW).bold().a("[" + getTimestamp() + "] [WARNING] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printForward(String message) {
        logger.info(Ansi.ansi().fg(Ansi.Color.CYAN).bold().a("[" + getTimestamp() + "] [FORWARD] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printDevice(String message) {
        logger.info(Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().a("[" + getTimestamp() + "] [DEVICE] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printAnonymous(String message) {
        logger.info(Ansi.ansi().fg(Ansi.Color.CYAN).bold().a("[" + getTimestamp() + "] [ANONYMOUS] ").boldOff().fgBrightBlack().a(message).reset().toString());
    }

    public static void printColoredAsciiArt() {
        String asciiArtTemplate =
                "  /$$$$$$           /$$$$$$  /$$                       /$$              \n" +
                        " /$$__  $$         /$$__  $$| $$                      | $$              \n" +
                        "| $$  \\__/        | $$  \\__/| $$ /$$   /$$  /$$$$$$$ /$$$$$$    /$$$$$$ \n" +
                        "|  $$$$$$  /$$$$$$| $$      | $$| $$  | $$ /$$_____/|_  $$_/   /$$__  $$\n" +
                        " \\____  $$|______/| $$      | $$| $$  | $$|  $$$$$$   | $$    | $$  \\__/\n" +
                        " /$$  \\ $$        | $$    $$| $$| $$  | $$ \\____  $$  | $$ /$$| $$      \n" +
                        "|  $$$$$$/        |  $$$$$$/| $$|  $$$$$$/ /$$$$$$$/  |  $$$$/| $$      \n" +
                        " \\______/          \\______/ |__/ \\______/ |_______/    \\___/  |__/      \n";

        String gradientColors = "https://github.com/MartinxMax\n";
        String clientInfo = "S-H4CK13@Мартин. S-Clustr(Shadow Cluster) Server";


        StringBuilder coloredAsciiArt = new StringBuilder();
        Random random = new Random();
        for (String line : asciiArtTemplate.split("\n")) {
            if (os_windows) {
                coloredAsciiArt.append(line).append("\n");
            } else {
                int randomColor = 16 + random.nextInt(240); // 256 colors
                coloredAsciiArt.append("\033[38;5;").append(randomColor).append("m").append(line).append("\033[0m\n");
            }
        }

        System.out.print(coloredAsciiArt.toString());

        String coloredGradientColors;
        String coloredClientInfo;

        if (os_windows) {
            coloredGradientColors = gradientColors;
            coloredClientInfo = clientInfo;
        } else {
            int randomColor1 = 16 + random.nextInt(240);
            int randomColor2 = 16 + random.nextInt(240);

            coloredGradientColors = "\033[38;5;" + randomColor1 + "m" + gradientColors + "\033[0m";
            coloredClientInfo = "\033[38;5;" + randomColor2 + "m" + clientInfo + "\033[0m";
        }

        System.out.print(centerText(coloredGradientColors, asciiArtTemplate));
        System.out.print(centerText(coloredClientInfo, asciiArtTemplate));
        System.out.println();
    }

    public static String centerText(String text, String referenceText) {
        String[] referenceLines = referenceText.split("\n");
        int maxLength = 0;
        for (String line : referenceLines) {
            if (line.length() > maxLength) {
                maxLength = line.length();
            }
        }

        String[] lines = text.split("\n");
        StringBuilder centeredText = new StringBuilder();
        for (String line : lines) {
            int padding = (maxLength - line.length()) / 2;
            for (int i = 0; i < padding; i++) {
                centeredText.append(" ");
            }
            centeredText.append(line).append("\n");
        }

        return centeredText.toString();
    }


    public static boolean getOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return true;
        } else {
            return false;
        }
    }

}
