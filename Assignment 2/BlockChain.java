import com.sun.istack.internal.NotNull;

import java.io.*;
import java.util.*;

public class BlockChain {
    private ArrayList<Block> chain;
    private HashMap<String, Integer> balances;
    private Boolean isValid;
    private LinkedList<String> errorLogs = new LinkedList<>();

    private static final boolean STOP_LOAD_IF_INVALID = false;
    private static final String FIRST_HASH = "00000";

    public BlockChain() {
        chain = new ArrayList<>();
        balances = new HashMap<>();
        isValid = null;
    }


    public static BlockChain fromFile(String fileName) {
        if (fileName == null) throw new NullPointerException();

        try {
            BufferedReader buffer = new BufferedReader(new FileReader(fileName));
            return fromFile(buffer);
        } catch (FileNotFoundException e) {
            return new BlockChain();
        }
    }

    private static BlockChain fromFile(BufferedReader buffer) {
        BlockChain chain = new BlockChain();
        String lastHash = FIRST_HASH;
        int lastIndex = -1;
        long lastTime = -1;
        Block block;

        while (true) {
            try {
                String firstLine = buffer.readLine();
                if (firstLine == null) { // Empty blockchain if empty file
                    buffer.close();
                    if (chain.isValid != null && !chain.isValid) chain.chain = new ArrayList<>(); // flush arraylist if blockchain invalid
                    return chain;
                }


                // Any of these throwing an error means invalid blockchain
                int index = Integer.valueOf(firstLine);
                long timeStamp = Long.valueOf(buffer.readLine());
                String sender = buffer.readLine();
                String receiver = buffer.readLine();
                int amount = Integer.valueOf(buffer.readLine());
                String nonce = buffer.readLine();
                String hash = buffer.readLine();
                Transaction transaction = new Transaction(sender, receiver, amount);


                if (index != lastIndex + 1) {
                    chain.isValid = false;
                    chain.errorLogs.add("Index mismatch error at index " + index + "!\n" + ""
                            + "Expected: " + (lastIndex + 1) + "\n"
                            + "Received: " + index);
                }

                if (lastTime > timeStamp) {
                    chain.isValid = false;
                    chain.errorLogs.add("Timestamp value error at index " + index + "!\n"
                            + "Value cannot be less than: " + lastTime + "\n"
                            + "Received value: " + timeStamp);
                }

                if (amount < 1) {
                    chain.isValid = false;
                    chain.errorLogs.add("Transaction amount error at index " + index + "!\n"
                            + "Cannot send less than 1 bitcoin in a transaction!"
                            + "Received value: " + amount);
                }

                if (!chain.assertBalance(sender, amount)) {
                    chain.isValid = false;
                    chain.errorLogs.add("Insufficient funds error at index " + index + "!\n"
                            + "Sender \"" + sender + "\" requires funds of at least " + amount + " bitcoin\n"
                            + "Sender balance: " + chain.getBalance(sender) + " bitcoin");
                }

                if (nonce == null) {
                    chain.isValid = false;
                    chain.errorLogs.add("Null nonce error at index " + index + "!\n"
                            + "No nonce specified in BlockChain file; cannot validate block");
                }

                if (hash == null) {
                    chain.isValid = false;
                    chain.errorLogs.add("Null hash error at index " + index + "!\n"
                            + "No hash specified in BlockChain file; cannot validate block");
                }


                if (STOP_LOAD_IF_INVALID && chain.isValid != null && !chain.isValid) {
                    buffer.close();
                    chain.chain = new ArrayList<>(); // flush the arraylist of blocks
                    return chain;
                }

                block = new Block(index, timeStamp, transaction, nonce, lastHash, hash);

                if (!block.validate()) {
                    if (!hash.startsWith("00000")) {
                        chain.errorLogs.add("Block validation error at index " + index + "!\n"
                                + "Block hash must start with substring \"00000\"");
                    } else {
                        chain.errorLogs.add("Block validation error at index " + index + "!\n"
                                + "Expected block hash: " + Sha1.hash(block.toString()) + "\n"
                                + "Received block hash: " + hash);
                    }
                    if (STOP_LOAD_IF_INVALID) {
                        buffer.close();
                        chain.chain = new ArrayList<>(); //flush the arraylist of blocks
                        return chain; // If there is an invalid block, the whole chain is invalid
                    }
                }

                chain.add(block);
                lastHash = hash;
                lastIndex = index;
                lastTime = timeStamp;
            } catch (Exception e) { // invalid blockchain
                System.out.println(e.getMessage());
                return null;
            }
        }
    }

