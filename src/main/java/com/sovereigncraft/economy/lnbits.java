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

public class lnbits {
    //string to construct the various API URLs for appropriate methods
    static String usersCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/users";
    static String invoiceCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/payments";
    static String userWalletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/wallets";
    static String walletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/wallet";
    //Methods to construct a string of JSON as required for different methods
    public static String processInvoicePutString(String invoice) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "true");
        stringMap.put("bolt11", invoice);
        return gson.toJson(stringMap);
    }
    public static String createInvoicePutString(Integer amt) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "false");
        stringMap.put("amount", amt.toString());
        stringMap.put("memo", "Vault");
        return gson.toJson(stringMap);
    }
    public static String userPutString(String uuid){
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("admin_id", ConfigHandler.getAdminUser());
        stringMap.put("wallet_name", uuid);
        stringMap.put("user_name", uuid);
        return gson.toJson(stringMap);
    }
    //create an invoice and return the bolt 11 ln string
    public static String createInvoice(String User, Integer amount) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invoiceCmd))
                .headers("X-Api-Key", getWalletinkey(User))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(createInvoicePutString(amount)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseJSON = response.body();
        String bolt11 = (String) json.JSON2Map(responseJSON).get("payment_request");
        return bolt11;
    }

    //process an invoice
    public static void processInvoice(String player, String invoice) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invoiceCmd))
                .headers("X-Api-Key", getWalletAdminKey(player))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(processInvoicePutString(invoice)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Create a user and wallet for a new user
    public static boolean createWallet(String uuid) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAPIKey())
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(userPutString(uuid)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return true;
    }

    //Get all users' LNBits account details
    public static Map getUsers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseJSON = response.body();
        String cleanerJSON = "{ 'users': " + responseJSON + " }";
        return json.JSON2Map(cleanerJSON);
    }
    public static Map getUser(String uuid) throws IOException, InterruptedException {
        Map map = getUsers();
        List users = (List) map.get("users");
        for (Object currentUser : users){
            Map user = (Map) currentUser;
            if (uuid.equals((String) user.get("name"))){
                return user;
            }
        } throw new NullPointerException();
    }



    //Get the details for the users wallet used in game. Used for balance inquiry

    public static String getWalletinkey(String name) throws IOException, InterruptedException {
        Map wallet = getWallet(name);
        return (String) wallet.get("inkey");
    }
    public static String getWalletAdminKey(String name) throws IOException, InterruptedException {
        Map wallet = getWallet(name);
        return (String) wallet.get("adminkey");
    }
    public static Map getWalletDetail(String name) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(walletCmd))
                .headers("X-Api-Key", (String) getWalletAdminKey(name))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseJSON = response.body();
        return json.JSON2Map(responseJSON);
    }
    public static Map getWallet(String uuid) throws IOException, InterruptedException {
        Map map = getWallets();
        List wallets = (List) map.get("wallets");
        for (Object currentWallet : wallets){
            Map wallet = (Map) currentWallet;
            if (uuid.equals((String) wallet.get("name"))){
                if (getUser(uuid).get("id").equals((String) wallet.get("user"))){
                    return wallet;
                }

            }
        } throw new NullPointerException();
    }
    public static Map getWallets() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userWalletCmd))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseJSON = response.body();
        String cleanerJSON = "{ 'wallets': " + responseJSON + " }";
        return json.JSON2Map(cleanerJSON);
    }
}
