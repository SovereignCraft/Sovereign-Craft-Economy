package com.sovereigncraft.economy;

import com.google.gson.*;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;

public class LNBits {
    // API URLs
    public static String extensionsCmd = "https://" + ConfigHandler.getHost() + "/api/v1/extension/";
    public static String usersCmd = "https://" + ConfigHandler.getHost() + "/usermanager/api/v1/users";
    public static String userV1Cmd = "https://" + ConfigHandler.getHost() + "/users/api/v1/user";
    public static String invoiceCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/payments";
    public static String lnurlpCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/lnurlp/api/v1/links";
    public static String lnurlwCmd = "https://" + ConfigHandler.getHost() + "/withdraw/api/v1/links";
    public static String walletCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/wallet";
    public static String currenciesCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/currencies";
    public static String convertCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/conversion";
    public static String lnurlscanCmd = "http://" + ConfigHandler.getHost() + ":" + ConfigHandler.getPort() + "/api/v1/lnurlscan/";
    public static String paylnurlCmd = "https://" + ConfigHandler.getHost() + "/api/v1/payments/lnurl";

    // Static Gson instance for efficiency
    private static final Gson GSON = new Gson();

    // JSON parsing utilities
    private Map<String, Object> parseJsonToMap(String jsonString) {
        try {
            TypeToken<Map<String, Object>> type = new TypeToken<Map<String, Object>>() {};
            Map<String, Object> result = GSON.fromJson(jsonString, type.getType());
            if (result == null) {
                Bukkit.getLogger().warning("parseJsonToMap returned null for input: " + jsonString);
            }
            return result;
        } catch (JsonSyntaxException e) {
            Bukkit.getLogger().warning("parseJsonToMap failed to parse: " + jsonString);
            Bukkit.getLogger().warning("Exception: " + e.getMessage());
            throw e;
        }
    }


    private List<String> parseJsonToList(String jsonString) {
        String cleanedJson = jsonString.trim().replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
        try {
            TypeToken<List<String>> type = new TypeToken<List<String>>() {};
            List<String> result = GSON.fromJson(cleanedJson, type.getType());
            if (result == null) {
                Bukkit.getLogger().warning("parseJsonToList returned null for input: " + cleanedJson);
            }
            return result;
        } catch (JsonSyntaxException e) {
            Bukkit.getLogger().warning("parseJsonToList failed to parse: " + cleanedJson);
            Bukkit.getLogger().warning("Exception: " + e.getMessage());
            throw e;
        }
    }

    private List<Map<String, Object>> parseJsonToListOfMaps(String jsonString) {
        String cleanedJson = jsonString.trim().replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
        try {
            TypeToken<List<Map<String, Object>>> type = new TypeToken<List<Map<String, Object>>>() {};
            List<Map<String, Object>> result = GSON.fromJson(cleanedJson, type.getType());
            if (result == null) {
                Bukkit.getLogger().warning("parseJsonToListOfMaps returned null for input: " + cleanedJson);
            }
            return result;
        } catch (JsonSyntaxException e) {
            Bukkit.getLogger().warning("parseJsonToListOfMaps failed to parse: " + cleanedJson);
            Bukkit.getLogger().warning("Exception: " + e.getMessage());
            throw e;
        }
    }

