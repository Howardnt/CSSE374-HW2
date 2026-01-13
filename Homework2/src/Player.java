
import java.util.HashMap;
import java.util.Map;

public class Player {

    public int id;
    public int vp;
    public Map<String, Integer> chips;

    public Player(int id) {
        this.id = id;
        this.vp = 0;
        this.chips = new HashMap<String, Integer>();
    }
    
    public void pickChips(String color, int amount) {
        chips.put(color, chips.getOrDefault(color, 0) + amount);
    }

    public void removeChip(String color, int amount) {
        chips.put(color, chips.get(color) - amount);
    }

    

    public void buyCard(String cardName, int cost) {
        if (chips.getOrDefault(cardName, 0) >= cost) {
            chips.put(cardName, chips.get(cardName) - cost);
            vp++;
        }
    }
}
