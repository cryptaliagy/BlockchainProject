import java.sql.Timestamp;

public class Block {
    private int index;
    private java.sql.Timestamp timestamp;
    private Transaction transaction;
    private String nonce;
    private String previousHash;
    private String hash;
    private Boolean isValid = null;


    public Block(int index, Transaction transaction, String previousHash) {
        this.index = index;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.transaction = transaction;
        this.previousHash = previousHash;
    }

    public Block(int index, long timestamp, Transaction transaction,
                 String nonce, String previousHash, String hash) {
        this.index = index;
        this.timestamp = new Timestamp(timestamp);
        this.transaction = transaction;
        this.nonce = nonce;
        this.previousHash = previousHash;
        this.hash = hash;
    }

    public boolean validate(String criteria) {
        if (isValid != null) {
            return isValid;
        }
        if (hash != null) {
            try {
                isValid = hash.equals(Sha1.hash(toString())) && hash.startsWith(criteria);
            } catch (Exception e) {
                isValid = false;
            }
        }
        else {
            try {
                int nonceVal = 0;
                while (true) {
                    nonce = makeNonce(nonceVal);
                    hash = Sha1.hash(toString());
                    if (hash.startsWith(criteria)) {
                        isValid = true;
                        break;
                    }
                    nonceVal++;
                }

            } catch (Exception e) {
                isValid = false;
            }
        }

        return isValid;

    }

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

    public int getIndex() {
        return index;
    }

    public long getTimeStamp() {
        return timestamp.getTime();
    }

    public Transaction getTransaction() {
        return transaction.copy();
    }

    public String getNonce() {
        return nonce;
    }

    public String getHash() {
        return hash;
    }

    public boolean equals(Block other) {
        return toString().equals(other.toString());
    }

    public String toString() {
        return timestamp.toString() + ":" + transaction.toString() + "." + nonce + previousHash;
    }
}