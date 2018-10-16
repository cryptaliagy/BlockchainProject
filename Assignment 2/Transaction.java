import com.sun.istack.internal.NotNull;

public class Transaction {
    private String sender;
    private String receiver;
    private int amount;

    public Transaction() {}

    public Transaction(@NotNull String sender, @NotNull String receiver, int amount) {
        if (sender == null || receiver == null) throw new NullPointerException();

        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }
    public void setSender(@NotNull String sender) {
        if (sender == null) throw new NullPointerException();
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        if (receiver == null) throw new NullPointerException();
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