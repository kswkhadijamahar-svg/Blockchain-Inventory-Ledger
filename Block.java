import java.security.MessageDigest;

// One Block = one NODE of the doubly linked list
public class Block {
    int id;
    String timestamp;
    String item;
    int quantity;
    String type;          // "IN" or "OUT"
    String hash;          // hash of this block
    String previousHash;  // hash of the previous block (this is the "chain")
    Block next, prev;     // linked list pointers

    Block(int id, String item, int quantity, String type, String previousHash) {
        this.id = id;
        this.timestamp = java.time.LocalDateTime.now().withNano(0).toString().replace('T', ' ');
        this.item = item;
        this.quantity = quantity;
        this.type = type;
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }

    // Creates a SHA-256 fingerprint of the block's data
    String calculateHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String data = id + timestamp + item + quantity + type + previousHash;
            byte[] bytes = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}