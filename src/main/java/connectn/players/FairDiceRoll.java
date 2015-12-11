package connectn.players;

public class FairDiceRoll extends Player {
    private int lastMove = 0;
    @Override
    public int makeMove() {
        return 4;
    }
}
