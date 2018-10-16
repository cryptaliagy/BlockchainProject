import org.junit.jupiter.api.*;


import static org.junit.jupiter.api.Assertions.*;

class BlockChainTest {

    static BlockChain baseFile;
    static BlockChain badName;
    static BlockChain insufficientFunds;
    static BlockChain negativeAmount;
    static BlockChain decreasingTime;
    static BlockChain incorrectIndicies;
    static BlockChain badFile;
    static BlockChain baseline = new BlockChain();


    @BeforeAll
    static void setup() {
        Block block = new Block(0, Long.valueOf("1231477200000"),
                new Transaction("bitcoin", "satoshi", 50),
                "XNm.c@@*X\\4Ff*=9GB2", "00000",
                "00000613d1aec0be473e97e50e2a9e9f9f3fd73c");
        baseline.add(block);

        block = new Block(1, Long.valueOf("1536150600000"),
                new Transaction("satoshi", "lucia", 25),
                "ZI4b]Cg+g2Tr`fn4EB", "00000613d1aec0be473e97e50e2a9e9f9f3fd73c",
                "0000062bfc086a10241b15741834644710e82e0b");
        baseline.add(block);

        block = new Block(2, Long.valueOf("1536166800000"),
                new Transaction("satoshi", "robert", 25),
                ":rR", "0000062bfc086a10241b15741834644710e82e0b",
                "0000000045d77dade8708c8df6ca5e167bc07688");
        baseline.add(block);
        baseFile = BlockChain.fromFile("bitcoinBank_baseFile.txt");
        badName = BlockChain.fromFile("bitcoinBank_badName.txt");
        insufficientFunds = BlockChain.fromFile("bitcoinBank_insufficientFunds.txt");
        negativeAmount = BlockChain.fromFile("bitcoinBank_negativeAmount.txt");
        decreasingTime = BlockChain.fromFile("bitcoinBank_decreasingTime.txt");
        badFile = BlockChain.fromFile("bitcoinBank_badFile.txt");
        incorrectIndicies = BlockChain.fromFile("bitcoinBank_incorrectIndicies.txt");
    }

    @Test
    void fromFile() {
        assertTrue(baseline.equals(baseFile));
        //assertNull(badName);
        assertNotNull(negativeAmount);
        assertNotNull(decreasingTime);
        assertNotNull(badFile);
        assertNotNull(insufficientFunds);
        assertNotNull(incorrectIndicies);
    }

    @Test
    void toFile() {
        BlockChain testing;
        try {
            baseline.toFile("bitcoinBank_baseLine_test");
            baseFile.toFile("bitcoinBank_baseFile_test");

            testing = BlockChain.fromFile("bitcoinBank_baseLine_test");
            assertTrue(baseline.equals(testing));

            testing = BlockChain.fromFile("bitcoinBank_baseFile_test");
            assertTrue(baseFile.equals(testing));

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void validateBlockchain() {
        assertTrue(baseline.validateBlockchain());
        assertTrue(baseFile.validateBlockchain());
        assertFalse(negativeAmount.validateBlockchain());
        assertFalse(decreasingTime.validateBlockchain());
        assertFalse(insufficientFunds.validateBlockchain());
        assertFalse(incorrectIndicies.validateBlockchain());
        assertFalse(badFile.validateBlockchain());
    }

    @Test
    void getBalance() {
        assertAll(
                () -> { assertEquals(0, baseline.getBalance("satoshi")); },
                () -> { assertEquals(25, baseline.getBalance("lucia")); },
                () -> { assertEquals(25, baseline.getBalance("robert")); },
                () -> { assertEquals(-50, baseline.getBalance("bitcoin")); });
    }

    @AfterAll
    static void takedown() {
        baseline = null;
        baseFile = null;
        badName = null;
        insufficientFunds = null;
        negativeAmount = null;
        decreasingTime = null;
        badFile = null;
        incorrectIndicies = null;
    }
}