import java.util.ArrayList;

public class BlockChain {
    private BlockChain blockchain;
    private ArrayList<Block> chain;

    // TODO: write fromFile function
    public static BlockChain fromFile(String fileName) {
        return null;
    }

    // TODO: write toFile function
    public void toFile(String fileName) { // shouldn't I pass a blockchain to be written?
    }

    // TODO: write validateBlockchain function
    public boolean validateBlockchain() {
        return false;
    }

    // TODO: write getBalance function
    public int getBalance(String username) {
        return 0;
    }

    // TODO: write add function
    public void add(Block block) {
    }


    public static void main(String[] args) {
        // Read blockchain from file
        BlockChain blockChain = fromFile(args[0]);

        // Validates blockchain
        if (!blockChain.validateBlockchain()) {
            // TODO: better error handling
            throw new IllegalStateException("Invalid blockchain");
        }

        // TODO: Get new transaction
        // TODO: Verify new transaction
        // TODO: add transaction to block
        // TODO: add block to blockchain
        // TODO: save blockchain to file

    }
}
