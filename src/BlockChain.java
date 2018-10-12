import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class BlockChain {
    private BlockChain blockchain;
    private ArrayList<Block> chain = new ArrayList<>();
    private static final String FIRST_HASH = "00000";

    // Exception handling done through the main program, this method does not interact with the user
    public static BlockChain fromFile(@NotNull String fileName) throws FileNotFoundException, NullPointerException {
        if (fileName == null) throw new NullPointerException();

        FileReader reader = new FileReader(fileName);
        BufferedReader buffer = new BufferedReader(reader);
        BlockChain chain = new BlockChain();
        boolean end = false;
        String lastHash = FIRST_HASH;
        String line = "";

        while (true) {
            Block block = new Block();
            for (int i = 0; i < 7; i++) {
                try {
                    line = buffer.readLine();
                } catch (IOException e) {
                    System.err.println("IO exception of some form occurred!\n" + e.getMessage());
                    return null;
                }

                if (line == null) {
                    end = true;
                    break;
                }

                block.feed(line);
            }
            if (end) break;
            block.feed(lastHash);
            lastHash = line; //last line fed is the hash
            chain.add(block);
        }


        return chain;
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


    public void add(Block block) {
        block.validate();
        chain.add(block);
    }


    public static void main(String[] args) {
        // TODO: Read blockchain from file
        // TODO: Validate blockchain
        // TODO: Get new transaction
        // TODO: Verify new transaction
        // TODO: add transaction to block
        // TODO: add block to blockchain
        // TODO: save blockchain to file

    }
}
