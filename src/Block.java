public class Block {
    private int index;
    private java.sql.Timestamp timestamp;
    private Transaction transaction;
    private String nonce;
    private String previousHash;
    private String hash;


    private String makeNonce(int nonceValue) {
        if (nonceValue == 0) return "!"; // String value for ASCII value of 33
        String nonce = "";
        int digit;
        while (nonceValue > 0) {
            digit = nonceValue % 94;
            nonceValue /= 94;
            nonce = Character.toString((char) (digit + 33)) + nonce;
        }

        return nonce;
    }

    public void registerTransaction(Transaction current) {
        timestamp = new java.sql.Timestamp(System.currentTimeMillis());

    }

    public String toString() {
        return timestamp.toString() + ":" + transaction.toString() + "." + nonce + previousHash;
    }
}