import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurnHandler {

	public int turn;
	public int moveNum;
	private String firstChip = null;
	private String secondChip = null;

	// NEW: Logger
	public GameLogger logger = new GameLogger();

	public TurnHandler() {
		turn = 1;
		moveNum = 0;
	}

	public void startTurn() {
		moveNum = 0;
		firstChip = null;
		secondChip = null;
	}

	public void changeTurn() {
		turn = (turn == 1) ? 2 : 1;
		startTurn();
	}

	public void handleChipSelection(Player player, String color) {
		if (moveNum != 0) return;

		if (firstChip == null) {
			firstChip = color;
			return;
		}

		if (secondChip == null) {
			secondChip = color;
			// Case 1: Two of same color
			if (firstChip.equals(secondChip)) {
				List<String> chips = new ArrayList<>();
				chips.add(color);
				chips.add(color);

				// Create Command, Execute, Log
				MoveChip move = new MoveChip(player, chips);
				move.execute();
				logger.addMove(move.toLogString());

				moveNum = 1;
				return;
			}
			return;
		}

		// Third selection check
		if (color.equals(firstChip) || color.equals(secondChip)) {
			firstChip = null;
			secondChip = null;
			throw new IllegalStateException("Invalid third chip. Must be unique. Selection reset.");
		}

		// Case 2: Three unique chips
		List<String> chips = new ArrayList<>();
		chips.add(firstChip);
		chips.add(secondChip);
		chips.add(color);

		// Create Command, Execute, Log
		MoveChip move = new MoveChip(player, chips);
		move.execute();
		logger.addMove(move.toLogString());

		moveNum = 1;
	}

	// UPDATED: Now requires CardData to pass to the Command
	public boolean purchaseCard(Player player, String costString, int vpValue, CardData cardData) {
		if (moveNum != 0) return false;

		Map<String, Integer> costMap = parseCost(costString);

		// Check affordability
		for (Map.Entry<String, Integer> entry : costMap.entrySet()) {
			int available = player.chips.getOrDefault(entry.getKey(), 0);
			if (available < entry.getValue()) {
				return false;
			}
		}

		// Create Command, Execute, Log
		MoveBuy move = new MoveBuy(player, costString, vpValue, costMap, cardData);
		move.execute();
		logger.addMove(move.toLogString());

		moveNum = 1;
		return true;
	}

	// Changed to public so ReplayController can use it
	public Map<String, Integer> parseCost(String cost) {
		Map<String, Integer> result = new HashMap<>();
		int i = 0;
		while (i < cost.length()) {
			String color = mapColor(cost.charAt(i));
			i++;
			StringBuilder num = new StringBuilder();
			while (i < cost.length() && Character.isDigit(cost.charAt(i))) {
				num.append(cost.charAt(i));
				i++;
			}
			int amount = Integer.parseInt(num.toString());
			result.put(color, amount);
		}
		return result;
	}

	private String mapColor(char c) {
		switch (c) {
			case 'W': return "White";
			case 'R': return "Red";
			case 'B': return "Blue";
			case 'G': return "Green";
			case 'K': return "Black";
			default: throw new IllegalArgumentException("Unknown color: " + c);
		}
	}

	public int MoveNum() { return moveNum; }

	public boolean checkWin(Player player1, Player player2, CardData cardData) {
		if (cardData.getCardData().isEmpty()) return true;
		if (player1.vp >= 20 || player2.vp >= 20) return true;
		return false;
	}

	public Player getWinner(Player player1, Player player2) {
		return (player1.vp > player2.vp) ? player1 : player2;
	}
}