import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameLogger {
    private List<String> log = new ArrayList<>();

    public void addMove(String moveString) {
        log.add(moveString);
    }

    public void clear() {
        log.clear();
    }

    public void saveGame(String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (String line : log) {
                out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    public List<String> loadGame(String filename) {
        List<String> loadedMoves = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                loadedMoves.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading game: " + e.getMessage());
        }
        return loadedMoves;
    }
}
