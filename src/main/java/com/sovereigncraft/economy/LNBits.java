package com.sovereigncraft.economy;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;
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
    public static String usersCmd = "https://" + ConfigHandler.getHost() + "/usermanager/api/v1/users";
    public static String invoiceCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/payments";
    public static String lnurlpCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/lnurlp/api/v1/links";
    public static String lnurlwCmd = "https://" + ConfigHandler.getHost() + "/withdraw/api/v1/links";
    public static String userWalletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/usermanager/api/v1/wallets";
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
        Map lnurl = convertLnaddrtoLnurl(lnaddr);
        if (lnurl.containsKey("callback")) {
            if (!lnurl.get("description").toString().isEmpty()) {
                player.sendMessage("Sending to: " + lnurl.get("description").toString());
            }
            Map paid = payLnurl(player.getUniqueId(), lnurl, amount);
            if (paid.containsKey("payment_hash")) player.sendMessage("Payment Successful");
            else player.sendMessage("Payment Failed");
        } else player.sendMessage("Invalid Lightning address");
    }
    public Map convertLnaddrtoLnurl (String lnaddr) {
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
    public String payLnurlPostString(Map lnurl, Double amount) {
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
    public Map payLnurl(UUID uuid, Map lnurl, Double amount) {
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
    public String userPutString(UUID uuid){
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("admin_id", ConfigHandler.getAdminUser());
        stringMap.put("wallet_name", String.valueOf(uuid));
        stringMap.put("user_name", String.valueOf(uuid));
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
        return (String) json.JSON2Map(responseJSON).get("bolt11");
    }
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
    @SneakyThrows
    public void extension(UUID uuid, String extension, Boolean enable) {
        String action = "";
        if (enable){
            action = "enable";
        } else { action = "disable"; }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(extensionsCmd))
                .headers("usr", (String) getUser(uuid).get("admin"), action, extension)
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(processInvoicePutString(extensionsCmd)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public boolean createWallet(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
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
        //check if Invoice Key is stored in the hashmap
        if (SCEconomy.playerInKey.containsKey(uuid)){
            // Bukkit.getLogger().info("using cached key");
            return SCEconomy.playerInKey.get(uuid).toString();
        }
        Map wallet = getWallet(uuid);
        SCEconomy.playerInKey.put(uuid,(String) wallet.get("inkey"));
        return SCEconomy.playerInKey.get(uuid).toString();
        //return (String) wallet.get("inkey");
    }
    public String getWalletAdminKey(UUID uuid) {
        //check if the Admin Key is stored in the hashmap
        if (SCEconomy.playerAdminKey.containsKey(uuid)){
            // Bukkit.getLogger().info("using cached key");
            //check if admin key doesn't actually return a wallet and if it doesn't remove the entry
            return SCEconomy.playerAdminKey.get(uuid).toString();
        }
        // Bukkit.getLogger().info("finding admin key");
        Map wallet = getWallet(uuid);
        SCEconomy.playerAdminKey.put(uuid,(String) wallet.get("adminkey"));
        return SCEconomy.playerAdminKey.get(uuid).toString();
    }
    public Map getWalletDetail(UUID uuid) {
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
        } return (new HashMap<>());
    }
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
        Map map = getWalletDetail(uuid);
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
    public boolean createAccount(UUID uuid) {
        Boolean account = createWallet(uuid);
        if (account){
            Boolean deposit = deposit(uuid, ConfigHandler.getStartingBalance());
            if(deposit){
                return true;
            }
            return true;
        }return false;

    }
}
