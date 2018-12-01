import java.io.*;
import java.util.*;

public class BlockChain {
    private ArrayList<Block> chain = new ArrayList<>();
    private HashMap<String, Integer> balances = new HashMap<>();
    private Boolean isValid = null;
    private LinkedList<String> errorLogs = new LinkedList<>();

    private static Properties properties = new Properties();

    private static boolean STOP_LOAD_IF_INVALID;
    private static String FIRST_HASH;
    private static String CRITERIA;
    private static String MINER_ID;
    private static String GREETING;

    private static void config() {
        InputStream reader = null;

        try {
            reader = new FileInputStream("blockchain.config");
            properties.load(reader);
            STOP_LOAD_IF_INVALID = Boolean.valueOf(properties.getProperty("stop_load"));
            FIRST_HASH = properties.getProperty("first_hash");
            CRITERIA = properties.getProperty("criteria");
            MINER_ID = properties.getProperty("miner_id");
            GREETING = "\nHave a nice day!";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String appendToFileName(String fileName, String toAppend) {
        if (fileName == null || toAppend == null) throw new NullPointerException();

        return fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                + toAppend + fileName.substring(fileName.lastIndexOf("."));
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
                    if (chain.isValid != null && !chain.isValid)
                        chain.chain = new ArrayList<>(); // flush arraylist if blockchain invalid
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

                if (!block.validate(CRITERIA)) {
                    if (!hash.startsWith(CRITERIA)) {
                        chain.errorLogs.add("Block validation error at index " + index + "!\n"
                                + "Block hash must start with substring \"" + CRITERIA + "\"");
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
                lastHash = Sha1.hash(block.toString()); // make sure the hash is the actual, not expected
                lastIndex = index;
                lastTime = timeStamp;
            } catch (Exception e) { // invalid blockchain
                chain.errorLogs.add("Unknown parsing error occurred!\nAre you sure this file contains a blockchain?");
                chain.chain = null;
                chain.isValid = false;
                return chain;
            }
        }
    }

    public void toFile(String fileName) {
        if (fileName == null) throw new NullPointerException();
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(fileName);

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void errorsToFile(String fileName) {
        if (fileName == null) throw new NullPointerException();

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(appendToFileName(fileName, "error_log"));

            for (String error : errorLogs) {
                writer.println(error);
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public boolean validateBlockchain() {
        if (isValid != null) {
            return isValid;
        }

        for (Block block : chain) {
            if (!block.validate(CRITERIA)) {
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
        if (!block.validate(CRITERIA)) {
            isValid = false;
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

    public static void main(String[] args) {
        config();
        int select;
        boolean end = false;
        String fileName = "";
        Scanner scanner = new Scanner(System.in);
        String[] options = new String[]{"Validate file with miner ID", "Read file and write new transactions",
                "Get BlockChain Stats"};

        makeMenu("BlockChain Program", options);


        select = getUserNumSelection(":", 1, 3);

        if (select == 4) {
            System.out.println(GREETING);
            return;
        }


        while (true) {
            System.out.print("What is the name of the file? ");
            fileName = scanner.next();
            boolean choice;

            if (new File(fileName).exists()) {
                System.out.println("\nReading BlockChain from file...");
                break;
            }
            else {
                choice = getUserYesNo("The file does not exist, do you want to try again?");
                if (!choice) {
                    System.out.println(GREETING);
                    return;
                }
            }
        }

        scanner.close();

        BlockChain blockChain = BlockChain.fromFile(fileName);

        System.out.println("Ensuring BlockChain validity...\n");


        if (!blockChain.validateBlockchain()) {
            System.out.println("BlockChain is not valid!\n\n");
            System.out.println("Found the following errors: ");

            for (String error : blockChain.errorLogs) {
                System.out.println(error);
            }

            boolean choice = getUserYesNo("Write errors to file?");
            if (choice) {
                blockChain.errorsToFile(fileName);
                System.out.println("Done!");
            }
            System.out.println(GREETING);
            return;
        }

        System.out.println("BlockChain is valid!");

        if (select == 1) {
            System.out.println("Appending miner ID to valid blockchain file");
            blockChain.toFile(appendToFileName(fileName, MINER_ID));
            System.out.println("Done!");
        } else if (select == 2) {
            while (!end) {
                end = addToChain(blockChain);
            }
            if (getUserYesNo("Write BlockChain to file?")) {
                System.out.println("Saving to file...");
                blockChain.toFile(appendToFileName(fileName, MINER_ID));
                System.out.println("Done!");
            }
        } else {
            int[] nonceVals = new int[blockChain.chain.size()-3];
            int sum = 0;
            for (int i = 0; i < blockChain.chain.size()-3; i++) {
                int nonce = convertNonce(blockChain.chain.get(i+3).getNonce());
                nonceVals[i] = nonce;
                sum += nonce;
            }

            Arrays.sort(nonceVals);

            System.out.println("Biggest nonce value: " + nonceVals[nonceVals.length-1]);
            System.out.println("Smallest nonce value: " + nonceVals[0]);
            System.out.println("Average nonce value: " + (int)((double) sum/nonceVals.length));
        }

        System.out.println(GREETING);
    }

    private static boolean addToChain(BlockChain blockChain) {
        System.out.println("Starting new transaction...");
        Scanner scanner = new Scanner(System.in);
        String sender, receiver;
        int amount;
        boolean end;

        while (true) {
            System.out.print("Sender: ");
            sender = scanner.next();
            System.out.print("Receiver: ");
            receiver = scanner.next();
            System.out.print("Amount: ");
            amount = scanner.nextInt();
            if (amount < 1) {
                System.out.println("Cannot send less than 1 bitcoin!");
            } else if (!blockChain.assertBalance(sender, amount)) {
                System.out.println("Sender cannot send more than they have!");
                System.out.println("Sender balance: " + blockChain.getBalance(sender));
            } else {
                break;
            }
            System.out.println("Please try again");

        }
        Transaction transaction = new Transaction(sender, receiver, amount);
        System.out.println("Adding transaction to a block...");
        Block block = new Block(blockChain.getLastIndex() + 1, transaction, blockChain.getLastHash());

        System.out.println("Generating valid nonce...");
        if (!block.validate(CRITERIA)) {
            System.out.println("Cannot validate block!");
            System.out.println("Block not added to the chain");
        } else {
            System.out.println("Block validation completed!");
            System.out.println("Block nonce: " + block.getNonce());
            System.out.println("Block hash: " + block.getHash());
            System.out.println("Adding block to BlockChain...");
            blockChain.add(block);
            System.out.println("Done!");
        }

        end = !getUserYesNo("Do you want to make a new transaction?");
        scanner.close();
        return end;

    }

    private static boolean getUserYesNo(String query) {
        Scanner scanner = new Scanner(System.in);
        String input;
        while (true) {
            System.out.print(query + "(y/n) ");
            input = scanner.next();
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n")) {
                scanner.close();
                return input.equalsIgnoreCase("y");
            } else System.out.println("Invalid input, try again");
        }
    }

    private static int getUserNumSelection(String prompt, int smallest, int largest) {
        Scanner scanner = new Scanner(System.in);
        int choice;
        while (true) {
            System.out.print(prompt + " ");
            choice = scanner.nextInt();
            if (!(choice < smallest || choice > largest)){
                break;
            } else System.out.println("Invalid input, try again");
        }
        return choice;
    }

    public boolean assertBalance(String username, int amount) {
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

    private static void makeMenu(String title, String[] options) {
        System.out.println(title);
        System.out.println("==================================");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d: %s\n", (i+1), options[i]);
        }
        System.out.printf("%d: %s\n\n", options.length+1, "Exit");
    }

    private static int convertNonce(String nonce) {
        byte[] nonceBytes = nonce.getBytes();
        int sum = 0;

        for (int i = 0; i < nonceBytes.length; i++) {
            sum += nonceBytes[i] * Math.pow(93, nonceBytes.length-1-i);
        }

        return sum;
    }
}