    public void toFile(@NotNull String fileName) throws FileNotFoundException {
        if (fileName == null) throw new NullPointerException();
        PrintWriter writer = new PrintWriter(fileName);

        for (Block block : chain) {
            writer.println(block.getIndex());
            writer.println(block.getTimeStamp());
            writer.println(block.getTransaction().getSender());
            writer.println(block.getTransaction().getReceiver());
            writer.println(block.getTransaction().getAmount());
            writer.println(block.getNonce());
            writer.println(block.getHash());
            writer.flush();
        }

        writer.close();
    }

    public boolean validateBlockchain() {
        if (isValid != null) {
            return isValid;
        }

        for (Block block : chain) {
            if (!block.validate()) {
                isValid = false;
            }
        }

        isValid = true;

        return isValid;

    }

    public int getBalance(String username) {
        if (balances.get(username) == null) {
            return 0; // User not in the hash table, doesn't have a balance
        }
        return balances.get(username);
    }


    public void add(Block block) {
        // Block should have been validated before this but it's always better safe than sorry
        if (!block.validate()) {
            return;
        }


        Transaction transaction = block.getTransaction();

        String sender = transaction.getSender();
        String receiver = transaction.getReceiver();
        int amount = transaction.getAmount();

        if (assertBalance(sender, amount)) {
            balances.put(sender, balances.getOrDefault(sender, 0) - amount);
        } else {
            return;
        }

        balances.put(receiver, balances.getOrDefault(receiver, 0) + amount);

        chain.add(block);
    }

    private boolean assertBalance(String username, int amount) {
        return balances.getOrDefault(username, 0) >= amount || username.equals("bitcoin");
    }

    public boolean equals(BlockChain other) {
        if (other == null) return false;

        if (chain.isEmpty()) {
            return other.chain.isEmpty();
        }
        Iterator<Block> otherIterator = other.chain.iterator();

        for (Block block : chain) {
            if (!otherIterator.hasNext()) {
                return false;
            }
            if (!block.equals(otherIterator.next())) {
                return false;
            }
        }

        return !otherIterator.hasNext();
    }

    public int getLastIndex() {
        return chain.size() - 1;
    }

    public String getLastHash() {
        return chain.get(getLastIndex()).getHash();
    }

    public static void main(String[] args) {
        String input = new String();
        Scanner scanner = new Scanner(System.in);

        System.out.println("BlockChain Program");
        System.out.println("==========================");
        System.out.println();

        while (!input.equalsIgnoreCase("yes")) {
            System.out.print("Would you like to read a file? ");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("no")) {
                System.out.println("Have a nice day");
                return;
            }
            if (!input.equalsIgnoreCase("yes")) {
                System.out.println("Please say yes or no");
            }
        }

        input = "";

