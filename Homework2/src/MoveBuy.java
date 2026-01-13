import java.util.Map;

public class MoveBuy extends GameMove {
    private String costString;
    private int vpValue;
    private CardData cardData;
    private Map<String, Integer> costMap;

    public MoveBuy(Player player, String costString, int vpValue, Map<String, Integer> costMap, CardData cardData) {
        super(player);
        this.costString = costString;
        this.vpValue = vpValue;
        this.costMap = costMap;
        this.cardData = cardData;
    }

    @Override
    public void execute() {
        // Pay chips
        for (Map.Entry<String, Integer> entry : costMap.entrySet()) {
            player.removeChip(entry.getKey(), entry.getValue());
        }
        // Add VP
        player.vp += vpValue;
        // Remove card from data
        cardData.removeCurrentCard(costString);
    }

    @Override
    public void undo() {
        // Refund chips
        for (Map.Entry<String, Integer> entry : costMap.entrySet()) {
            player.pickChips(entry.getKey(), entry.getValue());
        }
        // Remove VP
        player.vp -= vpValue;
        // Put card back
        cardData.addCardBack(costString, vpValue);
    }

    @Override
    public String toLogString() {
        // Format: BUY,PlayerID,CostString,VP
        return "BUY," + player.id + "," + costString + "," + vpValue;
    }
}
