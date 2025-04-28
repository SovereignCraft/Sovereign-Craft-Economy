package com.sovereigncraft.economy;

import com.google.gson.Gson;
import lombok.SneakyThrows;
//import org.bukkit.World; // isn't used.
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.*;

public class LNBits {
    //string to construct the various API URLs for appropriate methods
    public static String extensionsCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/extensions";
    // ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    //public static String usersCmd = "https://" + ConfigHandler.getHost() + "/usermanager/api/v1/users";
    public static String usersCmd = "https://" + ConfigHandler.getHost() + "/users/api/v1/user"; // User management (new path)
    public static String invoiceCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/payments";
    public static String lnurlpCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/lnurlp/api/v1/links";
    public static String lnurlwCmd = "https://" + ConfigHandler.getHost() + "/withdraw/api/v1/links";
    // ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    //public static String userWalletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/wallets";
    // might need getPort, unsure.
    public static String userWalletCmd = "https://" + ConfigHandler.getHost() + "/users/api/v1/user"; // For user-specific wallets
    public static String walletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/wallet";
    public static String currenciesCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/currencies";
    public static String convertCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/conversion";
    public static String lnurlscanCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/lnurlscan/";
    public static String paylnurlCmd = "https://" + ConfigHandler.getHost() + "/api/v1/payments/lnurl";
    //Methods to construct a string of JSON as required for different methods
    public String processInvoicePutString(String invoice) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "true");
        stringMap.put("bolt11", invoice);
        return gson.toJson(stringMap);
    }
    public String convertPostString(String from, Double amount, String to) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("from_", from.toLowerCase());
        stringMap.put("amount", String.valueOf(amount));
        stringMap.put("to", to);
        return gson.toJson(stringMap);
    }
    public void sendLNAddress (Player player, String lnaddr, Double amount){
        Map<String, Object> lnurl = convertLnaddrtoLnurl(lnaddr);
        if (lnurl.containsKey("callback")) {
            if (!lnurl.get("description").toString().isEmpty()) {
                player.sendMessage("Sending to: " + lnurl.get("description").toString());
            }
            Map<String, Object> paid = payLnurl(player.getUniqueId(), lnurl, amount);
            if (paid.containsKey("payment_hash")) player.sendMessage("Payment Successful");
            else player.sendMessage("Payment Failed");
        } else player.sendMessage("Invalid Lightning address");
    }
    public Map<String, Object> convertLnaddrtoLnurl (String lnaddr) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lnurlscanCmd + URLEncoder.encode(lnaddr)))
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
        return json.JSON2Map(responseJSON);
    }
    public String payLnurlPostString(Map<String, Object> lnurl, Double amount) {
        Gson gson = new Gson();
        int milliAmount = (int) Math.floor(amount * 1000);
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("description_hash", lnurl.get("description_hash").toString());
        stringMap.put("callback", lnurl.get("callback").toString());
        stringMap.put("amount", String.valueOf(milliAmount));
        stringMap.put("description", lnurl.get("description").toString());
        stringMap.put("unit", "sat");
         return gson.toJson(stringMap);
    }
    public Map<String, Object> payLnurl(UUID uuid, Map<String, Object> lnurl, Double amount) {
         HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paylnurlCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(payLnurlPostString(lnurl, amount)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return json.JSON2Map(response.body());
    }
    public Double getConversion (String from, Double amount, String to) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(convertCmd))
                .version(HttpClient.Version.HTTP_1_1)
                .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Currencies"))
                .POST(HttpRequest.BodyPublishers.ofString(convertPostString(from.toLowerCase(), amount, to.toLowerCase())))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return (Double) json.JSON2Map(response.body()).get(to);
    }

    public List<String> getCurrencies(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(currenciesCmd))
                .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Currencies"))
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
        return json.JSON2List(String.valueOf(response.body()));
    }
    public String createInvoicePutString(Double amt) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "false");
        stringMap.put("amount", amt.toString());
        stringMap.put("memo", "Sovereign Craft");
        return gson.toJson(stringMap);
    }
    public String createlnurlpPutString(String description, int min, int max, String comment, String username) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("description", description);
        stringMap.put("min", String.valueOf(min));
        stringMap.put("max", String.valueOf(max));
        stringMap.put("fiat_base_multiplier", "100");
        stringMap.put("username", username.toLowerCase());
        return gson.toJson(stringMap);
    }
    public String createlnurlwPutString(Double amt) {
        Gson gson = new Gson();
        int qty = amt.intValue();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("title", "Sovereign Craft Withdraw");
        stringMap.put("min_withdrawable", String.valueOf(qty));
        stringMap.put("max_withdrawable", String.valueOf(qty));
        stringMap.put("uses", "1");
        stringMap.put("wait_time", "1");
        stringMap.put("is_unique", "true");
        return gson.toJson(stringMap);
    }
    // ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    /*
    public String userPutString(UUID uuid){
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("admin_id", ConfigHandler.getAdminUser());
        stringMap.put("wallet_name", String.valueOf(uuid));
        stringMap.put("user_name", String.valueOf(uuid));
        return gson.toJson(stringMap);
    }
    */
    public String userPutString(UUID uuid) {
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("name", String.valueOf(uuid)); // Only "name" is required for LNBits 1.0.0 user creation
        return gson.toJson(stringMap);
    }
    //create an invoice and return the bolt 11 ln string
    public void createlnurlp(UUID uuid, String description, int min, int max, String comment, String username) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lnurlpCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(createlnurlpPutString(description, min, max, comment, username)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
          client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public String createlnurlw(UUID uuid, Double amt) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lnurlwCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(createlnurlwPutString(amt)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        String responseJSON = response.body();
        return (String) json.JSON2Map(responseJSON).get("lnurl");
    }
    public String createInvoice(UUID uuid, Double amount) {
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
        return (String) json.JSON2Map(responseJSON).get("payment_request");
    }
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    public Boolean userDelete(UUID uid){
        Map user = getUser(uid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd + "/" + user.get("id") ))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
        }
        return true;
    }
    */
    // ===== REFACTORED FOR LNBits 1.0.0 =====
    // Deletes a user by UUID (name).
    public Boolean userDelete(UUID uuid) {
        try {
            Map<String, Object> user = getUser(uuid);  // Uses refactored getUser(UUID)
            String userId = (String) user.get("id");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(usersCmd + "/" + userId))  // DELETE /users/api/v1/user/{user_id}
                    .headers("X-Api-Key", ConfigHandler.getAdminKey())
                    .version(HttpClient.Version.HTTP_1_1)
                    .DELETE()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            
            return true;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to delete user: " + uuid, e);
        }
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
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    This method posted to /extensions with deprecated usermanager-based headers (usr, action, extension).
    LNBits 1.0.0 uses PUT requests on /api/v1/extension/{ext_id}/{enable|disable} without requiring a payload or user context.
    Therefore, the original implementation is no longer valid.

    @SneakyThrows
    public void extension(UUID uuid, String extension, Boolean enable) {
        String action = "";
        if (enable){
            action = "enable";
        } else { 
            action = "disable"; 
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(extensionsCmd))
                .headers("usr", (String) getUser(uuid).get("admin"), action, extension)  // Deprecated usermanager headers
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(processInvoicePutString(extensionsCmd)))  // Incorrect payload for extension
                .build();
        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    */
    // ===== REFACTORED FOR LNBits 1.0.0 =====
    // Enables or disables an extension for the server.
    @SneakyThrows
    public void extension(String extension, Boolean enable) {
        String action = enable ? "enable" : "disable";
        String url = extensionsCmd + "/" + extension + "/" + action;  // PUT /api/v1/extension/{ext_id}/enable or disable
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Extension"))
                .version(HttpClient.Version.HTTP_1_1)
                .PUT(HttpRequest.BodyPublishers.noBody())  // No payload needed
                .build();
        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public boolean createWallet(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(userPutString(uuid)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (SCEconomy.playerAdminKey.containsKey(uuid)){
                SCEconomy.playerAdminKey.remove((uuid).toString());
            }
            if (SCEconomy.playerInKey.containsKey(uuid)){
                SCEconomy.playerInKey.remove((uuid).toString());
            }
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }

    //Get all users' LNBits account details
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
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
    */
    // ===== REFACTORED FOR LNBits 1.0.0 =====
    // LNBits 1.0.0 returns a raw list of users, so we directly return List<Map>.
    public List<Map<String, Object>> getUsers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(usersCmd))  // https://<host>/users/api/v1/user
                    .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (ConfigHandler.getDebug()) {
                SCEconomy.getInstance().getLogger().info("[DEBUG] getUsers() response: " + response.body());
            }
            // Parses response as a List<Map<String, Object>> using JSON2ListOfMaps.
            Map<String, Object> parsedResponse = json.JSON2Map(response.body());
            return (List<Map<String, Object>>) parsedResponse.get("data");  // Extract the array from "data"
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    // ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    /* 
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
    */
    public Map<String, Object> getUser(UUID uuid) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(usersCmd + "?all_wallets=true"))
                    .headers("Authorization", "Bearer " + ConfigHandler.getBearerToken("Users"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (ConfigHandler.getDebug()) {
                SCEconomy.getInstance().getLogger().info("[DEBUG] getUser() response: " + response.body());
            }
            // ===== PARSES USER LIST FROM LNBits 1.0.0 RESPONSE USING JSON2ListOfMaps =====
            List<Map<String, Object>> users = json.JSON2ListOfMaps(response.body());
            
            // ===== ITERATES TO FIND USER MATCHING UUID =====
            for (Map<String, Object> user : users) {
                if (String.valueOf(uuid).equals(user.get("name"))) {
                    return user;
                }
            }
            throw new NullPointerException("User not found for UUID: " + uuid);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    //Get the details for the users wallet used in game. Used for balance inquiry
    // ===== VERIFIED FOR LNBits 1.0.0 =====
    // Retrieves and caches the wallet's Invoice Key (inkey).
    public String getWalletinkey(UUID uuid) {
        if (SCEconomy.playerInKey.containsKey(uuid)){
            return SCEconomy.playerInKey.get(uuid).toString();
        }
        Map<String, Object> wallet = getWallet(uuid);  // getWallet(UUID) uses refactored user-based lookup
        SCEconomy.playerInKey.put(uuid, (String) wallet.get("inkey"));
        return SCEconomy.playerInKey.get(uuid).toString();
    }

    // ===== VERIFIED FOR LNBits 1.0.0 =====
    // Retrieves and caches the wallet's Admin Key (adminkey).
    public String getWalletAdminKey(UUID uuid) {
        if (SCEconomy.playerAdminKey.containsKey(uuid)){
            return SCEconomy.playerAdminKey.get(uuid).toString();
        }
        Map<String, Object> wallet = getWallet(uuid);  // getWallet(UUID) uses refactored user-based lookup
        SCEconomy.playerAdminKey.put(uuid, (String) wallet.get("adminkey"));
        return SCEconomy.playerAdminKey.get(uuid).toString();
    }
    public Map<String, Object> getWalletDetail(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(walletCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
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
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    public Map getWallet(UUID uuid) {
        Map map = getWallets();
        List wallets = (List) map.get("wallets");
        for (Object currentWallet : wallets){
            Map wallet = (Map) currentWallet;
            if (String.valueOf(uuid).equals((String) wallet.get("name"))){
                if (String.valueOf(getUser(uuid).get("id")).equals((String) wallet.get("user"))){
                    return wallet;
                }
            }
        }
        return (new HashMap<>());
    }
    */
    // ===== REFACTORED FOR LNBits 1.0.0 =====
    // Retrieves the first wallet associated with the user.
    @SuppressWarnings("unchecked")
    public Map<String, Object> getWallet(UUID uuid) {
        Map<String, Object> user = getUser(uuid);  // Uses refactored getUser(UUID)
        List<Map<String, Object>> wallets = (List<Map<String, Object>>) user.get("wallets");  // Extract wallets from user
        
        if (wallets == null || wallets.isEmpty()) {
            throw new NullPointerException("No wallet found for user: " + uuid);
        }
        
        return wallets.get(0);  // Assuming one wallet per user (modify if multiple needed)
    }
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    public Boolean hasAccount(UUID uuid) {
        Map wallet = getWallet(uuid);
        if (wallet != null && wallet.containsKey("user")) {
            return true;
        }
        if (SCEconomy.playerAdminKey.containsKey(uuid)){
            SCEconomy.playerAdminKey.remove((uuid).toString());
        }
        if (SCEconomy.playerInKey.containsKey(uuid)){
            SCEconomy.playerInKey.remove((uuid).toString());
        }
        return false;
    }
    */
    // ===== REFACTORED FOR LNBits 1.0.0 =====
    // Checks if a wallet exists for the given UUID (user name).
    public Boolean hasAccount(UUID uuid) {
        try {
            getWallet(uuid);  // Will throw if no wallet
            return true;      // Wallet exists
        } catch (NullPointerException e) {
            // Clear cached keys if no wallet found
            SCEconomy.playerAdminKey.remove(uuid);
            SCEconomy.playerInKey.remove(uuid);
            return false;
        }
    }
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
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
    */
    public boolean withdraw(UUID uuid, Double amount) {
        return processInvoice(uuid, createInvoice( ConfigHandler.getServerUUID(), amount));
    }
    public boolean deposit(UUID uuid, Double amount) {
        Boolean payment = processInvoice(ConfigHandler.getServerUUID(), createInvoice(uuid, amount));
        if (payment){
            return true;
        }
        return false;
    }
    public Double getBalance(UUID uuid) {
        Map<String, Object> map = getWalletDetail(uuid);
        Double bal =  (Double) map.get("balance") / 1000;
        return bal;
    }
    public String getBalanceString(UUID uuid) {
        return "⚡" + SCEconomy.getEco().numberFormat(getBalance(uuid));
    }
    public String numberFormat(Double number){
        DecimalFormat df = new DecimalFormat("###,###,##0.000");
        df.setGroupingSize(3);
        return df.format(number);
    }
    public String numberFiatFormat(Double number){
        DecimalFormat df = new DecimalFormat("###,###,##0.00");
        df.setGroupingSize(3);
        return df.format(number);
    }
    public Boolean has(UUID uuid, Double amt) {
        if (getBalance(uuid) >= amt) {
            return true;
        } return false;
    }
    /* ===== COMMENTED OUT DUE TO LNBits 1.0.0 API UPDATE =====
    public boolean createAccount(UUID uuid) {
        Boolean account = createWallet(uuid);
        if (account){
            Boolean deposit = deposit(uuid, ConfigHandler.getStartingBalance());
            if(deposit){
                return true;
            }
            return true;
        }
        return false;
    }
    */
    // ===== REFACTORED FOR LNBits 1.0.0 =====
    // Creates a user and wallet, and deposits the starting balance.
    public boolean createAccount(UUID uuid) {
        boolean accountCreated = createWallet(uuid);  // Uses refactored createWallet()
        if (accountCreated) {
            // Deposit starting balance if account was successfully created
            boolean depositSuccessful = deposit(uuid, ConfigHandler.getStartingBalance());
            return depositSuccessful;
        }
        return false;
    }
}
