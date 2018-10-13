import java.sql.Timestamp;
import java.util.Iterator;

public class Block {
    private int index;
    private java.sql.Timestamp timestamp;
    private Transaction transaction = new Transaction();
    private String nonce;
    private String previousHash;
    private String hash;
    private byte line = 0;


    public Block() {}

    public Block(int index, Timestamp timestamp, Transaction transaction,
                 String nonce, String previousHash, String hash) {
        this.index = index;
        this.timestamp = timestamp;
        this.transaction = transaction;
        this.nonce = nonce;
        this.previousHash = previousHash;
        this.hash = hash;
    }

    public void feed(String input) {
        switch(line) {
            case 0:
                index = Integer.valueOf(input);
                break;
            case 1:
                timestamp = new Timestamp(Long.valueOf(input));
                break;
            case 2:
                transaction.setSender(input);
                break;
            case 3:
                transaction.setReceiver(input);
                break;
            case 4:
                transaction.setAmount(Integer.valueOf(input));
                break;
            case 5:
                nonce = input;
                break;
            case 6:
                hash = input;
                break;
            case 7:
                previousHash = input;
                break;
            default:
                break;
        }
        line++;
    }

    public String[] dump() {
        return new String[] {String.valueOf(index), String.valueOf(timestamp.getTime()),
                transaction.getSender(), transaction.getReceiver(), String.valueOf(transaction.getAmount()),
                nonce, hash };
    }

    // TODO: block validation logic

    public void validate() {

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

    public void registerTransaction(Transaction current) {
        if (transaction != null) {
            System.err.println("This block already has all the transactions it can hold!");
            return;
        }
        timestamp = new Timestamp(System.currentTimeMillis());
        this.transaction = current;
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

    public String getPreviousHash() {
        return previousHash;
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