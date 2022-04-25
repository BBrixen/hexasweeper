package Models;

import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ScoreBoard {

    private static final int NUM_TOP_TIMES = 5;
    private static final String fileName = "scores.txt";
    private final Pair[] topTimes;

    public ScoreBoard() {
        topTimes = new Pair[NUM_TOP_TIMES];
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);

            int i = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.split(" ");
                double time = Double.parseDouble(words[0]);
                topTimes[i] = new Pair<>(time, line.substring(words[0].length()+1));
                i++;
            }
        } catch (FileNotFoundException ignored) {}

        for (int i = 0; i < topTimes.length; i++) {
            Pair time = topTimes[i];
            if (time == null) {
                topTimes[i] = new Pair<>(0.0, "No Entry");
            }
        }
    }

    public void addNewTime(double time, String difficulty) {
        boolean changed = false;
        System.out.println("time to change is: " + time);
        for (int i = 0; i < topTimes.length; i++) {
            Pair<Double, String> entry = topTimes[i];
            if (time < entry.getKey() || entry.getKey() == 0) {
                changed = true;
                double tempTime = entry.getKey();
                String tempDiff = entry.getValue();

                // swap the values
                topTimes[i] = new Pair<>(time, difficulty);
                time = tempTime;
                difficulty = tempDiff;
            }
        }

        if (changed) {
            saveTime();
        }
    }

    /**
     * This returns a mapping from the game difficulty to the time achieved
     * It still retuns the top times, it just returns the top times with each difficulty
     * @return - a mapping which includes the difficulty and time
     */
    public Pair<Double, String>[] getTopTimes() {
        return topTimes;
    }

    private void saveTime() {
        System.out.println("saving");
        try {
            FileWriter writer = new FileWriter(fileName);
            for (Pair<Double, String> time : topTimes) {
                writer.write(time.getKey() + " " + time.getValue() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            try {
                File file = new File(fileName);
                file.createNewFile();
                saveTime();
            } catch (IOException ignored) {}
        }
    }
}
