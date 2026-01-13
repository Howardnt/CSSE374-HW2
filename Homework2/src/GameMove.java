public abstract class GameMove {
    protected Player player;

    public GameMove(Player player) {
        this.player = player;
    }

    // Perform the action
    public abstract void execute();

    // Reverse the action (for replay "Previous" button)
    public abstract void undo();

    // Convert to string for saving to file
    public abstract String toLogString();
}