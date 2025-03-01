import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class FraudDetection extends JFrame {
    private JTextField cardNumberField, amountField, locationField, transIdField, blacklistField;
    private JTextArea resultArea;
    private JButton addTransactionButton, viewTransactionsButton, updateTransactionButton, deleteTransactionButton;
    private JButton blacklistCardButton, viewBlacklistedButton, removeBlacklistButton;

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/credit_card_db";
    private static final String DB_USER = "root";  // Change if necessary
    private static final String DB_PASS = "duNagiri$88";  // Change if necessary

    public FraudDetection() {
        setTitle("Credit Card Fraud Detection System");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Input Fields
        add(new JLabel("Card Number:"));
        cardNumberField = new JTextField(16);
        add(cardNumberField);

        add(new JLabel("Amount:"));
        amountField = new JTextField(10);
        add(amountField);

        add(new JLabel("Location:"));
        locationField = new JTextField(15);
        add(locationField);

        add(new JLabel("Transaction ID (For Update/Delete):"));
        transIdField = new JTextField(5);
        add(transIdField);

        add(new JLabel("Blacklist Card Number:"));
        blacklistField = new JTextField(16);
        add(blacklistField);

        // Buttons
        addTransactionButton = new JButton("Add Transaction");
        viewTransactionsButton = new JButton("View Transactions");
        updateTransactionButton = new JButton("Update Transaction Status");
        deleteTransactionButton = new JButton("Delete Transaction");
        blacklistCardButton = new JButton("Blacklist Card");
        viewBlacklistedButton = new JButton("View Blacklisted Cards");
        removeBlacklistButton = new JButton("Remove Blacklisted Card");

        add(addTransactionButton);
        add(viewTransactionsButton);
        add(updateTransactionButton);
        add(deleteTransactionButton);
        add(blacklistCardButton);
        add(viewBlacklistedButton);
        add(removeBlacklistButton);

        // Result Area
        resultArea = new JTextArea(20, 60);
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea));

        // Action Listeners
        addTransactionButton.addActionListener(this::addTransaction);
        viewTransactionsButton.addActionListener(e -> showTableData("transactions"));
        updateTransactionButton.addActionListener(this::updateTransaction);
        deleteTransactionButton.addActionListener(this::deleteTransaction);
        blacklistCardButton.addActionListener(this::blacklistCard);
        viewBlacklistedButton.addActionListener(e -> showTableData("blacklisted_cards"));
        removeBlacklistButton.addActionListener(this::removeBlacklistCard);

        setVisible(true);
    }

    private void addTransaction(ActionEvent e) {
        String cardNumber = cardNumberField.getText();
        String location = locationField.getText();
        double amount;

        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid amount. Please enter a number.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             CallableStatement stmt = conn.prepareCall("{CALL AddTransaction(?, ?, ?)}")) {
            stmt.setString(1, cardNumber);
            stmt.setDouble(2, amount);
            stmt.setString(3, location);
            stmt.execute();
            resultArea.setText("Transaction Added Successfully!");
        } catch (SQLException ex) {
            resultArea.setText("Database Error: " + ex.getMessage());
        }
    }

    private void updateTransaction(ActionEvent e) {
        int transactionId;
        try {
            transactionId = Integer.parseInt(transIdField.getText());
        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid Transaction ID.");
            return;
        }

        String newStatus = JOptionPane.showInputDialog(this, "Enter new status (Valid/Fraudulent):");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             CallableStatement stmt = conn.prepareCall("{CALL UpdateTransactionStatus(?, ?)}")) {
            stmt.setInt(1, transactionId);
            stmt.setString(2, newStatus);
            stmt.execute();
            resultArea.setText("Transaction Updated Successfully!");
        } catch (SQLException ex) {
            resultArea.setText("Database Error: " + ex.getMessage());
        }
    }

    private void deleteTransaction(ActionEvent e) {
        int transactionId;
        try {
            transactionId = Integer.parseInt(transIdField.getText());
        } catch (NumberFormatException ex) {
            resultArea.setText("Invalid Transaction ID.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             CallableStatement stmt = conn.prepareCall("{CALL DeleteTransaction(?)}")) {
            stmt.setInt(1, transactionId);
            stmt.execute();
            resultArea.setText("Transaction Deleted Successfully!");
        } catch (SQLException ex) {
            resultArea.setText("Database Error: " + ex.getMessage());
        }
    }

    private void blacklistCard(ActionEvent e) {
        String cardNumber = blacklistField.getText();
        if (cardNumber.isEmpty()) {
            resultArea.setText("Please enter a card number to blacklist.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             CallableStatement stmt = conn.prepareCall("{CALL BlacklistCard(?)}")) {
            stmt.setString(1, cardNumber);
            stmt.execute();
            resultArea.setText("Card " + cardNumber + " has been blacklisted.");
        } catch (SQLException ex) {
            resultArea.setText("Database Error: " + ex.getMessage());
        }
    }

    private void removeBlacklistCard(ActionEvent e) {
        String cardNumber = blacklistField.getText();
        if (cardNumber.isEmpty()) {
            resultArea.setText("Please enter a card number to remove from blacklist.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             CallableStatement stmt = conn.prepareCall("{CALL RemoveBlacklistedCard(?)}")) {
            stmt.setString(1, cardNumber);
            stmt.execute();
            resultArea.setText("Card " + cardNumber + " removed from blacklist.");
        } catch (SQLException ex) {
            resultArea.setText("Database Error: " + ex.getMessage());
        }
    }

    private void showTableData(String tableName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            StringBuilder result = new StringBuilder("Table: " + tableName + "\n");

            // Display column headers
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                result.append(metaData.getColumnName(i)).append("\t");
            }
            result.append("\n");

            // Display rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.append(rs.getString(i)).append("\t");
                }
                result.append("\n");
            }

            resultArea.setText(result.toString());
        } catch (SQLException ex) {
            resultArea.setText("Database Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FraudDetection::new);
    }
}