        while (true) {
            String fileName;
            while (true) {
                System.out.print("What is the name of the file? ");
                fileName = scanner.nextLine();
                if (new File(fileName).exists()) {
                    System.out.println("\nReading BlockChain from file...");
                    break;
                }
                System.out.print("\nThis file does not exist, do you want to try again? If no, an empty BlockChain will be made ");
                input = scanner.nextLine();
                if (input.equalsIgnoreCase("no")) {
                    System.out.println("Making an empty BlockChain...");
                    break;
                }
            }

            BlockChain blockChain = BlockChain.fromFile(fileName);

            System.out.println("Ensuring BlockChain validity...\n");


            if (blockChain == null || !blockChain.validateBlockchain()) {
                System.out.println("BlockChain is not valid!\n\n");
                if (blockChain == null) {
                    System.out.println("Unknown parsing error occurred!");
                    System.out.println("Are you sure this file contains a blockchain?");
                } else {
                    System.out.println("Found the following errors: ");

                    for (String error : blockChain.errorLogs) {
                        System.out.println(error);
                    }
                }
                input = "";
                while (!input.equalsIgnoreCase("yes")) {
                    System.out.print("Do you want to try again? ");
                    input = scanner.nextLine();
                    if (input.equalsIgnoreCase("no")) {
                        System.out.println("Have a nice day");
                        return;
                    }
                    if (!input.equalsIgnoreCase("yes")) {
                        System.out.println("Please say yes or no");
                    }
                }

                input = "";

                System.out.println("Trying one more time...");
                continue;
            }

            System.out.println("BlockChain valid!");

            input = getMakeNewTransaction(scanner, blockChain);
            if (input == null) return;

            input = "";

            while (true) {
                System.out.println("Starting new transaction...");
                int amount;
                String sender;
                String receiver;
                while (true) {
                    try {
                        System.out.print("Sender: ");
                        sender = scanner.nextLine();
                        System.out.print("Receiver: ");
                        receiver = scanner.nextLine();
                        System.out.print("Amount: ");
                        amount = scanner.nextInt();
                        if (!blockChain.assertBalance(sender, amount)) {
                            System.out.println("Sender does not have enough bitcoin! Try again");
                            continue;
                        }
                        if (amount < 1) {
                            System.out.println("Must send at least 1 bitcoin!");
                            continue;
                        }
                        break;
                    } catch (Exception e) {
                        System.out.println("Value needs to be an integer!");
                    }
                }

                Transaction transaction = new Transaction(sender, receiver, amount);

                System.out.println("Creating new block for this transaction...");

                Block block = new Block(blockChain.getLastIndex() + 1, transaction, blockChain.getLastHash());

                System.out.println("Generating a valid nonce for this block...");

                if (block.validate()) {
                    System.out.println("Valid nonce found!");
                }

                System.out.println("Block nonce: " + block.getNonce());
                System.out.println("Block hash: " + block.getHash());
                System.out.println("Adding block to BlockChain...");
                blockChain.add(block);

                input = getMakeNewTransaction(scanner, blockChain);
                if (input == null) return;
            }

        }
    }

    private static String getMakeNewTransaction(Scanner scanner, BlockChain blockChain) {
        String input = "";
        while (!input.equalsIgnoreCase("yes")) {
            System.out.print("Would you like to add a new transaction to the BlockChain? ");
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("no")) {
                while (!input.equalsIgnoreCase("yes")) {
                    System.out.print("Write this chain to file? ");
                    input = scanner.nextLine();
                    if (input.equalsIgnoreCase("no")) {
                        System.out.println("Have a nice day");
                        return null;
                    }
                    if (!input.equalsIgnoreCase("yes")) {
                        System.out.println("Please say yes or no");
                    }
                }
                String fileName;
                while (true) {
                    System.out.print("What should be the file name? ");
                    fileName = scanner.nextLine();
                    System.out.print("Writing blockchain to file... ");
                    try {
                        blockChain.toFile(fileName);
                        System.out.println("File written!");
                        System.out.println("Have a nice day");
                        return null;
                    } catch (FileNotFoundException e) {
                        System.out.println("An error occurred, please try a different file name");
                    }
                }
            }
            if (!input.equalsIgnoreCase("yes")) {
                System.out.println("Please say yes or no");
            }
        }
        return "";
    }
}
