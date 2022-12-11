package com.sovereigncraft.economy;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class lnbits {
    //string to construct the various API URLs for appropriate methods
    static String usersCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/users";
    static String invoiceCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/payments";
    static String userWalletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/wallets";
    static String walletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/wallet";
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
        stringMap.put("memo", "Vault");
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invoiceCmd))
                .headers("X-Api-Key", (String) getWalletinkey(uuid))
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
    public boolean createWallet(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAPIKey())
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(userPutString(uuid)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }

    //Get all users' LNBits account details
    public Map getUsers(){
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
        Map map = SCEconomy.getEco().getUsers();
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
        Map map = SCEconomy.getEco().getWallets();
        List wallets = (List) map.get("wallets");
        for (Object currentWallet : wallets){
            Map wallet = (Map) currentWallet;
            if (String.valueOf(uuid).equals((String) wallet.get("name"))){
                if (String.valueOf(getUser(uuid).get("id")).equals((String) wallet.get("user"))){
                    return wallet;
                }

            }
        } throw new NullPointerException();
    }
    public Boolean hasAccount(UUID uuid) {
        Map map = SCEconomy.getEco().getWallet(uuid);
        if (map == null) {
            return false;
        } return true;
    }
    public Map getWallets() {
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
        return json.JSON2Map(cleanerJSON);
    }
    public boolean withdraw(UUID uuid, Double amount) {
        SCEconomy.getEco().processInvoice(uuid, SCEconomy.getEco().createInvoice( ConfigHandler.getServerUUID(), amount));
        return true;
    }
    public boolean deposit(UUID uuid, Double amount) {
        if (SCEconomy.getEco().processInvoice(ConfigHandler.getServerUUID(), SCEconomy.getEco().createInvoice(uuid, amount))){
            return false;
        }
        return true;
    }
    public Double getBalance(UUID uuid) {
        Map map = SCEconomy.getEco().getWalletDetail(uuid);
        Double bal =  (Double) map.get("balance") / 1000;
        return bal;
    }
    public Boolean has(UUID uuid, Double amt) {
        if (getBalance(uuid) >= amt) {
            return true;
        } return false;
    }
    public boolean createAccount(UUID uuid) {
        if (SCEconomy.getEco().createWallet(uuid)){
            if(SCEconomy.getEco().deposit(uuid, ConfigHandler.getStartingBalance())){
                return true;
            }
            return true;
        }return false;

    }
}
