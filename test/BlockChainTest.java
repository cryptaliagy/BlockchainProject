import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BlockChainTest {

    static BlockChain blockChain;
    static String baseFile = "bitcoinBank";
    static String testFile = "testBank";


    @BeforeAll
    static void setup() {
        blockChain = BlockChain.fromFile(baseFile);
    }

    @Test
    void readFile() {
        StringBuffer buffer;
        try {
            FileReader reader = new FileReader(baseFile);
            buffer = new StringBuffer();

            while (reader.ready()) {
                buffer.append(Character.toString((char) reader.read()));
            }

            reader.close();

            assertEquals(buffer.toString(), blockChain.dump());

        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            fail();
        }
        Block[] blocks = blockChain.getChain();
        assertAll( () -> { assertEquals(3, blocks.length); },
                () -> {
                    for (int i = 0; i < blocks.length; i++) {
                        assertEquals(i, blocks[i].getIndex());
                    }
                },
                () -> {
                    String lastHash = "00000";
                    for (int i = 0; i < blocks.length; i++) {
                        assertEquals(lastHash, blocks[i].getPreviousHash());
                        lastHash = blocks[i].getHash();
                    }
                });

    }

    @Test
    void toFile() {
        blockChain.toFile(testFile);
        assertTrue(blockChain.equals(BlockChain.fromFile(baseFile)));
        assertTrue(blockChain.equals(BlockChain.fromFile(testFile)));

    }

    @Test
    void validateBlockchain() {
    }

    @Test
    void getBalance() {
        try {
            BufferedReader buffer = new BufferedReader(new FileReader("usernames"));
            assertTrue(blockChain.getBalance("bitcoin") < 0);
            String user = buffer.readLine();

            while (user != null) {
                assertTrue(blockChain.getBalance(user) >= 0);
                user = buffer.readLine();
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}