import com.sun.istack.internal.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BlockChain {
    private ArrayList<Block> chain = new ArrayList<>();
    private HashMap<String, Integer> balances = new HashMap<>();
    private static final String FIRST_HASH = "00000";

    // Exception handling done through the main program, this method does not interact with the user
    public static BlockChain fromFile(@NotNull String fileName) {
        if (fileName == null) throw new NullPointerException();

        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader buffer = new BufferedReader(reader);
            BlockChain chain = new BlockChain();
            String lastHash = FIRST_HASH;
            String line = "";

            while (true) {
                Block block = new Block();
                try {
                    for (int i = 0; i < 7; i++) {
                        line = buffer.readLine();

                        if (line == null) { // eof
                            buffer.close();
                            return chain;
                        }

                        block.feed(line);
                    }
                    block.feed(lastHash);
                    lastHash = line; //last line fed is the hash
                    chain.add(block);
                } catch (IOException e) {
                    System.err.println("IO exception of some form occurred!\n" + e.getMessage());
                    return null;
                }
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    // TODO: write toFile function
    public void toFile(@NotNull String fileName) {
        if (fileName == null) throw new NullPointerException();
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(dump());

            writer.close();
        }
        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
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
        chain.add(block);
    }

    public String dump() {
        StringBuffer buffer = new StringBuffer();

        for (Block block : chain) {
            String[] set = block.dump();
            for (String data : set) {
                buffer.append(data);
                buffer.append("\n");
            }
        }

        buffer.deleteCharAt(buffer.length()-1);
        return buffer.toString();
    }

    public Block[] getChain() {
        return chain.toArray(new Block[] {});
    }

    public boolean equals(BlockChain other) {
        Iterator<Block> blockIterator = chain.iterator();
        Iterator<Block> otherIterator = other.chain.iterator();

        while (blockIterator.hasNext()) {
            if (!otherIterator.hasNext()) {
                return false;
            }
            if (!blockIterator.next().equals(otherIterator.next())) {
                return false;
            }
        }

        return !otherIterator.hasNext();
    }


    public static void main(String[] args) {

        System.out.println(BlockChain.fromFile("bitcoinBank").equals(BlockChain.fromFile("bitcoinBank")));
        // TODO: Read blockchain from file
        // TODO: Validate blockchain
        // TODO: Get new transaction
        // TODO: Verify new transaction
        // TODO: add transaction to block
        // TODO: add block to blockchain
        // TODO: save blockchain to file

    }
}
