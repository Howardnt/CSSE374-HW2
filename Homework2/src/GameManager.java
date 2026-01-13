package hw2ThreeLayerDesign;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GameManager {

    public JFrame gameWindow;

    private CardData cardData;
    private Player player1;
    private Player player2;
    private TurnHandler turnHandler;

    private final String[] colors = {"Red", "Blue", "Green", "Black", "White"};

    // Persistent GUI components
    private JPanel cardPanel;
    private JPanel infoPanel;
    private JPanel chipPanel;

    // Player info labels
    private Map<String, JLabel> player1ChipLabels = new HashMap<>();
    private Map<String, JLabel> player2ChipLabels = new HashMap<>();
    private JLabel player1VPLabel;
    private JLabel player2VPLabel;
    private JLabel turnLabel;

    // Card buttons mapped by cost string
    private Map<String, JButton> cardButtons = new HashMap<>();

    // Chip buttons for highlighting
    private Map<String, JButton> chipButtonsMap = new HashMap<>();

    public GameManager() {
        gameWindow = new JFrame("Mini Splendor");
        cardData = new CardData();
        player1 = new Player(1);
        player2 = new Player(2);
        turnHandler = new TurnHandler();

        cardData.gatherCardsFromFile("cards.txt");
    }

    public void initialize() {
        gameWindow.setSize(1000, 700);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setLayout(new BorderLayout());

        createCardPanel();
        createInfoPanel();
        createChipPanel();

        gameWindow.add(cardPanel, BorderLayout.CENTER);
        gameWindow.add(infoPanel, BorderLayout.EAST);
        gameWindow.add(chipPanel, BorderLayout.SOUTH);

        updateGUI();
        gameWindow.setVisible(true);
    }

    // -------------------- CREATE PANELS --------------------
    private void createCardPanel() {
        cardPanel = new JPanel(new GridLayout(3, 5, 10, 10));
        cardPanel.setBorder(BorderFactory.createTitledBorder("Cards"));

        Map<String, Integer> cards = cardData.getCardData();
        for (Map.Entry<String, Integer> entry : cards.entrySet()) {
            String costString = entry.getKey();
            int vp = entry.getValue();
            JButton card = createCardButton(costString, vp);
            cardPanel.add(card);
            cardButtons.put(costString, card);
        }
    }

    private JButton createCardButton(String costString, int vp) {
        JButton card = new JButton();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(new JLabel("Cost: " + costString));
        card.add(new JLabel("VP: " + vp));

        card.addActionListener(e -> handleCardPurchase(costString, vp));
        return card;
    }

    private void createInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Player Info"));
        infoPanel.setPreferredSize(new Dimension(200, 0));

        infoPanel.add(new JLabel("Player 1"));
        player1VPLabel = new JLabel();
        infoPanel.add(player1VPLabel);
        for (String color : colors) {
            JLabel label = new JLabel();
            player1ChipLabels.put(color, label);
            infoPanel.add(label);
        }

        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("Player 2"));
        player2VPLabel = new JLabel();
        infoPanel.add(player2VPLabel);
        for (String color : colors) {
            JLabel label = new JLabel();
            player2ChipLabels.put(color, label);
            infoPanel.add(label);
        }

        turnLabel = new JLabel();
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(turnLabel);

        JButton newGame = new JButton("New Game");
        newGame.addActionListener(e -> resetGame());
        infoPanel.add(newGame);
    }

    private void createChipPanel() {
        chipPanel = new JPanel(new GridLayout(1, colors.length, 10, 10));
        chipPanel.setBorder(BorderFactory.createTitledBorder("Take Chips"));

        for (String color : colors) {
            JButton button = new JButton(color);
            chipButtonsMap.put(color, button);
            button.addActionListener(e -> handleChipClick(color));
            chipPanel.add(button);
        }
    }

    // -------------------- GUI UPDATE --------------------
    private void updateGUI() {
        // Update VP labels
        player1VPLabel.setText("VP: " + player1.vp);
        player2VPLabel.setText("VP: " + player2.vp);

        // Update chip labels
        for (String color : colors) {
            player1ChipLabels.get(color).setText(color + ": " + player1.chips.getOrDefault(color, 0));
            player2ChipLabels.get(color).setText(color + ": " + player2.chips.getOrDefault(color, 0));
        }

        // Update turn label
        turnLabel.setText("Turn: Player " + turnHandler.turn);

        // Remove card buttons for cards bought
        cardButtons.keySet().removeIf(costString -> {
            if (!cardData.getCardData().containsKey(costString)) {
                JButton btn = cardButtons.get(costString);
                cardPanel.remove(btn);
                return true;
            }
            return false;
        });

        cardPanel.revalidate();
        cardPanel.repaint();
        infoPanel.revalidate();
        infoPanel.repaint();

       
    }


    // -------------------- HANDLERS --------------------
    private void handleChipClick(String color) {
        Player current = (turnHandler.turn == 1) ? player1 : player2;

        try {
            turnHandler.handleChipSelection(current, color);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(gameWindow, ex.getMessage());
            turnHandler.startTurn(); // reset selection
        }

        if (turnHandler.MoveNum() == 1) {
            turnHandler.changeTurn();
        }

        updateGUI();
    }

    private void handleCardPurchase(String costString, int vp) {
        Player current = (turnHandler.turn == 1) ? player1 : player2;
        boolean bought = turnHandler.purchaseCard(current, costString, vp);

        if (!bought) {
            JOptionPane.showMessageDialog(gameWindow, "Cannot buy this card.");
            return;
        }

     // Remove card from board
        cardData.removeCurrentCard(costString);
        cardPanel.remove(cardButtons.get(costString));
        cardButtons.remove(costString);

        

        // Check for win
        if (turnHandler.checkWin(player1, player2, cardData)) {
            Player winner = turnHandler.getWinner(player1, player2);
            String msg = (winner != null)
                    ? "Game over! Player " + winner.id + " wins!"
                    : "Game over! It's a tie!";
            JOptionPane.showMessageDialog(gameWindow, msg);
            resetGame();
            return;
        }

        turnHandler.changeTurn();
        updateGUI();
    }

    private void resetGame() {
        // Reset domain objects
        player1 = new Player(1);
        player2 = new Player(2);
        turnHandler = new TurnHandler();

        // Reset card data
        cardData.clearCards();
        cardData.gatherCardsFromFile("cards.txt");

        // Clear the JFrame
        gameWindow.getContentPane().removeAll();

        // Clear GUI maps
        cardButtons.clear();
        chipButtonsMap.clear();
        player1ChipLabels.clear();
        player2ChipLabels.clear();

        // Recreate all panels
        createCardPanel();
        createInfoPanel();
        createChipPanel();

        // Add panels back to JFrame
        gameWindow.add(cardPanel, BorderLayout.CENTER);
        gameWindow.add(infoPanel, BorderLayout.EAST);
        gameWindow.add(chipPanel, BorderLayout.SOUTH);

        // Update GUI to show fresh data
        updateGUI();
    }

}
