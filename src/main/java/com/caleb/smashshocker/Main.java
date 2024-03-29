package com.caleb.smashshocker;

import com.github.sarxos.webcam.Webcam;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import nu.pattern.OpenCV;
import org.imgscalr.Scalr;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static HashMap<Integer, Integer> playerDamages = new HashMap<>();
    private static HashMap<Integer, Integer> playerDamageColors = new HashMap<>();
    private static HashMap<Integer, JLabel> damageLabels = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Integer>> playerDamageHistory = new HashMap<>(); // playerNum -> (color -> count)
    private static HashMap<Integer, Integer> timesHit = new HashMap<>();
    private static HashMap<Integer, Integer> damageCooldowns = new HashMap<>();

    private static Tesseract tesseract;
    private static JLabel p1Damage;
    private static JLabel p2Damage;
    private static JLabel p3Damage;
    private static JLabel p4Damage;
    private static JLabel gameTime;
    private static JLabel gameStage;
    private static JLabel connected;
    private static JLabel image;

    private static JFrame p1Image;
    private static JFrame p2Image;

    private static ArduinoController arduinoController;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting...");
        OpenCV.loadLocally();

        WebcamManager webcamManager = WebcamManager.getInstance();
        if(!webcamManager.init()) {
            System.out.println("Failed to initialize capture card");
            return;
        }

        // Creates J Window
        createWindow();

        arduinoController = new ArduinoController();
        if(arduinoController.openPort()) {
            connected.setText("CONNECTED");
        } else {
            connected.setText("NOT CONNECTED");
            System.out.println("Shock box failed to connect.");
            System.exit(0);
        }
        try {

            runTask();

        }
        catch (TesseractException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private static void createWindow() throws Exception {
        JFrame frame = new JFrame("Smash Shocker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.setSize(1500, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.setLocation(0, 0);

        for(int i = 1; i <= 4; i++) {
            JButton button = new JButton(i + "");
            int finalI = i;
            button.addActionListener(e -> {
                try {
                    arduinoController.sendChar(finalI);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            frame.add(button);
        }

        p1Damage = new JLabel("P1: 0");
        p1Damage.setFont(new Font("Arial", Font.BOLD, 30));
        p2Damage = new JLabel("P2: 0");
        p2Damage.setFont(new Font("Arial", Font.BOLD, 30));
        p3Damage = new JLabel("P3: 0");
        p3Damage.setFont(new Font("Arial", Font.BOLD, 30));
        p4Damage = new JLabel("P4: 0");
        p4Damage.setFont(new Font("Arial", Font.BOLD, 30));

        gameTime = new JLabel("Time: 0");
        connected = new JLabel("NOT CONNECTED");
        gameStage = new JLabel("");

        image = new JLabel(new ImageIcon(WebcamManager.getInstance().takeScreenShot()));

        frame.add(p1Damage);
        frame.add(p2Damage);
        frame.add(p3Damage);
        frame.add(p4Damage);
        frame.add(gameTime);
        frame.add(connected);
        frame.add(gameStage);
        frame.add(image);

        damageLabels.put(1, p1Damage);
        damageLabels.put(2, p2Damage);
        damageLabels.put(3, p3Damage);
        damageLabels.put(4, p4Damage);
    }

    /**
     * Our main task that gets the screenshots and checks them
     * @throws Exception
     */
    public static void runTask() throws Exception {
        System.out.println("Running task...");
        Timer timer = new Timer();

        final int INTERVAL = 50;

        timer.schedule(new TimerTask() {
            int iter = 0;
            int playerCount = 0;
            int timesNA = 5;
            int timesConfirmed = 0;
            int lastTime = 0;

            int iterAfterGame = 0;
            @Override
            public void run() {
                boolean playing = true;
                BufferedImage screenCapture = null;
                Graphics2D g2d = null;

                for(Map.Entry<Integer, Integer> damageCooldown : damageCooldowns.entrySet()) {
                    if(damageCooldown.getValue() > 0) {
                        damageCooldowns.put(damageCooldown.getKey(), damageCooldown.getValue() - 1);
                    }
                }

                try {

                    if(iterAfterGame > 0) {
                        iterAfterGame--;
                        playing = false;
                    }


                    screenCapture = WebcamManager.getInstance().takeScreenShot();

                    g2d.setFont(new Font("Serif", Font.BOLD, 30));
                    image.setIcon(new ImageIcon(screenCapture));

                    double pauseScreenSimilarity = isPauseScreen(screenCapture);

                    double killScreenSimilarity = isKillScreen(screenCapture);

                    if(isCSS(screenCapture) <= 0.1) {
                        gameStage.setText("CSS");
                        playing = false;
                        System.out.println("CSS: " + isCSS(screenCapture));
                        g2d.drawString("CSS", 10, 10);
                    } else if(isCSSReady(screenCapture) <= 0.05) {
                        gameStage.setText("CSS READY");
                        playing = false;
                        System.out.println("CSS READY: " + isCSSReady(screenCapture));
                        g2d.drawString("CSS READY", 10, 10);
                    } else if(isStageSelect(screenCapture) <= 0.1) {
                        gameStage.setText("STAGE SELECT");
                        playing = false;
                        System.out.println("STAGE SELECT: " + isStageSelect(screenCapture));
                        g2d.drawString("STAGE SELECT", 10, 10);
                    }

                    if(playing) {
                        if(killScreenSimilarity<= 0.10) {
                            playing = false;
                            gameStage.setText("KILL SCREEN");
                            System.out.println("KILL SCREEN: " + killScreenSimilarity);
                            g2d.drawString("KILL SCREEN", 10, 10);
                        }
                        if(pauseScreenSimilarity <= 0.1) {
                            gameStage.setText("PAUSED");
                            playing = false;
                            System.out.println("PAUSED: " + pauseScreenSimilarity);
                            g2d.drawString("PAUSED", 10, 10);
                        }
                        if(isGame(screenCapture) <= 0.1) {
                            gameStage.setText("GAME SCREEN");
                            playing = false;
                            iterAfterGame = 200;
                            System.out.println("GAME SCREEN: " + isGame(screenCapture));
                            g2d.drawString("GAME SCREEN", 10, 10);
                        }
                    }


                    if(playing) {
                        BufferedImage filtered = ImageProcessing.applyFilters(screenCapture);
                        playerCount = countPlayers2(screenCapture);
                        if (playerCount == 0)
                            timesNA++;
                        else {
                            //Assuming we are on a menu because 0 players have been found over 5 iterations, do 3 more checks to MAKE sure we are not in a game, so we dont get shocked mid menu
                            if (timesNA > 5) {
                                for (int i = 0; i < 3; i++) {
                                    playerCount = countPlayers2(screenCapture);
                                    if (playerCount == 0) {
                                        timesNA++;
                                    } else {
                                        timesNA = 0;
                                        break;
                                    }

                                }
                            } else {
                                timesNA = 0;
                            }
                        }
                    }



                } catch (TesseractException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(playerCount == 0) {
                    for(Map.Entry<Integer, JLabel> entry : damageLabels.entrySet()) {
                        entry.getValue().setText("N/A");
                    }
                    playing = false;
                    System.out.println("No players found.");
                }
                iter++;

                gameStage.setText("PLAYING");

                try {
//                    screenCapture = takeScreenShot();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for(ScreenPosition.PeriodPositions playerCategory : ScreenPosition.PeriodPositions.values()) {
                    if(!playerCategory.name().startsWith(convertNumberFrom(playerCount)))
                        continue;
                    for(ScreenPosition.PeriodPositions position : ScreenPosition.PeriodPositions.values()) {
                        int playerNum = convertNumber(position.name().split("_")[2]);
                        if(playerNum > playerCount) {
                            damageLabels.get(playerNum).setText("N/A!");
                        }

                        if(!position.name().split("_")[0].equals(playerCategory.name().split("_")[0]))
                            continue;
                        try {


                            BufferedImage cropped = getPlayerImage(position.getDamagePosition(), screenCapture);
                            int color = getAverageColor(cropped).getRGB();
                            int currentColor = playerDamageColors.getOrDefault(playerNum, 0);

                            damageLabels.get(playerNum).setText("P" + playerNum + ": " + timesHit.getOrDefault(playerNum, 0));


                            playerDamageColors.put(playerNum, color);


                            int[] pauseColors = {-3741976, -2753028, -2960407, -2425090, -67106};

                            for(int pauseColor : pauseColors) {
                                if(getDistanceBetweenColors(color, pauseColor) <= 0.05) {
                                    gameStage.setText("PAUSED");
                                    return;
                                }
                            }

                            if(isTimerPresent(screenCapture) > 0.05) {
                                System.out.println("Timer is not present");
                                return;
                            }

                            if(getDistanceBetweenColors(color, 3479317) <= 0.1 || getDistanceBetweenColors(color, -65794) <= 0.1) {
                                continue;
                            }



                            if(!playing)
                                continue;

                            if(Integer.toHexString(color).equals("000000"))
                                continue;

                            try {
                                //System.out.println(getDistanceBetweenColors(color, currentColor));
                                if (getDistanceBetweenColors(color, currentColor) >= 0.01) {

                                    if (damageCooldowns.containsKey(playerNum) && damageCooldowns.get(playerNum) > 0) {
                                        System.out.println("Player " + playerNum + " is on cooldown for " + damageCooldowns.get(playerNum) + " more iterations.");
                                        g2d.setColor(Color.BLUE);
                                        ScreenPosition.PlayerPositions playerPosition = ScreenPosition.PlayerPositions.valueOf(position.name());
                                        g2d.drawRect(playerPosition.getPlayerPosition().getX() - 100, playerPosition.getPlayerPosition().getY() - 100, playerPosition.getPlayerPosition().getWidth() + 100, playerPosition.getPlayerPosition().getHeight() + 100);
                                        continue;
                                    }
                                    if (getDistanceBetweenColors(color, currentColor) > 0.1) {
                                        System.out.println("DAMAGED: Player " + playerNum + " changed color from " + ("#" + Integer.toHexString(currentColor).substring(2)) + " to " + ("#" + Integer.toHexString(color).substring(2)) + " (Difference: " + getDistanceBetweenColors(color, currentColor) + ") (" + playerCount + " players)");
                                        arduinoController.sendChar(playerNum);
                                        damageCooldowns.put(playerNum, 10);

                                        timesHit.put(playerNum, timesHit.getOrDefault(playerNum, 0) + 1);
                                        //purple rgb
                                        Color red = new Color(28, 144, 36, 90);
                                        g2d.setColor(red);
                                        ScreenPosition.PlayerPositions playerPosition = ScreenPosition.PlayerPositions.valueOf(position.name());
                                        g2d.fillRect(playerPosition.getPlayerPosition().getX() - 200, playerPosition.getPlayerPosition().getY() - 200, playerPosition.getPlayerPosition().getWidth() + 200, playerPosition.getPlayerPosition().getHeight() + 200);
                                        //logImage(playerNum + "-" + color + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".png", screenCapture);
                                    } else {
                                        System.out.println("Player " + playerNum + " changed color from " + ("#" + Integer.toHexString(currentColor).substring(2)) + " to " + ("#" + Integer.toHexString(color).substring(2)) + " (Difference: " + getDistanceBetweenColors(color, currentColor) + ") (" + playerCount + " players)");
                                    }
                                }
                            } catch (StringIndexOutOfBoundsException e) {}


                            //timeTaken.setText(Math.max(playerDamageColors.getOrDefault(playerNum, 0), color) + "-" + Math.min(playerDamageColors.getOrDefault(playerNum, 0), color) + " = " + (Math.max(playerDamageColors.getOrDefault(playerNum, 0), color) - Math.min(playerDamageColors.getOrDefault(playerNum, 0), color)));


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        }, 0, INTERVAL);
    }

    //get the difference of two integers
    public static int getDifference(int a, int b) {
        return Math.max(a, b) - Math.min(a, b);
    }

    public static double isKillScreen(BufferedImage image) {
        int color = getAverageColor(image).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(11366240));
    }

    public static double isStageSelect(BufferedImage image) {
        int color = getAverageColor(image).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(7766661));
    }
    public static double isPauseScreen(BufferedImage image) throws IOException {
        int color = getAverageColor(getPlayerImage(new ScreenPosition(1370, 200, 550, 650), image)).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(1326657));
    }

    public static double isCSS(BufferedImage image) throws IOException {
        int color = getAverageColor(getPlayerImage(new ScreenPosition(100, 100, 1840-100, 550-100), image)).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(10452856));
    }

    public static double isGame(BufferedImage image) throws IOException {
        int color = getAverageColor(getPlayerImage(new ScreenPosition(430, 350, 1038, 171), image)).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(9745281));
    }

    public static double isCSSReady(BufferedImage image) throws IOException {
        int color = getAverageColor(getPlayerImage(new ScreenPosition(253, 176, 1305, 268), image)).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(7622429));
    }

    public static double isDeath(BufferedImage image) throws IOException {
        int color = getAverageColor(getPlayerImage(new ScreenPosition(906, 444, 107, 45), image)).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(16711421));
    }

    public static double isTimerPresent(BufferedImage image) throws IOException {
        int color = getAverageColor(getPlayerImage(new ScreenPosition(1808, 89, 2 , 2), image)).getRGB();
        return getDistanceBetweenColors(new Color(color), new Color(16776447));
    }

    public static double getDistanceBetweenColors(Color color1, Color color2) {
        int red1 = color1.getRed();
        int green1 = color1.getGreen();
        int blue1 = color1.getBlue();

        int red2 = color2.getRed();
        int green2 = color2.getGreen();
        int blue2 = color2.getBlue();

        double distance = Math.sqrt(Math.pow(red1 - red2, 2) + Math.pow(green1 - green2, 2) + Math.pow(blue1 - blue2, 2));
        return distance / Math.sqrt(Math.pow(255, 2) + Math.pow(255, 2) + Math.pow(255, 2));
    }

    public static double getDistanceBetweenColors(int color1, int color2) {
        return getDistanceBetweenColors(new Color(color1), new Color(color2));
    }

    public static Color getAverageColor(BufferedImage bi) {
        int step = 5;

        int sampled = 0;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                if (x % step == 0 && y % step == 0) {
                    Color pixel = new Color(bi.getRGB(x, y));
                    sumr += pixel.getRed();
                    sumg += pixel.getGreen();
                    sumb += pixel.getBlue();
                    sampled++;
                }
            }
        }
        return new Color(Math.round(sumr / sampled), Math.round(sumg / sampled), Math.round(sumb / sampled));
    }

    public static int convertNumber(String string) {
        switch(string.toLowerCase(Locale.ROOT)) {
            case "one":
                return 1;
            case "two":
                return 2;
            case "three":
                return 3;
            case "four":
                return 4;
        } //etc
        return 0;
    }

    public static String convertNumberFrom(int number) {
        switch(number) {
            case 1:
                return "ONE";
            case 2:
                return "TWO";
            case 3:
                return "THREE";
            case 4:
                return "FOUR";
        } //etc
        return "ZERO";
    }




    private static int countPlayers(BufferedImage bufferedImage) throws TesseractException, IOException {
        ScreenPosition.DamagePositions highestResults = null;
        int highestCount = 0;
        System.out.println("PLAYER COUNT TESTS");
        System.out.println(String.format("%-7s%6s%4s", "Count", "P#", "%"));
        outer: for(ScreenPosition.DamagePositions playerCategory : ScreenPosition.DamagePositions.values()) {
            AtomicInteger count = new AtomicInteger();
            if(!playerCategory.name().endsWith("ONE"))
                continue;
            ArrayList<BufferedImage> playerImages = new ArrayList<>();
            for(ScreenPosition.DamagePositions damagePosition : ScreenPosition.DamagePositions.values()) {
                if(!damagePosition.name().split("_")[0].equals(playerCategory.name().split("_")[0]))
                    continue;
                BufferedImage cropped = getPlayerImage(damagePosition.getDamagePosition(), bufferedImage);
                cropped = ImageProcessing.scale(cropped, 5, true);
                fill(cropped);
                playerImages.add(cropped);

            }

            long ocrStartTime = System.currentTimeMillis();
            playerImages.stream().parallel().forEach(playerImage -> {
                try {
                    Tesseract1 tesseract1 = new Tesseract1();
                    tesseract1.setPageSegMode(8);
                    tesseract1.setTessVariable("debug_file", "C:\\Temp\\tesseract.log");
                    tesseract1.setDatapath("./tessdata/");
                    String text = tesseract1.doOCR(playerImage).replaceAll("[^0-9]", "");
                    //String text = tesseract.doOCR(playerImage).replaceAll("[^0-9]", "");
                    if(!text.isEmpty()) {
                        for(int i = 0; i < text.length(); i++) {
                            count.getAndIncrement();
                        }
                    }
                } catch (TesseractException e) {
                    e.printStackTrace();
                }
            });
            long ocrEndTime = System.currentTimeMillis();
            System.out.println(String.format("%-7s%6s%4s", count.get(), playerCategory.name().split("_")[1], (ocrEndTime - ocrStartTime) + "ms"));

            if(count.get() > highestCount) {
                highestCount = count.get();
                highestResults = playerCategory;
            }
        }
        if(highestResults == null)
            return 0;
        switch(highestResults) {
            case TWO_PLAYERS_ONE:
                return 2;
            case THREE_PLAYERS_ONE:
                return 3;
            case FOUR_PLAYERS_ONE:
                return 4;
        }
        return 0;
    }

    public static int countPlayers2(BufferedImage bufferedImage) throws IOException {
        ScreenPosition.PlayerPositions highestResults = null;
        int highestCount = 0;
        int playerCount = 1;
        for(ScreenPosition.PlayerPositions playerCategory : ScreenPosition.PlayerPositions.values()) {
            int score = 0;

            //get the category
            if(!playerCategory.name().endsWith("ONE"))
                continue;

            playerCount++;

            //get the player images
            for(ScreenPosition.PlayerPositions playerPosition : ScreenPosition.PlayerPositions.values()) {
                if(!playerPosition.name().split("_")[0].equals(playerCategory.name().split("_")[0]))
                    continue;

                BufferedImage cropped = getPlayerImage(playerPosition.getPlayerPosition(), bufferedImage);
                //display(cropped);

                int color = getAverageColor(cropped).getRGB();

                double differenceFromPlayer = getDistanceBetweenColors(color, playerPosition.getColor());
                double differenceFromCPU= getDistanceBetweenColors(color, -5261384);

                if(differenceFromPlayer <= 0.15 || differenceFromCPU <= 0.1) {
                    score++;
                }
            }

            System.out.println(String.format("%-7s%6s%4s", score, playerCategory.name().split("_")[1], "N/A"));

            if(score > highestCount) {
                highestCount = score;
                highestResults = playerCategory;
            }
        }
        if(highestResults == null)
            return 0;
        switch(highestResults) {
            case TWO_PLAYERS_ONE:
                return 2;
            case THREE_PLAYERS_ONE:
                return 3;
            case FOUR_PLAYERS_ONE:
                return 4;
        }
        return 0;
    }

    private static BufferedImage getPlayerImage(ScreenPosition position, BufferedImage bufferedImage) throws IOException {
        return bufferedImage.getSubimage(position.getX(), position.getY(), position.getWidth(), position.getHeight());
    }


    public static void fill(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); ++x) {
            Fill.floodFill(image, x, 0, Color.white, Color.BLACK);
        }
    }

    public static void fillSide(BufferedImage image, int xPos, int yPos) {
        for (int x = xPos; x < image.getWidth(); ++x) {
            if(image.getRGB(x, yPos) != Color.black.getRGB()) {
                image.setRGB(x, yPos, Color.black.getRGB());
                fillVert(image, x, yPos);
            } else break;
        }
        for (int x = xPos - 1; x > 0; ++x) {
            if(image.getRGB(x, yPos) != Color.black.getRGB()) {
                image.setRGB(x, yPos, Color.black.getRGB());
                fillVert(image, x, yPos);
            } else break;
        }
    }

    public static void fillVert(BufferedImage image, int xPos, int yPos) {
        for (int y = yPos; y < image.getHeight(); ++y) {
            if(image.getRGB(xPos, y) != Color.black.getRGB()) {
                image.setRGB(xPos, y, Color.black.getRGB());
                fillSide(image, xPos, y);
            } else break;
        }
        for (int y = yPos - 1; y > 0; --y) {
            if(image.getRGB(xPos, y) != Color.black.getRGB()) {
                image.setRGB(xPos, y, Color.black.getRGB());
                fillSide(image, xPos, y);
            } else break;
        }
    }

}