    // Get all users' LNBits account details (for migration only)
    public Map getUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawBody = response.body() != null ? response.body() : "";
            String cleanerJSON = "{ \"users\": " + rawBody + " }";
            return parseJsonToMap(cleanerJSON);
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("getUsers failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Get a user by UUID (for migration only)
    public Map getUser(UUID uuid) {
        Map map = getUsers();
        List users = (List) map.get("users");
        for (Object currentUser : users) {
            Map user = (Map) currentUser;
            if (String.valueOf(uuid).equals((String) user.get("name"))) {
                return user;
            }
        }
        return null;
    }

    // Get a user by external_id from V1 API
    public Map getUserV1ByExternalId(UUID uuid) {
        String userQueryCmd = userV1Cmd + "?external_id=" + uuid.toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userQueryCmd))
                .header("accept", "application/json")
                .header("Cookie", "cookie_access_token=" + ConfigHandler.getAccessToken())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawBody = response.body() != null ? response.body() : "";
            if (response.statusCode() == 200 && !rawBody.trim().isEmpty()) {
                String cleanedBody = rawBody.trim().replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
                try {
                    Map responseMap = parseJsonToMap(cleanedBody);
                    if (responseMap != null && responseMap.containsKey("total")) {
                        Object totalObj = responseMap.get("total");
                        if (totalObj instanceof Number) {
                            int total = ((Number) totalObj).intValue();
                            if (total == 1) {
                                List dataList = (List) responseMap.get("data");
                                if (dataList != null && !dataList.isEmpty()) {
                                    Bukkit.getLogger().info("User found for UUID: " + uuid);
                                    return (Map) dataList.get(0);
                                }
                            } else if (total == 0) {
                                Bukkit.getLogger().info("No user found for UUID: " + uuid);
                                return null;
                            }
                        }
                    }
                    Bukkit.getLogger().warning("Unexpected response structure: " + cleanedBody);
                } catch (JsonSyntaxException e) {
                    Bukkit.getLogger().warning("JSON parsing failed for body: " + cleanedBody);
                    Bukkit.getLogger().warning("Exception: " + e.getMessage());
                    return null;
                }
            } else {
                Bukkit.getLogger().warning("Invalid response: Status=" + response.statusCode() + ", Body=" + rawBody);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Request failed: " + e.getMessage());
            return null;
        }
        return null;
    }

    // Get a single wallet by UUID
    public Map getWallet(UUID uuid) {
        Map user = getUserV1ByExternalId(uuid);
        if (user == null) {
            Bukkit.getLogger().info("§cCould not find the user account with UUID: " + uuid + "Failure getting the wallet");
            return new HashMap<>();
        }
        String userId = (String) user.get("id");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userV1Cmd + "/" + userId + "/wallet"))
                .header("accept", "application/json")
                .header("Cookie", "cookie_access_token=" + ConfigHandler.getAccessToken())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawBody = response.body() != null ? response.body() : "";
            if (response.statusCode() == 200 && !rawBody.trim().isEmpty()) {
                String cleanedBody = rawBody.trim().replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
                try {
                    List<Map> wallets = GSON.fromJson(cleanedBody, List.class);
                    if (wallets != null) {
                        Bukkit.getLogger().info("getWallet for " + uuid + ": found wallets = " + wallets);
                        for (Map wallet : wallets) {
                            String name = (String) wallet.get("name");
                            if (name != null && name.equals(uuid.toString())) {
                                return wallet;
                            }
                        }
                        // Fallback: Return first wallet if no exact match
                        Bukkit.getLogger().warning("No player wallet found!");
                    }
                } catch (JsonSyntaxException e) {
                    Bukkit.getLogger().warning("getWallet for " + uuid + ": JSON parsing failed, body = " + cleanedBody);
                    Bukkit.getLogger().warning("Exception: " + e.getMessage());
                }
            } else {
                Bukkit.getLogger().warning("getWallet for " + uuid + ": invalid response, status = " + response.statusCode() + ", body = " + rawBody);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("getWallet for " + uuid + ": request failed, error = " + e.getMessage());
        }
        Bukkit.getLogger().info("getWallet for " + uuid + ": no matching wallet found, returning empty map");
        return new HashMap<>();
    }

    // Check if a user has an account
    public Boolean hasAccount(UUID uuid) {
        Map wallet = getWallet(uuid);
        return wallet != null && wallet.containsKey("user");
    }

    // Update a user with an external_id
    public void updateUserWithDefaults(String userId, Player player) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", userId);
        body.put("external_id", player.getUniqueId().toString());
        body.put("username", player.getName());
        body.put("extensions", Arrays.asList("lndhub", "boltcards", "lnurlp", "withdraw"));
        String jsonBody = GSON.toJson(body);
        Bukkit.getLogger().info("updateUserWithDefaults: Sending request for userId=" + userId + ", body=" + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userV1Cmd + "/" + userId))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "cookie_access_token=" + ConfigHandler.getAccessToken())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawBody = response.body() != null ? response.body() : "";
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Bukkit.getLogger().info("updateUserWithDefaults: Successfully updated user for userId=" + userId + ", response=" + rawBody);
            } else {
                Bukkit.getLogger().warning("updateUserWithDefaults: Failed to update user for userId=" + userId + ", status=" + response.statusCode() + ", response=" + rawBody);
                throw new RuntimeException("Failed to update user, status=" + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("updateUserWithDefaults: Request failed for userId=" + userId + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Create a user in V1
    public boolean createWalletV1(UUID uuid) {
        Player player = Bukkit.getServer().getPlayer(uuid);
        if (getUserV1ByExternalId(uuid) == null) {
            //make user account
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("username", player.getName());
            body.put("password", UUID.randomUUID().toString());
            body.put("password_repeat", body.get("password"));
            body.put("external_id", player.getUniqueId().toString());
            body.put("extensions", Arrays.asList("lndhub", "boltcards", "lnurlp", "withdraw"));

            String jsonBody = GSON.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(userV1Cmd))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Cookie", "cookie_access_token=" + ConfigHandler.getAccessToken())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    Bukkit.getLogger().warning("Failed to create user for " + player.getName() + ". Status: " + response.statusCode() + ", Body: " + response.body());
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                Bukkit.getLogger().warning("createWalletV1 user creation failed for player " + player.getName() + ": " + e.getMessage());
                return false;
            }
        }
        //create wallet for user account
        Map<String, Object> user = getUserV1ByExternalId(uuid);
        if (user == null) {
            Bukkit.getLogger().warning("Could not find user account for " + uuid + " after creation attempt.");
            return false;
        }
        String userId = (String) user.get("id");

        Map<String, String> walletBody = new HashMap<>();
        walletBody.put("name", uuid.toString());
        String jsonWalletBody = GSON.toJson(walletBody);

        HttpRequest walletRequest = HttpRequest.newBuilder()
                .uri(URI.create(userV1Cmd + "/" + userId + "/wallet"))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "cookie_access_token=" + ConfigHandler.getAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonWalletBody))
                .build();

        HttpClient walletClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = walletClient.send(walletRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Bukkit.getLogger().warning("Failed to create wallet for user " + userId + ". Status: " + response.statusCode() + ", Body: " + response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("createWalletV1 wallet creation request failed for player " + player.getName() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    // JSON construction methods
    public String processInvoicePutString(String invoice) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "true");
        stringMap.put("bolt11", invoice);
        return GSON.toJson(stringMap);
    }

    public String convertPostString(String from, Double amount, String to) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("from_", from.toLowerCase());
        stringMap.put("amount", String.valueOf(amount));
        stringMap.put("to", to);
        return GSON.toJson(stringMap);
    }

    public void sendLNAddress(Player player, String lnaddr, Double amount) {
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

    public Map convertLnaddrtoLnurl(String lnaddr) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lnurlscanCmd + URLEncoder.encode(lnaddr)))
                .headers("X-Api-Key", ConfigHandler.getAdminKey())
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseJsonToMap(response.body());
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("convertLnaddrtoLnurl failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String payLnurlPostString(Map lnurl, Double amount) {
        int milliAmount = (int) Math.floor(amount * 1000);
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("description_hash", lnurl.get("description_hash").toString());
        stringMap.put("callback", lnurl.get("callback").toString());
        stringMap.put("amount", String.valueOf(milliAmount));
        stringMap.put("description", lnurl.get("description").toString());
        stringMap.put("unit", "sat");
        return GSON.toJson(stringMap);
    }

    public Map payLnurl(UUID uuid, Map lnurl, Double amount) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paylnurlCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(payLnurlPostString(lnurl, amount)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseJsonToMap(response.body());
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("payLnurl failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Double getConversion(String from, Double amount, String to) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(convertCmd))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(convertPostString(from.toLowerCase(), amount, to.toLowerCase())))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (Double) parseJsonToMap(response.body()).get(to);
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("getConversion failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<String> getCurrencies() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(currenciesCmd))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawBody = response.body() != null ? response.body() : "";
            if (response.statusCode() == 200 && !rawBody.trim().isEmpty()) {
                return parseJsonToList(rawBody);
            } else {
                Bukkit.getLogger().warning("Invalid response in getCurrencies: Status=" + response.statusCode() + ", Body=" + rawBody);
                return new ArrayList<>();
            }
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("getCurrencies request failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String createInvoicePutString(Double amt) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("out", "false");
        stringMap.put("amount", amt.toString());
        stringMap.put("memo", "Sovereign Craft");
        return GSON.toJson(stringMap);
    }

    public String createlnurlpPutString(String description, int min, int max, String comment, String username) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("description", description);
        stringMap.put("min", String.valueOf(min));
        stringMap.put("max", String.valueOf(max));
        stringMap.put("fiat_base_multiplier", "100");
        stringMap.put("username", username.toLowerCase());
        return GSON.toJson(stringMap);
    }

    public String createlnurlwPutString(Double amt) {
        int qty = amt.intValue();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("title", "Sovereign Craft Withdraw");
        stringMap.put("min_withdrawable", String.valueOf(qty));
        stringMap.put("max_withdrawable", String.valueOf(qty));
        stringMap.put("uses", "1");
        stringMap.put("wait_time", "1");
        stringMap.put("is_unique", "true");
        return GSON.toJson(stringMap);
    }

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
            Bukkit.getLogger().warning("createlnurlp failed: " + e.getMessage());
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
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (String) parseJsonToMap(response.body()).get("lnurl");
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("createlnurlw failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String createInvoice(UUID uuid, Double amount) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invoiceCmd))
                .headers("X-Api-Key", getWalletinkey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(createInvoicePutString(amount)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (String) parseJsonToMap(response.body()).get("bolt11");
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("createInvoice failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

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
            Bukkit.getLogger().warning("processInvoice failed: " + e.getMessage());
            return false;
        }
    }

    @SneakyThrows
    public void extension(UUID uuid, String extension, Boolean enable) {
        Map user = getUserV1ByExternalId(uuid);
        if (user == null) {
            throw new RuntimeException("User not found for UUID: " + uuid);
        }
        String userId = (String) user.get("id");
        String action = enable ? "enable" : "disable";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(extensionsCmd + extension + "/" + action + "?usr=" + userId))
                .header("accept", "application/json")
                .header("Cookie", ConfigHandler.getCookie())
                .version(HttpClient.Version.HTTP_1_1)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public boolean createAccount(UUID uuid) {
        Player player = Bukkit.getServer().getPlayer(uuid);
        if (player == null) {
            Bukkit.getLogger().warning("createAccount failed: No player found for UUID " + uuid);
            return false;
        }
        Boolean account = createWalletV1(player.getUniqueId());
        if (account) {
            Boolean deposit = deposit(uuid, ConfigHandler.getStartingBalance());
            if (deposit) {
                return true;
            }
            return true;
        }
        return false;
    }

    public Map getWalletDetail(UUID uuid) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(walletCmd))
                .headers("X-Api-Key", getWalletAdminKey(uuid))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseJsonToMap(response.body());
        } catch (IOException | InterruptedException e) {
            Bukkit.getLogger().warning("getWalletDetail failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean withdraw(UUID uuid, Double amount) {
        return processInvoice(uuid, createInvoice(ConfigHandler.getServerUUID(), amount));
    }

    public boolean deposit(UUID uuid, Double amount) {
        Boolean payment = processInvoice(ConfigHandler.getServerUUID(), createInvoice(uuid, amount));
        if (payment) {
            return true;
        }
        return false;
    }

    public Double getBalance(UUID uuid) {
        Map map = getWalletDetail(uuid);
        Double bal = (Double) map.get("balance") / 1000;
        return bal;
    }

    public String getBalanceString(UUID uuid) {
        return "⚡" + SCEconomy.getEco().numberFormat(getBalance(uuid));
    }

    public String numberFormat(Double number) {
        DecimalFormat df = new DecimalFormat("###,###,##0.000");
        df.setGroupingSize(3);
        return df.format(number);
    }

    public String numberFiatFormat(Double number) {
        DecimalFormat df = new DecimalFormat("###,###,##0.00");
        df.setGroupingSize(3);
        return df.format(number);
    }

    public Boolean has(UUID uuid, Double amt) {
        if (getBalance(uuid) >= amt) {
            return true;
        }
        return false;
    }

    public String getWalletinkey(UUID uuid) {
        if (SCEconomy.playerInKey.containsKey(uuid)) {
            return SCEconomy.playerInKey.get(uuid).toString();
        }
        Map wallet = getWallet(uuid);
        SCEconomy.playerInKey.put(uuid, (String) wallet.get("inkey"));
        return SCEconomy.playerInKey.get(uuid).toString();
    }

    public String getWalletAdminKey(UUID uuid) {
        if (SCEconomy.playerAdminKey.containsKey(uuid)) {
            return SCEconomy.playerAdminKey.get(uuid).toString();
        }
        Map wallet = getWallet(uuid);
        SCEconomy.playerAdminKey.put(uuid, (String) wallet.get("adminkey"));
        return SCEconomy.playerAdminKey.get(uuid).toString();
    }
}
