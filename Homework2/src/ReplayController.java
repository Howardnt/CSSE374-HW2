import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import java.util.Map;
import java.util.HashMap;

public class ReplayController {
    private GameManager gameManager;
    private List<GameMove> moveHistory;
    private int currentMoveIndex = -1;
    private Timer autoPlayTimer;
    private TurnHandler turnHandlerStub; // For parsing logic only

    public ReplayController(GameManager gm) {
        this.gameManager = gm;
        this.moveHistory = new ArrayList<>();
        this.turnHandlerStub = new TurnHandler(); // Used for parsing costs

        // Auto-play timer: Executes a move every 1.5 seconds
        autoPlayTimer = new Timer(1500, e -> stepForward());
    }

    public void loadReplay(String filename, Player p1, Player p2, CardData cardData) {
        GameLogger logger = new GameLogger();
        List<String> lines = logger.loadGame(filename);
        moveHistory.clear();
        currentMoveIndex = -1;

        for (String line : lines) {
            String[] parts = line.split(",");
            String type = parts[0];
            int pid = Integer.parseInt(parts[1]);
            Player p = (pid == 1) ? p1 : p2;

            if (type.equals("CHIP")) {
                List<String> colors = new ArrayList<>();
                for (int i = 2; i < parts.length; i++) {
                    colors.add(parts[i]);
                }
                moveHistory.add(new MoveChip(p, colors));
            } else if (type.equals("BUY")) {
                String costString = parts[2];
                int vp = Integer.parseInt(parts[3]);
                Map<String, Integer> costMap = turnHandlerStub.parseCost(costString);
                moveHistory.add(new MoveBuy(p, costString, vp, costMap, cardData));
            }
        }
    }

    public void stepForward() {
        if (currentMoveIndex < moveHistory.size() - 1) {
            currentMoveIndex++;
            GameMove move = moveHistory.get(currentMoveIndex);
            move.execute();
            gameManager.updateGUI();
        } else {
            autoPlayTimer.stop();
        }
    }

    public void stepBackward() {
        if (currentMoveIndex >= 0) {
            GameMove move = moveHistory.get(currentMoveIndex);
            move.undo();
            currentMoveIndex--;
            gameManager.updateGUI();
        }
    }

    public void toggleAutoPlay() {
        if (autoPlayTimer.isRunning()) {
            autoPlayTimer.stop();
        } else {
            autoPlayTimer.start();
        }
    }
}
