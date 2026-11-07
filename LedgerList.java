import java.util.ArrayList;
import java.util.List;

// Our OWN doubly linked list (no built-in LinkedList). Used by the GUI.
// Every method that changes data returns a message starting with "OK:" or "ERROR:".
public class LedgerList {
    Block head, tail;
    int nextId = 1;
    int invalidId = -1;   // block found by the last validation (-1 = none)

    int getStock(String item) {
        int total = 0;
        for (Block b = head; b != null; b = b.next) {
            if (b.item.equalsIgnoreCase(item)) {
                total += b.type.equals("IN") ? b.quantity : -b.quantity;
            }
        }
        return total;
    }

    // CREATE: add a block at the end -> O(1)
    String add(String item, int qty, String type) {
        if (item.isEmpty()) return "ERROR: Item name is required.";
        if (qty <= 0) return "ERROR: Quantity must be greater than 0.";
        if (type.equals("OUT") && getStock(item) < qty) {
            return "ERROR: Not enough stock! Available: " + getStock(item);
        }
        String prevHash = (tail == null) ? "0" : tail.hash;
        Block b = new Block(nextId++, item, qty, type, prevHash);
        if (head == null) {
            head = tail = b;
        } else {
            tail.next = b;
            b.prev = tail;
            tail = b;
        }
        invalidId = -1;
        return "OK: Block " + b.id + " added (" + type + " " + qty + " " + item + ").";
    }

    // READ: all blocks, head to tail -> O(n)
    List<Block> toList() {
        List<Block> list = new ArrayList<>();
        for (Block b = head; b != null; b = b.next) list.add(b);
        return list;
    }

    // READ: blocks of one item -> O(n)
    List<Block> search(String item) {
        List<Block> list = new ArrayList<>();
        for (Block b = head; b != null; b = b.next) {
            if (b.item.equalsIgnoreCase(item)) list.add(b);
        }
        return list;
    }

    Block find(int id) {
        for (Block b = head; b != null; b = b.next) if (b.id == id) return b;
        return null;
    }

    // recalculate hashes from this block to the end
    void rehashFrom(Block start) {
        for (Block b = start; b != null; b = b.next) {
            b.previousHash = (b.prev == null) ? "0" : b.prev.hash;
            b.hash = b.calculateHash();
        }
    }

    // UPDATE -> O(n)
    String update(int id, int newQty, String newType) {
        Block b = find(id);
        if (b == null) return "ERROR: Block " + id + " not found.";
        if (newQty <= 0) return "ERROR: Quantity must be greater than 0.";
        b.quantity = newQty;
        b.type = newType;
        rehashFrom(b);
        invalidId = -1;
        return "OK: Block " + id + " updated and chain re-hashed.";
    }

    // DELETE: unlink the node, then repair the chain -> O(n)
    String delete(int id) {
        Block b = find(id);
        if (b == null) return "ERROR: Block " + id + " not found.";
        if (b.prev != null) b.prev.next = b.next; else head = b.next;
        if (b.next != null) b.next.prev = b.prev; else tail = b.prev;
        rehashFrom(b.next);
        invalidId = -1;
        return "OK: Block " + id + " deleted and chain re-hashed.";
    }

    // VALIDATE: check every hash -> O(n)
    String validate() {
        invalidId = -1;
        for (Block b = head; b != null; b = b.next) {
            String expectedPrev = (b.prev == null) ? "0" : b.prev.hash;
            if (!b.hash.equals(b.calculateHash()) || !b.previousHash.equals(expectedPrev)) {
                invalidId = b.id;
                return "ERROR: CHAIN INVALID! Tampering detected at Block " + b.id + ".";
            }
        }
        return "OK: Chain is valid. No tampering detected.";
    }

    // DEMO ONLY: change data WITHOUT fixing the hash
    String tamper(int id, int newQty) {
        Block b = find(id);
        if (b == null) return "ERROR: Block " + id + " not found.";
        b.quantity = newQty;
        return "OK: Block " + id + " secretly modified. Now click Validate Chain!";
    }
}