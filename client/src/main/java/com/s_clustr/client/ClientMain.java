package com.s_clustr.client;
import java.util.Random;


public class ClientMain {
    private static boolean os_windows = getOperatingSystem();
    public static void main(String[] args) {
        printColoredAsciiArt();
        S_Clustr sClustr = new S_Clustr("3.0");
        sClustr.cmdLoop(); // Start command loop
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
        String clientInfo = "S-H4CK13@Мартин. S-Clustr(Shadow Cluster) Client";


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
