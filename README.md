# Blockchain-Based Inventory Ledger

A desktop inventory system built in Java (Swing) for the SW 212 Data Structures & Algorithms open ended lab.

Every stock movement (IN / OUT) is stored as a block in a doubly linked list written from scratch. Each block holds a SHA-256 hash of its data and the hash of the previous block, so any secret change to a record is detected.

## Features
- Create, Read (show all, search, stock level), Update and Delete
- Automatic stock calculation per item
- Rejects OUT transactions larger than the available stock
- Chain validation that finds the exact tampered block
- Colour-coded table with a background picture

## Files
- Block.java - node of the linked list
- LedgerList.java - doubly linked list and all operations
- LedgerApp.java - Swing graphical interface
- background.png - background picture

## How to run
Requires JDK 17 or newer. Keep all files in the same folder, then run:

    javac *.java
    java LedgerApp

## Author
Khadija, Batch K25SW, MUET Shaheed Zulfiqar Ali Bhutto Campus, Khairpur Mir's