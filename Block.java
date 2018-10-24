

import java.sql.Timestamp;

public class Block {
	private int index; // the index of the block in the list
	private java.sql.Timestamp timestamp; // time at which transaction
	// has been processed
	private Transaction transaction; // the transaction object
	private String nonce; // random string (for proof of work)
	private String previousHash; // previous hash (set to "" in first block)
	//(in first block, set to string of zeroes of size of complexity "00000")
	private String hash; // hash of the block (hash of string obtained
	
	public Block(int index, Timestamp timestamp, Transaction transaction, String nonce, String previousHash, String hash) {
		this.index = index;
		this.timestamp = timestamp;
		this.transaction = transaction;
		this.nonce = nonce;
		this.hash = hash;
		this.previousHash = previousHash;
	}
	//second constructor for when the nonce and hash will be generated afterwards (Proof-of-Work)
	public Block(int index, Timestamp timestamp, Transaction transaction, String previousHash) {
		this.index = index;
		this.timestamp = timestamp;
		this.transaction = transaction;
		this.previousHash = previousHash;
	}

	public int getIndex() {

		return this.index;
	}

	public Timestamp getTimestamp() {

		return this.timestamp;
	}

	public Transaction getTransaction() {

		return this.transaction;
	}

	public String getNonce() {

		return this.nonce;
	}

	public String getHash() {

		return this.hash;
	}

	public String getPreviousHash() {

		return this.previousHash;
	}

	public String toString() {
		return this.timestamp.toString() + ":" + this.transaction.toString() + "." + this.nonce+ this.previousHash;
	}

	public void setNonce(String randomAsciiString) {

		this.nonce = randomAsciiString;
	}

	public void setHash(String calculatedHash) {

		this.hash = calculatedHash;
	}

}