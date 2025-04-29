// OutletMenu.java
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class OutletMenu {
    private static ArrayList<Item> globalCart = new ArrayList<>();
    private JFrame frame;

    public OutletMenu(String outletName) {
        frame = new JFrame("Cuisine Crossroad - " + outletName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());

        JPanel foodPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JScrollPane scrollPane = new JScrollPane(foodPanel);

        HashMap<String, Item[]> outletItems = getOutletItems();
        Item[] items = outletItems.getOrDefault(outletName, new Item[0]);

        for (Item baseItem : items) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createTitledBorder(baseItem.getName() + " (₹" + baseItem.getPrice() + ")"));

            // Image
            try {
                ImageIcon icon = new ImageIcon("images/" + outletName.toLowerCase().replace(" ", "") + "/" + baseItem.getName().toLowerCase().replace(" ", "") + ".jpg");
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(img));
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(imageLabel);
            } catch (Exception e) {
                panel.add(new JLabel("Image not found"));
            }

            // Variant selection
            JComboBox<String> variantBox = new JComboBox<>(getVariants(baseItem.getName()));
            panel.add(new JLabel("Choose Variant:"));
            panel.add(variantBox);

            // Topping/Flavor
            JComboBox<String> optionsBox = null;
            if (baseItem.getName().equalsIgnoreCase("Pizza") || baseItem.getName().equalsIgnoreCase("Pastry")) {
                String[] options = baseItem.getName().equalsIgnoreCase("Pizza")
                        ? new String[]{"No Topping (₹0)", "Cheese (₹30)", "Extra Cheese (₹50)", "Paneer (₹40)", "Corn (₹25)"}
                        : new String[]{"Vanilla (₹0)", "Chocolate (₹10)", "Strawberry (₹10)"};

                optionsBox = new JComboBox<>(options);
                panel.add(new JLabel(baseItem.getName().equalsIgnoreCase("Pizza") ? "Toppings:" : "Flavors:"));
                panel.add(optionsBox);
                panel.putClientProperty("customOptionBox", optionsBox);
            }

            // Quantity
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
            panel.add(new JLabel("Quantity:"));
            panel.add(spinner);

            panel.putClientProperty("item", baseItem);
            panel.putClientProperty("variantBox", variantBox);
            panel.putClientProperty("quantitySpinner", spinner);

            foodPanel.add(panel);
        }

        JButton backBtn = new JButton("← Back to Outlets");
        backBtn.addActionListener(e -> {
            frame.dispose();
            new MainMenu(); 
        });

        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.addActionListener(e -> {
            for (Component comp : foodPanel.getComponents()) {
                JPanel p = (JPanel) comp;
                Item base = (Item) p.getClientProperty("item");
                String variant = ((JComboBox<String>) p.getClientProperty("variantBox")).getSelectedItem().toString();
                int qty = (int) ((JSpinner) p.getClientProperty("quantitySpinner")).getValue();

                int extraPrice = 0;
                String custom = "";

                if (p.getClientProperty("customOptionBox") != null) {
                    JComboBox<String> box = (JComboBox<String>) p.getClientProperty("customOptionBox");
                    custom = box.getSelectedItem().toString();
                    extraPrice = extractPrice(custom); 
                }

                if (qty > 0) {
                    String name = base.getName() + " - " + variant + (custom.isEmpty() ? "" : " [" + custom + "]");
                    Item ordered = new Item(name, base.getPrice() + extraPrice);
                    ordered.setQuantity(qty);
                    globalCart.add(ordered);
                }
            }

            JOptionPane.showMessageDialog(frame, "Items added to cart!");
        });

        JButton checkoutBtn = new JButton("Confirm Order");
        checkoutBtn.addActionListener(e -> {
            if (globalCart.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Cart is empty!");
                return;
            }
        
            String time = JOptionPane.showInputDialog(frame, "Enter the date and time, when you will receive the order?");
            if (time != null && !time.trim().isEmpty()) {
                String customerName = JOptionPane.showInputDialog(frame, "Enter your name:");

                if (customerName != null && !customerName.trim().isEmpty()) {
                    OrderDatabaseInserter.insertOrderData(globalCart, customerName, time);
        
                    JOptionPane.showMessageDialog(frame, "Order placed successfully!");
    
                    new CheckoutPage(globalCart, time);
        
                    globalCart.clear();
        
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Name cannot be empty!");
                }
            }
        });
        

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backBtn);
        bottomPanel.add(addToCartBtn);
        bottomPanel.add(checkoutBtn);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private HashMap<String, Item[]> getOutletItems() {
        HashMap<String, Item[]> map = new HashMap<>();
        map.put("Dominos", new Item[]{new Item("Pizza", 250), new Item("Pasta", 200)});
        map.put("Bakery", new Item[]{new Item("Pastry", 100), new Item("Cake", 400)});
        map.put("Kathi Rolls", new Item[]{new Item("Veg Roll",100), new Item("Non Veg Roll",130), new Item("Panner roll",150)});
        map.put("Dunking Donut", new Item[]{new Item("Honey Donout",80), new Item("Chocholate Donout",110)});
        map.put("Annapurna", new Item[]{new Item("Dosa",60), new Item("Idli",40)});
        map.put("Food Court", new Item[]{new Item("Samosa",30), new Item("Aalu paratha",40)});
        return map;
    }

    private String[] getVariants(String itemName) {
        switch (itemName.toLowerCase()) {
            case "pizza":
                return new String[]{"Cheese Pizza", "Veg Pizza", "Paneer Pizza"};
            case "pastry":
                return new String[]{"Small", "Medium", "Large"};
            default:
                return new String[]{"Regular"};
        }
    }

    private int extractPrice(String optionText) {
        if (optionText.contains("₹")) {
            try {
                return Integer.parseInt(optionText.replaceAll(".*₹", "").replaceAll("[^\\d]", ""));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}
