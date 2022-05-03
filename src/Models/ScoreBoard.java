package Models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class ScoreBoard {

    private static final int NUM_TOP_TIMES = 5;
    private static final String fileName = "scores.txt";
    private static HashMap<String, Double[]> topTimes;

    public ScoreBoard() {
        // create the top times
        topTimes = new HashMap<>();
        topTimes.put("Very Easy", new Double[NUM_TOP_TIMES]);
        topTimes.put("Easy", new Double[NUM_TOP_TIMES]);
        topTimes.put("Normal", new Double[NUM_TOP_TIMES]);
        topTimes.put("Hard", new Double[NUM_TOP_TIMES]);
        topTimes.put("Very Hard", new Double[NUM_TOP_TIMES]);

        // populate top times with saves from file
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.split(" ");
                
                // add to the hashmap
                double time = Double.parseDouble(words[0]);
                String difficulty =  line.substring(words[0].length()+1);
                this.addNewTime(time, difficulty, false); // we dont want to save since we are reading
            }
        } catch (FileNotFoundException ignored) {}
    }

    public void addNewTime(double time, String difficulty, boolean saving) {
        boolean changed = false;
        System.out.println("time to change is: " + time);

        Double[] times = topTimes.get(difficulty);

        for (int i = 0; i < NUM_TOP_TIMES; i++) {
            if (times[i] == null)
                times[i] = 0.0;

            if (times[i] == 0.0 || time < times[i]) {
                changed = true;
                double tempTime = times[i];

                // swap the values
                times[i] = time;
                time = tempTime;
            }
        }

        // edit and possibly save the top times
        topTimes.put(difficulty, times);
        if (saving && changed)
            saveTime();
    }

    /**
     * This returns a mapping from the game difficulty to the time achieved
     * It still retuns the top times, it just returns the top times with each difficulty
     * @return - a mapping which includes the difficulty and time
     */
    public Double[] getTopTimes(String difficulty) {
        return topTimes.get(difficulty);
    }

    /**
     * This saves the times stored in the scoreboard into a text file
     * We call this when the user generates a new top time that we want to save
     */
    private void saveTime() {
        try {
            FileWriter writer = new FileWriter(fileName);
            for (String difficulty : topTimes.keySet()) {
                for (Double time : topTimes.get(difficulty)) {
                	if (time != null && time != 0.0) {
                        writer.write(time + " " + difficulty + "\n");
                	}
                }
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
