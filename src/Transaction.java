public class Transaction {
    private String sender;
    private String receiver;
    private int amount;

    public Transaction() {}

    public Transaction(String sender, String receiver, int amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getAmount() {
        return amount;
    }

    public Transaction copy() {
        return new Transaction(sender, receiver, amount);
    }

    public String toString() {
        return sender + ":" + receiver + "=" + amount;
    }
}