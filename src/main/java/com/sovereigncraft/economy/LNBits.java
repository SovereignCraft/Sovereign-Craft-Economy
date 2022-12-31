package com.sovereigncraft.economy;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.*;

public class LNBits {
    //string to construct the various API URLs for appropriate methods
    public static String usersCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/users";
    public static String invoiceCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/payments";
    public static String userWalletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/wallets";
    public static String walletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/wallet";
    //Methods to construct a string of JSON as required for different methods
    public String processInvoicePutString(String invoice) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "true");
        stringMap.put("bolt11", invoice);
        return gson.toJson(stringMap);
    }
    public String createInvoicePutString(Double amt) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "false");
        stringMap.put("amount", amt.toString());
        stringMap.put("memo", "Sovereign Craft");
        return gson.toJson(stringMap);
    }
    public String userPutString(UUID uuid){
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("admin_id", ConfigHandler.getAdminUser());
        stringMap.put("wallet_name", String.valueOf(uuid));
        stringMap.put("user_name", String.valueOf(uuid));
        return gson.toJson(stringMap);
    }
    //create an invoice and return the bolt 11 ln string
    public String createInvoice(UUID uuid, Double amount) {
        //System.out.println("Creating an Invoice");
        //System.out.println(getWalletinkey(uuid));
        //System.out.println(invoiceCmd);
        //System.out.println(createInvoicePutString(amount));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invoiceCmd))
                .headers("X-Api-Key", getWalletinkey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(createInvoicePutString(amount)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        String responseJSON = response.body();
        //System.out.println(String.join(",", "payment request: ", (String) json.JSON2Map(responseJSON).get("payment_request")));
        return (String) json.JSON2Map(responseJSON).get("payment_request");
    }

    //process an invoice
    public Boolean processInvoice(UUID uuid, String invoice) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invoiceCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(processInvoicePutString(invoice)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    // Create a user and wallet for a new user
    public boolean tosMessage(Player sender){
        if (!(sender instanceof Player)) {
            sender.sendMessage( "Only players can get their web wallet");
            return true;
        }

        Player player = (Player) sender;
        String url = ConfigHandler.getTOSURL();
        System.out.println(url);
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "tellraw " + player.getName() +
                        " {\"text\":\"" + "You have no wallet. Please accept the Terms of Service by clicking this message" +
                        "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
                        url + "\"}}");
        return true;
    }
    public boolean createWallet(UUID uuid) {
        //System.out.println("Create Wallet Called this API:");
        //System.out.println(usersCmd);
        //System.out.println(ConfigHandler.getAPIKey());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAPIKey())
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(userPutString(uuid)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        //System.out.println(userPutString(uuid));
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }

    //Get all users' LNBits account details
    public Map getUsers(){
        //System.out.println("getting users");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        String responseJSON = response.body();
        String cleanerJSON = "{ 'users': " + responseJSON + " }";
        return json.JSON2Map(cleanerJSON);
    }
    public Map getUser(UUID uuid) {
        Map map = getUsers();
        List users = (List) map.get("users");
        for (Object currentUser : users){
            Map user = (Map) currentUser;
            if (String.valueOf(uuid).equals((String) user.get("name"))){
                return user;
            }
        } throw new NullPointerException();
    }



    //Get the details for the users wallet used in game. Used for balance inquiry

    public String getWalletinkey(UUID uuid) {
        //System.out.println("constructing inkey");
        Map wallet = getWallet(uuid);
        return (String) wallet.get("inkey");
    }
    public String getWalletAdminKey(UUID uuid) {
        Map wallet = getWallet(uuid);
        return (String) wallet.get("adminkey");
    }
    public Map getWalletDetail(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(walletCmd))
                .headers("X-Api-Key", (String) getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        String responseJSON = response.body();
        return json.JSON2Map(responseJSON);
    }
    public Map getWallet(UUID uuid) {
        //System.out.println("getting a wallet");
        Map map = getWallets();
        //System.out.println("Wallets gotten");
        List wallets = (List) map.get("wallets");
        for (Object currentWallet : wallets){
            Map wallet = (Map) currentWallet;
            if (String.valueOf(uuid).equals((String) wallet.get("name"))){
                if (String.valueOf(getUser(uuid).get("id")).equals((String) wallet.get("user"))){
                    return wallet;
                }

            }
        } return (new HashMap<>());
    }
    public Boolean hasAccount(UUID uuid) {
        //System.out.println("checking wallet");
        Map wallet = getWallet(uuid);
        return (wallet != null && wallet.containsKey("user"));
    }
    public Map getWallets() {
        //System.out.println("Getting all the wallets");
        //System.out.println(String.join(",", "with admin key: ", ConfigHandler.getAdminKey()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userWalletCmd))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        String responseJSON = response.body();
        String cleanerJSON = "{ 'wallets': " + responseJSON + " }";
        //System.out.println("I'm a map!");
        return json.JSON2Map(cleanerJSON);
    }
    public boolean withdraw(UUID uuid, Double amount) {
        return processInvoice(uuid, createInvoice( ConfigHandler.getServerUUID(), amount));
    }
    public boolean deposit(UUID uuid, Double amount) {
        //System.out.println("Deposit running");
        Boolean payment = processInvoice(ConfigHandler.getServerUUID(), createInvoice(uuid, amount));
        //System.out.println("Payment complete");
        if (payment){
            return true;
        }
        return false;
    }
    public Double getBalance(UUID uuid) {
        Map map = getWalletDetail(uuid);
        Double bal =  (Double) map.get("balance") / 1000;
        return bal;
    }
    public String getBalanceString(UUID uuid) {
        return "⚡" + SCEconomy.getEco().numberFormat(getBalance(uuid));
    }
    public String numberFormat(Double number){
        DecimalFormat df = new DecimalFormat("###,###,###");
        df.setGroupingSize(3);
        return df.format(number);
    }
    public Boolean has(UUID uuid, Double amt) {
        if (getBalance(uuid) >= amt) {
            return true;
        } return false;
    }
    public boolean createAccount(UUID uuid) {
        Boolean account = createWallet(uuid);
        if (account){
            //System.out.println("deposit starting balance command");
            //System.out.println(String.join(",", "Attempting to deposit:", Double.toString(ConfigHandler.getStartingBalance())));
            Boolean deposit = deposit(uuid, ConfigHandler.getStartingBalance());
            if(deposit){
                return true;
            }
            return true;
        }return false;

    }
}
