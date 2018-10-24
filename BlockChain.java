
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;



public class BlockChain {

	final static String USERID = "snaya091"; //snaya091@uottawa.ca 
	
	final Random random = new Random();

	List<Block> listOfBlocks;
	
//Prompts user for transaction information
	public static Transaction promptTransactionDetails (Scanner scanner) {

		System.out.println("Sender name:");
		String sender = scanner.nextLine();
		System.out.println("Receiver name:");
		String receiver = scanner.nextLine();
		int amount = 0;
		while(amount <= 0) {
			System.out.println("Amount (>0):");
			String amountStr = scanner.nextLine();
			try {
				amount = Integer.valueOf(amountStr);
			}
			catch (NumberFormatException e) {
				System.out.println("Amount must be numeric.");
				continue;
			}
		}

		
		return new Transaction(sender, receiver, amount);
	}
//asks user for details involving saving the file for the specified blockchain
	public static void promptFileSaveOptions (Scanner scanner, BlockChain blockChain) {

		System.out.println("Save the BlockChain to [filename_userid.txt] filename:");
		String fileName = scanner.nextLine();

		blockChain.toFile(fileName+"_"+BlockChain.USERID+".txt");
	}

	//Main Method that is being run
	public static void main (String [] arg) {

		BlockChain blockChain = BlockChain.fromFile("blockchain_sgund051.txt");

		if (!blockChain.validateBlockchain()) {
			System.out.println("Invalid blockchain");
		} else {
			System.out.println("Blockchain created of size " + blockChain.size());


			System.out.println("Blockchain is valid");


			Scanner scanner = new Scanner(System.in);

			while (true) {
				System.out.println("1. Enter a transaction\n2. Save the blockchain\n3. Exit\n:"); //3 options provided for chosing the operation
				try {
					String ans = scanner.next("[1,2,3]");
					scanner.nextLine();
					if (0 == ans.compareTo("1")) { //action triggered for entering a transaction
						Transaction transaction = promptTransactionDetails(scanner);
						if ((blockChain.getBalance(transaction.getSender()) < transaction.getAmount()) && !transaction.getSender().equals("bitcoin")) {
							System.out.println("Not enough balance for " + transaction.toString());
							continue;
						}
						Block lastBlock = blockChain.listOfBlocks.get(blockChain.listOfBlocks.size() - 1);
						Block newBlock = new Block(lastBlock.getIndex() + 1, new Timestamp(System.currentTimeMillis()), transaction, lastBlock.getHash());

						blockChain.add(newBlock);
					} else if (0 == ans.compareTo("2")) { //action triggered for saving file
						promptFileSaveOptions(scanner, blockChain);
					} else if (0 == ans.compareTo("3")) { //action triggered for exiting
						System.exit(0);
					}
				} catch (InputMismatchException e) {
					scanner.next();
					System.out.println("Error: Invalid input");
				}
			}
		}
	}
	//constructor
	public BlockChain(List<Block> listOfBlocks) {
		// TODO Auto-generated constructor stub
		this.listOfBlocks = listOfBlocks;
	}

	//calculates size of blockchain
	public int size() {
		if(listOfBlocks != null) {
			return listOfBlocks.size();
		}
		return 0;
	}

	// method to convert amount from type str to type int and making it an official transaction
	private static Transaction getTransaction(String sender, String receiver, String amountStr) {

		if(sender == null || sender.isEmpty() ||
			receiver == null || receiver.isEmpty() || 
			amountStr == null) 
		{
			// invalid transaction parameters
			return null;
		}
		try {
			int amount = Integer.parseInt(amountStr);
			return new Transaction(sender, receiver, amount);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	/*
	 * Read blockchain from a file
	 */
	public static BlockChain fromFile(String fileName) {

		List<Block> tempListOfBlocks = new ArrayList<Block>();

		try {
			BufferedReader br = Files.newBufferedReader(Paths.get(fileName));

			try {
				String previousHash = "00000";
				// read the first line - index value for the block
				String indexStr;
				while ((indexStr = br.readLine()) != null) {
					// read the remaining six entries for a block
					String [] blockLines = new String [6];
					for (int i=0; i < 6; i++) {
						blockLines[i] = br.readLine();
					}
					int newIndex = Integer.valueOf(indexStr);

					// timestamp is Long value (milliseconds since Jan-1,1970)
					Timestamp timestamp = new Timestamp(Long.valueOf(blockLines[0]));
					Transaction transaction = getTransaction(blockLines[1], blockLines[2], blockLines[3]);
					String nonce = blockLines[4];
					String hash = blockLines[5];
					tempListOfBlocks.add(new Block(newIndex, timestamp, transaction, nonce, previousHash, hash));
					previousHash = hash;

				}
				br.close();
			} catch (NumberFormatException e) {
				// invalid index or amount value makes the block invalid
				br.close();
			}
		} catch (IOException e) {
			// file io error
			e.printStackTrace();
		}

		return new BlockChain(tempListOfBlocks);
	}

	//converts blockchain to a file
	public void toFile(String fileName) {
		
		try {
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileName));
			
			Iterator<Block> itr = listOfBlocks.iterator(); //iterates through list of blocks
			while (itr.hasNext()) { //continues until it reaches end of list
				Block block = itr.next();
				// write the block entries in order
				bw.write(String.valueOf(block.getIndex())); 
				bw.newLine();
				// write doesnot support long values - convert the timestamp long value to String
				bw.write(String.valueOf(block.getTimestamp().getTime()));
				bw.newLine();
				bw.write(block.getTransaction().getSender());
				bw.newLine();
				bw.write(block.getTransaction().getReceiver());
				bw.newLine();
				bw.write(String.valueOf(block.getTransaction().getAmount()));
				bw.newLine();
				bw.write(block.getNonce());
				bw.newLine();
				bw.write(block.getHash());
				if(itr.hasNext()) {
					bw.newLine();
				}
			}
			bw.close();
			System.out.println("Successfully saved the blockchain to file "+fileName);
		} catch (IOException  e) {
			// file io error
			e.printStackTrace();
		}
	}

