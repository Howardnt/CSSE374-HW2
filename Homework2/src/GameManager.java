import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class GameManager {

    public JFrame gameWindow;
    private CardData cardData;
    private Player player1;
    private Player player2;
    private TurnHandler turnHandler;
    private ReplayController replayController; // New

    private final String[] colors = {"Red", "Blue", "Green", "Black", "White"};

    // GUI Components
    private JPanel cardPanel;
    private JPanel infoPanel;
    private JPanel chipPanel;
    private JPanel replayPanel; // New

    private Map<String, JLabel> player1ChipLabels = new HashMap<>();
    private Map<String, JLabel> player2ChipLabels = new HashMap<>();
    private JLabel player1VPLabel;
    private JLabel player2VPLabel;
    private JLabel turnLabel;
    private Map<String, JButton> cardButtons = new HashMap<>();
    private Map<String, JButton> chipButtonsMap = new HashMap<>();

    public GameManager() {
        gameWindow = new JFrame("Mini Splendor");
        cardData = new CardData();
        player1 = new Player(1);
        player2 = new Player(2);
        turnHandler = new TurnHandler();
        replayController = new ReplayController(this); // New

        // Check file path per your environment (e.g. "Homework2/cards.txt")
        cardData.gatherCardsFromFile("cards.txt");
    }

    public void initialize() {
        gameWindow.setSize(1000, 750);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setLayout(new BorderLayout());

        createCardPanel();
        createInfoPanel();
        createChipPanel();
        createReplayPanel(); // New

        gameWindow.add(cardPanel, BorderLayout.CENTER);
        gameWindow.add(infoPanel, BorderLayout.EAST);
        gameWindow.add(chipPanel, BorderLayout.SOUTH);
        // Replay panel is initially hidden or added at top
        gameWindow.add(replayPanel, BorderLayout.NORTH);
        replayPanel.setVisible(false);

        updateGUI();
        gameWindow.setVisible(true);
    }

    // -------------------- CREATE PANELS --------------------
    private void createCardPanel() {
        cardPanel = new JPanel(new GridLayout(3, 5, 10, 10));
        cardPanel.setBorder(BorderFactory.createTitledBorder("Cards"));
        refreshCardButtons();
    }

    // Helper to redraw cards completely (needed for Replay)
    private void refreshCardButtons() {
        cardPanel.removeAll();
        cardButtons.clear();
        Map<String, Integer> cards = cardData.getCardData();
        for (Map.Entry<String, Integer> entry : cards.entrySet()) {
            JButton card = createCardButton(entry.getKey(), entry.getValue());
            cardPanel.add(card);
            cardButtons.put(entry.getKey(), card);
        }
        cardPanel.revalidate();
        cardPanel.repaint();
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

        // NEW: Load Replay Button
        JButton loadReplay = new JButton("Load Replay");
        loadReplay.addActionListener(e -> startReplayMode());
        infoPanel.add(loadReplay);
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

    // NEW: Replay Control Panel
    private void createReplayPanel() {
        replayPanel = new JPanel();
        replayPanel.setBorder(BorderFactory.createTitledBorder("Replay Controls"));

        JButton btnPrev = new JButton("<< Prev");
        JButton btnAuto = new JButton("Play/Pause");
        JButton btnNext = new JButton("Next >>");
        JButton btnExit = new JButton("Exit Replay");

        btnPrev.addActionListener(e -> replayController.stepBackward());
        btnAuto.addActionListener(e -> replayController.toggleAutoPlay());
        btnNext.addActionListener(e -> replayController.stepForward());
        btnExit.addActionListener(e -> resetGame()); // Exit goes back to new game

        replayPanel.add(btnPrev);
        replayPanel.add(btnAuto);
        replayPanel.add(btnNext);
        replayPanel.add(btnExit);
    }

    // -------------------- GUI UPDATE --------------------
    public void updateGUI() {
        player1VPLabel.setText("VP: " + player1.vp);
        player2VPLabel.setText("VP: " + player2.vp);

        for (String color : colors) {
            player1ChipLabels.get(color).setText(color + ": " + player1.chips.getOrDefault(color, 0));
            player2ChipLabels.get(color).setText(color + ": " + player2.chips.getOrDefault(color, 0));
        }

        turnLabel.setText("Turn: Player " + turnHandler.turn);

        // SYNC CARDS: Instead of just removing, we refresh based on data
        // because Undo might have put a card back.
        refreshCardButtons();

        // Remove buttons for cards that are NOT in data
        // (refreshCardButtons handles this by recreating from data)
    }

    // -------------------- HANDLERS --------------------
    private void handleChipClick(String color) {
        if (replayPanel.isVisible()) return; // Disable input during replay

        Player current = (turnHandler.turn == 1) ? player1 : player2;
        try {
            turnHandler.handleChipSelection(current, color);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(gameWindow, ex.getMessage());
            turnHandler.startTurn();
        }

        if (turnHandler.MoveNum() == 1) {
            turnHandler.changeTurn();
        }
        updateGUI();
    }

    private void handleCardPurchase(String costString, int vp) {
        if (replayPanel.isVisible()) return; // Disable input during replay

        Player current = (turnHandler.turn == 1) ? player1 : player2;

        // Updated to pass cardData
        boolean bought = turnHandler.purchaseCard(current, costString, vp, cardData);

        if (!bought) {
            JOptionPane.showMessageDialog(gameWindow, "Cannot buy this card.");
            return;
        }

        // Save game state check
        if (turnHandler.checkWin(player1, player2, cardData)) {
            saveGameWithRotation();

            Player winner = turnHandler.getWinner(player1, player2);
            JOptionPane.showMessageDialog(gameWindow, "Game Over! Winner: " + winner.id + "\nGame saved to history.");
            resetGame();
            return;
        }

        turnHandler.changeTurn();
        updateGUI();
    }

    private void startReplayMode() {
        // 1. Find all saved game files
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) -> name.startsWith("Game_") && name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(gameWindow, "No saved games found!");
            return;
        }

        // 2. Sort them new -> old for the user
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        // 3. Create a list of names for the dropdown
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }

        // 4. Show Selection Dialog
        String selectedFile = (String) JOptionPane.showInputDialog(
                gameWindow,
                "Select a game to replay:",
                "Load Replay",
                JOptionPane.QUESTION_MESSAGE,
                null,
                fileNames,
                fileNames[0] // Default to the newest
        );

        // 5. If user picked a file, load it
        if (selectedFile != null) {
            // Reset everything first
            resetGameInternal();

            // Switch UI
            chipPanel.setVisible(false);
            replayPanel.setVisible(true);

            // Load data
            replayController.loadReplay(selectedFile, player1, player2, cardData);
            updateGUI();
        }
    }

    public void resetGame() {
        resetGameInternal();
        chipPanel.setVisible(true);
        replayPanel.setVisible(false);
        updateGUI();
    }

    private void resetGameInternal() {
        player1 = new Player(1);
        player2 = new Player(2);
        turnHandler = new TurnHandler();
        cardData.clearCards();
        cardData.gatherCardsFromFile("cards.txt");
        // Ensure replay controller points to new objects if necessary,
        // but here we just pass them in loadReplay.
    }

    private void saveGameWithRotation() {
        // 1. Generate Timestamp Filename
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = "Game_" + timestamp + ".txt";

        // 2. Save the current game
        turnHandler.logger.saveGame(filename);

        // 3. Manage the 20-file limit
        File dir = new File(".");
        // Filter for files starting with "Game_" and ending in ".txt"
        File[] files = dir.listFiles((d, name) -> name.startsWith("Game_") && name.endsWith(".txt"));

        if (files != null && files.length > 20) {
            // Sort by last modified (Oldest first)
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));

            // Delete the excess old files
            int filesToDelete = files.length - 20;
            for (int i = 0; i < filesToDelete; i++) {
                files[i].delete();
            }
        }
    }
}