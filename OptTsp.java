import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.io.PrintWriter;

public class OptTsp extends Thread {
    private int id;
    private long startTime;

    private int totalLen = 0;
    private int[][] graph;

    private ArrayList<Integer> tour = null;

    public int bestTourLength = -1;
    public ArrayList<Integer> bestTour;

    public OptTsp(int _id, int[][] _graph, long _startTime) {
        id = _id;
        graph = _graph;
        startTime = _startTime;
        totalLen = graph.length;
    }

    private int calcOpt() {
        int loc = 0;
        int length = 0;

        while (loc < totalLen) {
            int loc2 = loc + 1;
            int before_dist = graph[tour.get(loc)][tour.get(loc2)];

            int res = 0;

            while (loc2 < totalLen) {
                    int after_dist = graph[tour.get(loc)][tour.get(loc2)];
                    if (before_dist > after_dist) {
                        res = loc2;
                        before_dist = after_dist;
                    }
                loc2 += 1;
            }

            if (res != 0) {
                int tmp = tour.get(loc + 1);
                tour.set(loc + 1, tour.get(res));
                tour.set(res, tmp);
            }

            length += graph[tour.get(loc)][tour.get(loc + 1)];
            loc += 1;
        }

        return length;
    }


    public void run() {
        tour = new ArrayList<Integer>();
        bestTour = new ArrayList<Integer>();
        for (int i = 0; i < totalLen + 1; i++)
            bestTour.add(0);

        while (startTime + 28000 > System.currentTimeMillis()) {
            tour.clear();
            for (int i = 1; i < totalLen; i++)
                tour.add(i);
            Collections.shuffle(tour);
            tour.add(0, 0);
            tour.add(0);

            int len = calcOpt();
            if (len < bestTourLength || bestTourLength == -1) {
                bestTourLength = len;
                for (int i = 0; i < totalLen; i++) {
                    bestTour.set(i, tour.get(i));
                }
            }
        }
    }

    public static int[][] loadData(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i = 0;

        int[][] graph = new int[1000][1000];

        while ((line = buf.readLine()) != null) {
            String splitA[] = line.split(",");
            LinkedList<String> split = new LinkedList<String>();
            for (String s : splitA)
                if (!s.isEmpty())
                    split.add(s);

            int j = 0;

            for (String s : split)
                if (!s.isEmpty())
                    graph[i][j++] = Integer.parseInt(s)   ;

            i++;
        }
        return graph;
    }

    public static String tourToString(ArrayList<Integer> bestTour) {
        String t = new String();
        for (int i : bestTour)
            t = t + " " + i;
        return t;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("java OptTsp <input>");
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            int[][] graph = loadData(args[0]);
            int nbTh = 5;
            int bestId = -1;
            OptTsp[] tab = new OptTsp[nbTh];
            for (int i = 0; i < nbTh; i++){
                tab[i] = new OptTsp(i, graph, startTime);
                tab[i].start();
            }
            for (int i = 0; i < nbTh; i++){
                try {
                    tab[i].join();
                } catch (Exception e) {}
                if (bestId == -1 || tab[bestId].bestTourLength > tab[i].bestTourLength){
                    bestId = i;
                }
            }
            System.out.println("Best tour length: " + tab[bestId].bestTourLength);

            PrintWriter writer = new PrintWriter("out.txt", "UTF-8");
            for (int i : tab[bestId].bestTour)
                writer.println(i + 1);
            writer.close();

            System.out.println("Duration in ms: " + (System.currentTimeMillis() - startTime));

        } catch (IOException e) {
            System.err.println("Error reading graph.");
            return;
        }
    }
}