	//validates the blockchain using conditions
	public boolean validateBlockchain() {

		Map<String,Integer> balance = new HashMap<String, Integer>(); //creates hash map of user (string) and balance (int)

		String previousHash = "00000";
		int previousIndex = 0;

		Iterator<Block> itr = listOfBlocks.iterator();
		while (itr.hasNext()) {
			Block block = itr.next();

			Transaction transaction = block.getTransaction();

			// 1. get senders current balance and check if less than transaction amount
			// if sender is bitcoin no need to validate balance
			Integer senderBalance = 0;
			if(0 != transaction.getSender().compareTo("bitcoin")) {
				if(!balance.containsKey(transaction.getSender())) {
					System.out.println("Sender "+transaction.getSender()+" not found");
					return false;
				}
				senderBalance = balance.get(transaction.getSender());
				if(senderBalance < block.getTransaction().getAmount()) {
					System.out.println("Sender "+transaction.getSender()+" does not have enough balance");
					return false;
				}
			}


			// 2. validate the block
			// compare the previous index and hash values
			if(previousIndex != block.getIndex()) {
				System.out.println("Index mismatch, Expected Index: "+previousIndex+", Actual Index: "+block.getIndex());
				return false;
			}
			if(0 != previousHash.compareTo(block.getPreviousHash())) {
				System.out.println("previousHash mismatch"+previousHash+" "+block.getPreviousHash());
				return false;
			}
			try {
				// calculate the hash and compare it
				String calculatedHash = Sha1.hash(block.toString());
				if(0 != calculatedHash.compareTo(block.getHash())) {
					System.out.println("hash mismatch"+calculatedHash+" "+block.getHash());
					return false;
				}
				// update the previousIndex and previousHash values for the next iteration (block)
				previousIndex++;
				previousHash = block.getHash();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			// 3. Update the sender's and receiver's current balance
			senderBalance -=transaction.getAmount(); // sender's new balance
			balance.put(transaction.getSender(), senderBalance);

			if(balance.containsKey(transaction.getReceiver())) {
				Integer receiverBalance = balance.get(transaction.getReceiver());
				receiverBalance +=transaction.getAmount(); //receiver's new balance
				balance.put(transaction.getReceiver(), receiverBalance);
			}
			else {
				// add a new receiver entry
				balance.put(transaction.getReceiver(), transaction.getAmount());
			}

		}

		return true;

	}

	//gets balance of specified username
	public int getBalance(String username) {

		int balance = 0;
		
		Iterator<Block> itr = listOfBlocks.iterator();
		while (itr.hasNext()) {
			Block block = itr.next();

			Transaction transaction = block.getTransaction();

			// if username is sender reduce the balance
			if(0 == transaction.getSender().compareTo(username)) {
				balance -= transaction.getAmount();
			}
			// if username is receiver increase the balance
			if(0 == transaction.getReceiver().compareTo(username)) {
				balance += transaction.getAmount();
			}
		}
		

		return balance;
	}

	//generates random ascii string from a spectrum of 94 characters for nonce generation
	private String getRandomAsciiString(int nbOfChars) {

		StringBuilder randStr = new StringBuilder();
		random.ints(nbOfChars, 33, 127).forEach(intVal -> {
			randStr.append((char) intVal);
			});
		
		return randStr.toString();
		
	}

	// Contains Proof-of-Work Algorithm and Nonce and Hash Generation
	public void add(Block block) {

		try {
			// 94 (126-33+1) possible unique values for a character value
			int uniqueValues = 94;
			// we will try a random string of size 3 and upto 20
			int initialSizeOfString = 3;
			int finalSizeOfString = 20;
			
			int sizeOfString = initialSizeOfString;
			// maximum number of possible unique string values
			int maxUniqueStrings = (int) Math.pow(uniqueValues, sizeOfString);
			
			block.setNonce(getRandomAsciiString(sizeOfString));
			String calculatedHash = Sha1.hash(block.toString());
			int totalCombinationsChecked = 0;
			int combinationsChecked = 0;
			while(!calculatedHash.startsWith("00000")) {

				combinationsChecked++;
				if(combinationsChecked >= maxUniqueStrings) {
					// increase the size of the string
					sizeOfString++;
					System.out.println("Checking with nonce string of size "+sizeOfString);
					totalCombinationsChecked += combinationsChecked;
					combinationsChecked = 0; //reset
					if(sizeOfString > finalSizeOfString) {
						// break if size of string greater than max
						break;
					}
					// update max unique string values
					maxUniqueStrings = (int) Math.pow(uniqueValues, sizeOfString);
				}
				// get the next nonce value and calculate hash
				block.setNonce(getRandomAsciiString(sizeOfString));
				calculatedHash = Sha1.hash(block.toString());
			}
			
			if(calculatedHash.startsWith("00000")) {
				System.out.println("Found a valid nonce= "+block.getNonce()+" after "+(totalCombinationsChecked+combinationsChecked+1)+" checks");
				block.setHash(calculatedHash);
				this.listOfBlocks.add(block);
				System.out.println("Successfully added block to the chain "+block.toString());
			}
			else {
				System.out.println("Failed to add block to the chain. Nonce values checked for sizes "+initialSizeOfString+" to "+finalSizeOfString);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
