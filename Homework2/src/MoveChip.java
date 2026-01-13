import java.util.List;

public class MoveChip extends GameMove {
    private List<String> colors;

    public MoveChip(Player player, List<String> colors) {
        super(player);
        this.colors = colors;
    }

    @Override
    public void execute() {
        for (String c : colors) {
            player.pickChips(c, 1);
        }
    }

    @Override
    public void undo() {
        for (String c : colors) {
            player.removeChip(c, 1);
        }
    }

    @Override
    public String toLogString() {
        // Format: CHIP,PlayerID,Color1,Color2...
        return "CHIP," + player.id + "," + String.join(",", colors);
    }
}
