import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CardData {

    private Map<String, Integer> cards = new HashMap<>();

    public void gatherCardsFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null && cards.size() < 15) {
                String[] parts = line.split(",");
                String cost = parts[0];
                int value = Integer.parseInt(parts[1]);
                cards.put(cost, value);
            }
        } catch (IOException e) {
            System.out.println("Error reading card data: " + e);
        }
    }

    public Map<String, Integer> getCardData() {
        return cards;
    }

    public void removeCurrentCard(String costString) {
        cards.remove(costString);
    }

    // NEW: For Replay/Undo
    public void addCardBack(String costString, int vp) {
        cards.put(costString, vp);
    }

    public void clearCards() {
        if (cards != null) {
            cards.clear();
        } else {
            cards = new HashMap<>();
        }
    }
}