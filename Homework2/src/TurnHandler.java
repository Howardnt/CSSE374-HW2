package hw2ThreeLayerDesign;

import java.util.HashMap;
import java.util.Map;

public class TurnHandler {

	public int turn; // 1 for player1, 2 for player2
	public int moveNum; // 0 = action not done, 1 = action done
	public Map<String, Integer> cards; // Card data, can be ignored for now
	private String firstChip = null;
	private String secondChip = null;

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

		if (moveNum != 0) {
			return;
		}

		// First selection
		if (firstChip == null) {
			firstChip = color;
			return;
		}

		// Second selection
		if (secondChip == null) {
			secondChip = color;

			// Two of same color
			if (firstChip.equals(secondChip)) {
				player.pickChips(color, 2);
				moveNum = 1;
				return;
			}

			return;

		}

		// Third selection
		if (color.equals(firstChip) || color.equals(secondChip)) {
			firstChip = null;
			secondChip = null;
			throw new IllegalStateException("Invalid third chip. Must be unique. Selection reset.");
		}

		// Valid 3 unique chips
		player.pickChips(firstChip, 1);
		player.pickChips(secondChip, 1);
		player.pickChips(color, 1);

		moveNum = 1;
		return;
	}

	// Purchase card (stub)
	public boolean purchaseCard(Player player, String costString, int vpValue) {

		if (moveNum != 0) {
			return false;
		}

		Map<String, Integer> costMap = parseCost(costString);

		// Check affordability
		for (Map.Entry<String, Integer> entry : costMap.entrySet()) {
			int available = player.chips.getOrDefault(entry.getKey(), 0);
			if (available < entry.getValue()) {
				return false;
			}
		}

		// Pay cost
		for (Map.Entry<String, Integer> entry : costMap.entrySet()) {
			player.removeChip(entry.getKey(), entry.getValue());
		}

		// Gain VP
		player.vp += vpValue;

		moveNum = 1;
		return true;
	}

	private Map<String, Integer> parseCost(String cost) {
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
		case 'W':
			return "White";
		case 'R':
			return "Red";
		case 'B':
			return "Blue";
		case 'G':
			return "Green";
		case 'K':
			return "Black";
		default:
			throw new IllegalArgumentException("Unknown color: " + c);
		}
	}

	public int MoveNum() {
		return moveNum;
	}
	
	public boolean checkWin(Player player1, Player player2, CardData cardData) {
	    // Either all cards gone
	    if (cardData.getCardData().isEmpty()) return true;

	    // Or a player reaches 20 VP
	    if (player1.vp >= 20 || player2.vp >= 20) return true;

	    return false;
	}


	public Player getWinner(Player player1, Player player2) {
	    if (player1.vp > player2.vp) return player1;
	    else return player2;
	}
}
