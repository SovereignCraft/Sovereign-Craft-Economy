package com.sovereigncraft.economy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class LNBits {
    private final String host;
    private final String adminKey;
    private final String accessToken;
    private final String bedrockPrefix;
    private final String lnbitsBedrockSuffix;
    private final double startingBalance;
    private final UUID serverUUID;
    private final PlayerBridge playerBridge;
    private final Logger logger;

    private final String extensionsCmd;
    private final String usersCmd;
    private final String userV1Cmd;
    private final String invoiceCmd;
    private final String lnurlpCmd;
    private final String lnurlwCmd;
    private final String walletCmd;
    private final String currenciesCmd;
    private final String convertCmd;
    private final String lnurlscanCmd;
    private final String paylnurlCmd;

    private final Map<UUID, String> playerInKeyCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerAdminKeyCache = new ConcurrentHashMap<>();


    private static final Gson GSON = new Gson();

    public LNBits(String host, int port, String adminKey, String accessToken, String bedrockPrefix, String lnbitsBedrockSuffix, double startingBalance, UUID serverUUID, PlayerBridge playerBridge, Logger logger) {
        this.host = host;
        this.adminKey = adminKey;
        this.accessToken = accessToken;
        this.bedrockPrefix = bedrockPrefix;
        this.lnbitsBedrockSuffix = lnbitsBedrockSuffix;
        this.startingBalance = startingBalance;
        this.serverUUID = serverUUID;
        this.playerBridge = playerBridge;
        this.logger = logger;

        this.extensionsCmd = "https://" + host + "/api/v1/extension/";
        this.usersCmd = "https://" + host + "/usermanager/api/v1/users";
        this.userV1Cmd = "https://" + host + "/users/api/v1/user";
        this.invoiceCmd = "http://" + host + ":" + port + "/api/v1/payments";
        this.lnurlpCmd = "http://" + host + ":" + port + "/lnurlp/api/v1/links";
        this.lnurlwCmd = "https://" + host + "/withdraw/api/v1/links";
        this.walletCmd = "http://" + host + ":" + port + "/api/v1/wallet";
        this.currenciesCmd = "http://" + host + ":" + port + "/api/v1/currencies";
        this.convertCmd = "http://" + host + ":" + port + "/api/v1/conversion";
        this.lnurlscanCmd = "http://" + host + ":" + port + "/api/v1/lnurlscan/";
        this.paylnurlCmd = "https://" + host + "/api/v1/payments/lnurl";
    }

    public void clearPlayerCache(UUID uuid) {
        playerInKeyCache.remove(uuid);
        playerAdminKeyCache.remove(uuid);
    }

    private Map<String, Object> parseJsonToMap(String jsonString) {
        try {
            TypeToken<Map<String, Object>> type = new TypeToken<Map<String, Object>>() {};
            Map<String, Object> result = GSON.fromJson(jsonString, type.getType());
            if (result == null) {
                logger.warning("parseJsonToMap returned null for input: " + jsonString);
            }
            return result;
        } catch (JsonSyntaxException e) {
            logger.warning("parseJsonToMap failed to parse: " + jsonString);
            logger.warning("Exception: " + e.getMessage());
            throw e;
        }
    }

    private List<String> parseJsonToList(String jsonString) {
        String cleanedJson = jsonString.trim().replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
        try {
            TypeToken<List<String>> type = new TypeToken<List<String>>() {};
            List<String> result = GSON.fromJson(cleanedJson, type.getType());
            if (result == null) {
                logger.warning("parseJsonToList returned null for input: " + cleanedJson);
            }
            return result;
        } catch (JsonSyntaxException e) {
            logger.warning("parseJsonToList failed to parse: " + cleanedJson);
            logger.warning("Exception: " + e.getMessage());
            throw e;
        }
    }

    public Map getUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(usersCmd))
                .headers("X-Api-Key", adminKey)
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
            logger.warning("getUsers failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

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

    public Map getUserV1ByExternalId(UUID uuid) {
        String userQueryCmd = userV1Cmd + "?external_id=" + uuid.toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userQueryCmd))
                .header("accept", "application/json")
                .header("Cookie", "cookie_access_token=" + accessToken)
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
                                    logger.info("User found for UUID: " + uuid);
                                    return (Map) dataList.get(0);
                                }
                            } else if (total == 0) {
                                logger.info("No user found for UUID: " + uuid);
                                return null;
                            }
                        }
                    }
                    logger.warning("Unexpected response structure: " + cleanedBody);
                } catch (JsonSyntaxException e) {
                    logger.warning("JSON parsing failed for body: " + cleanedBody);
                    logger.warning("Exception: " + e.getMessage());
                    return null;
                }
            } else {
                logger.warning("Invalid response: Status=" + response.statusCode() + ", Body=" + rawBody);
            }
        } catch (Exception e) {
            logger.warning("Request failed: " + e.getMessage());
            return null;
        }
        return null;
    }

    public Map getWallet(UUID uuid) {
        Map user = getUserV1ByExternalId(uuid);
        if (user == null) {
            Map<String, Object> oldUser = getUser(uuid);
            if (oldUser != null) {
                if (playerBridge.isPlayerOnline(uuid)) {
                    playerBridge.sendMessage(uuid, "§eFound your existing ⚡ wallet in the old system, migrating to the new system...");
                }
                logger.info("Migrating account for " + playerBridge.getPlayerName(uuid) + " to V1");
                String userID = (String) oldUser.get("id");
                try {
                    updateUserWithDefaults(userID, uuid);
                    if (playerBridge.isPlayerOnline(uuid)) {
                        playerBridge.sendMessage(uuid, "§aWallet migration successful!");
                    }
                } catch (RuntimeException e) {
                    if (playerBridge.isPlayerOnline(uuid)) {
                        playerBridge.sendMessage(uuid, "§cFailed to migrate your ⚡ wallet. Please contact an admin.");
                    }
                    logger.warning("Migration failed for player " + playerBridge.getPlayerName(uuid) + ": " + e.getMessage());
                }
            } else {
                if (playerBridge.isPlayerOnline(uuid)) {
                    playerBridge.sendMessage(uuid, "§eYour ⚡ wallet is being created.");
                }
                Map newWallet = createWalletV1(uuid);
                if (!newWallet.isEmpty()) {
                    if (playerBridge.isPlayerOnline(uuid)) {
                        playerBridge.sendMessage(uuid, "§aYour ⚡ wallet has been created!");
                    }
                    boolean deposited = deposit(uuid, startingBalance);
                    if (!deposited) {
                        if (playerBridge.isPlayerOnline(uuid)) {
                            playerBridge.sendMessage(uuid, "§cWarning: Could not deposit starting balance. Wallet may need funding.");
                        }
                    }
                } else {
                    if (playerBridge.isPlayerOnline(uuid)) {
                        playerBridge.sendMessage(uuid, "§cFailed to create your ⚡ wallet. Please contact an admin.");
                    }
                }
            }
        }
        String userId = (String) user.get("id");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userV1Cmd + "/" + userId + "/wallet"))
                .header("accept", "application/json")
                .header("Cookie", "cookie_access_token=" + accessToken)
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
                        logger.info("getWallet for " + uuid + ": found wallets = " + wallets);
                        for (Map wallet : wallets) {
                            String name = (String) wallet.get("name");
                            if (name != null && name.equals(uuid.toString())) {
                                return wallet;
                            }
                        }
                    }
                } catch (JsonSyntaxException e) {
                    logger.warning("getWallet for " + uuid + ": JSON parsing failed, body = " + cleanedBody);
                    logger.warning("Exception: " + e.getMessage());
                }
            } else {
                logger.warning("getWallet for " + uuid + ": invalid response, status = " + response.statusCode() + ", body = " + rawBody);
            }
        } catch (Exception e) {
            logger.warning("getWallet for " + uuid + ": request failed, error = " + e.getMessage());
        }
        logger.info("getWallet for " + uuid + ": no matching wallet found, creating new wallet");
        return createWalletV1(uuid);
    }

    public Boolean hasAccount(UUID uuid) {
        return getWalletinkey(uuid) != null;
    }

    public void updateUserWithDefaults(String userId, UUID playerUUID) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", userId);
        body.put("external_id", playerUUID.toString());
        String username = playerBridge.getPlayerName(playerUUID);
        if (username.startsWith(bedrockPrefix)) {
            username = username.substring(bedrockPrefix.length()) + lnbitsBedrockSuffix;
        }
        if (playerBridge.isPlayerOnline(playerUUID)) {
            playerBridge.sendMessage(playerUUID, "your wallet will have a username of " + username + " .");
        }
        body.put("username", username);
        body.put("extensions", Arrays.asList("lndhub", "boltcards", "lnurlp", "withdraw"));
        String jsonBody = GSON.toJson(body);
        logger.info("updateUserWithDefaults: Sending request for userId=" + userId + ", body=" + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(userV1Cmd + "/" + userId))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "cookie_access_token=" + accessToken)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String rawBody = response.body() != null ? response.body() : "";
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("updateUserWithDefaults: Successfully updated user for userId=" + userId + ", response=" + rawBody);

            } else {
                logger.warning("updateUserWithDefaults: Failed to update user for userId=" + userId + ", status=" + response.statusCode() + ", response=" + rawBody);
                throw new RuntimeException("Failed to update user, status=" + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.warning("updateUserWithDefaults: Request failed for userId=" + userId + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Map createWalletV1(UUID uuid) {
        if (getUserV1ByExternalId(uuid) == null) {
            Map<String, Object> body = new LinkedHashMap<>();
            String username = playerBridge.getPlayerName(uuid);
            if (username.startsWith(bedrockPrefix)) {
                username = username.substring(bedrockPrefix.length()) + lnbitsBedrockSuffix;
            }
            body.put("username", username);
            body.put("password", UUID.randomUUID().toString());
            body.put("password_repeat", body.get("password"));
            if (playerBridge.isPlayerOnline(uuid)) {
                playerBridge.sendMessage(uuid, "your wallet will have a username of " + username + " .");
                playerBridge.sendMessage(uuid, "Your initial password for your wallet will be: " + body.get("password") + "there's no need to write it down as you can access your wallet from other commands.");
            }
            body.put("external_id", uuid.toString());
            body.put("extensions", Arrays.asList("lndhub", "boltcards", "lnurlp", "withdraw"));

            String jsonBody = GSON.toJson(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(userV1Cmd))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Cookie", "cookie_access_token=" + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    logger.warning("Failed to create user for " + playerBridge.getPlayerName(uuid) + ". Status: " + response.statusCode() + ", Body: " + response.body());
                    return new HashMap<>();
                }
            } catch (IOException | InterruptedException e) {
                logger.warning("createWalletV1 user creation failed for player " + playerBridge.getPlayerName(uuid) + ": " + e.getMessage());
                return new HashMap<>();
            }
        }
        Map<String, Object> user = getUserV1ByExternalId(uuid);
        if (user == null) {
            logger.warning("Could not find user account for " + uuid + " after creation attempt.");
            return new HashMap<>();
        }
        String userId = (String) user.get("id");

        Map<String, String> walletBody = new HashMap<>();
        walletBody.put("name", uuid.toString());
        String jsonWalletBody = GSON.toJson(walletBody);

        HttpRequest walletRequest = HttpRequest.newBuilder()
                .uri(URI.create(userV1Cmd + "/" + userId + "/wallet"))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "cookie_access_token=" + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonWalletBody))
                .build();

        HttpClient walletClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = walletClient.send(walletRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warning("Failed to create wallet for user " + userId + ". Status: " + response.statusCode() + ", Body: " + response.body());
                return new HashMap<>();
            }
        } catch (IOException | InterruptedException e) {
            logger.warning("createWalletV1 wallet creation request failed for player " + playerBridge.getPlayerName(uuid) + ": " + e.getMessage());
            return new HashMap<>();
        }

        return new HashMap<>();
    }

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

    public boolean sendLNAddress(UUID playerUUID, String lnaddr, Double amount) {
        Map lnurl = convertLnaddrtoLnurl(lnaddr, playerUUID);
        if (lnurl.containsKey("callback")) {
            if (!lnurl.get("description").toString().isEmpty()) {
                playerBridge.sendMessage(playerUUID, "Sending to: " + lnurl.get("description").toString());
            }
            Map paid = payLnurl(playerUUID, lnurl, amount);
            if (paid.containsKey("payment_hash")) playerBridge.sendMessage(playerUUID, "Payment Successful");
            else playerBridge.sendMessage(playerUUID, "Payment Failed");
        } else playerBridge.sendMessage(playerUUID, "Invalid Lightning address");
        return false;
    }

    public Map convertLnaddrtoLnurl(String lnaddr, UUID playerUUID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lnurlscanCmd + URLEncoder.encode(lnaddr)))
                .headers("X-Api-Key", getWalletAdminKey(playerUUID))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseJsonToMap(response.body());
        } catch (IOException | InterruptedException e) {
            logger.warning("convertLnaddrtoLnurl failed: " + e.getMessage());
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
            logger.warning("payLnurl failed: " + e.getMessage());
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
            logger.warning("getConversion failed: " + e.getMessage());
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
                logger.warning("Invalid response in getCurrencies: Status=" + response.statusCode() + ", Body=" + rawBody);
                return new ArrayList<>();
            }
        } catch (IOException | InterruptedException e) {
            logger.warning("getCurrencies request failed: " + e.getMessage());
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
            logger.warning("createlnurlp failed: " + e.getMessage());
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
            logger.warning("createlnurlw failed: " + e.getMessage());
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
            logger.warning("createInvoice failed: " + e.getMessage());
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
            logger.warning("processInvoice failed: " + e.getMessage());
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
                .header("Cookie", accessToken)
                .version(HttpClient.Version.HTTP_1_1)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        client.send(request, HttpResponse.BodyHandlers.ofString());
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
            logger.warning("getWalletDetail failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean withdraw(UUID uuid, Double amount) {
        return processInvoice(uuid, createInvoice(serverUUID, amount));
    }

    public boolean deposit(UUID uuid, Double amount) {
        return processInvoice(serverUUID, createInvoice(uuid, amount));
    }

    public Double getBalance(UUID uuid) {
        Map map = getWalletDetail(uuid);
        Double bal = (Double) map.get("balance") / 1000;
        return bal;
    }

    public String getBalanceString(UUID uuid) {
        return "⚡" + numberFormat(getBalance(uuid));
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
        return getBalance(uuid) >= amt;
    }

    public String getWalletinkey(UUID uuid) {
        if (playerInKeyCache.containsKey(uuid)) {
            return playerInKeyCache.get(uuid);
        }
        Map wallet = getWallet(uuid);
        String inkey = null;
        if (wallet != null && wallet.containsKey("inkey")) {
            inkey = (String) wallet.get("inkey");
        }
        playerInKeyCache.put(uuid, inkey);
        return inkey;
    }

    public String getWalletAdminKey(UUID uuid) {
        if (playerAdminKeyCache.containsKey(uuid)) {
            return playerAdminKeyCache.get(uuid);
        }
        Map wallet = getWallet(uuid);
        String adminkey = null;
        if (wallet != null && wallet.containsKey("adminkey")) {
            adminkey = (String) wallet.get("adminkey");
        }
        playerAdminKeyCache.put(uuid, adminkey);
        return adminkey;
    }
}
