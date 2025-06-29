# LNBits 1.0.0 - User Management API Reference

This document describes the API endpoints available under the `/users/api/v1` namespace in LNBits 1.0.0. These endpoints are responsible for user management and associated wallet operations.

All requests require a Bearer Token for authentication with appropriate ACL permissions.

---

## Endpoints

### - Get All Users

* **GET** `/users/api/v1/user`

* Retrieves a paginated list of users.

* Query Parameters:

  * `usr`: string (UUID)
  * `limit`: integer (default: 100)
  * `offset`: integer (default: 0)
  * `sortby`: string
  * `direction`: string — asc or desc
  * `search`: string — general text search
  * `email`: string — filter/search
  * `user`: string — filter/search
  * `username`: string — filter/search
  * `pubkey`: string — filter/search
  * `wallet_id`: string — filter/search
  * `cookie_access_token`: string (cookie)

* Response:

```json
{
  "data": [
    "string"
  ],
  "total": 0
}
```

### - Create User

* **POST** `/users/api/v1/user`
* Creates a new LNBits user.
* Body (JSON):

```json
{
  "id": "string",
  "email": "string",
  "username": "string",
  "password": "stringst",
  "password_repeat": "stringst",
  "pubkey": "string",
  "extensions": [
    "string"
  ],
  "extra": {
    "email_verified": false,
    "first_name": "string",
    "last_name": "string",
    "display_name": "string",
    "picture": "string",
    "provider": "lnbits"
  }
}
```

* Response:

```json
{
  "id": "string",
  "email": "string",
  "username": "string",
  "password": "stringst",
  "password_repeat": "stringst",
  "pubkey": "string",
  "extensions": [
    "string"
  ],
  "extra": {
    "email_verified": false,
    "first_name": "string",
    "last_name": "string",
    "display_name": "string",
    "picture": "string",
    "provider": "lnbits"
  }
}
```

### - Get User by ID

* **GET** `/users/api/v1/user/{user_id}`
* Retrieves a specific user by their internal LNBits ID.
* Parameters:

  * `user_id`: string (path)
  * `usr`: string (UUID, query)
  * `cookie_access_token`: string (cookie)
* Response:

```json
{
  "id": "string",
  "created_at": "2025-06-29T23:18:03.559Z",
  "updated_at": "2025-06-29T23:18:03.559Z",
  "email": "string",
  "username": "string",
  "pubkey": "string",
  "extensions": [],
  "wallets": [],
  "admin": false,
  "super_user": false,
  "has_password": false,
  "extra": {
    "email_verified": false,
    "provider": "lnbits"
  }
}
```

### - Update User

* **PUT** `/users/api/v1/user/{user_id}`
* Updates user details.
* Body (JSON):

```json
{
  "username": "string",
  "email": "string (optional)"
}
```

### - Delete User

* **DELETE** `/users/api/v1/user/{user_id}`
* Deletes a user from the system.

### - Reset Password

* **PUT** `/users/api/v1/user/{user_id}/reset_password`
* Resets the user’s password.

### - Admin Permissions

* **GET** `/users/api/v1/user/{user_id}/admin`
* Toggles admin privileges for a user.

### - Get Wallets for User

* **GET** `/users/api/v1/user/{user_id}/wallet`
* Lists all wallets associated with the user.

### - Create Wallet for User

* **POST** `/users/api/v1/user/{user_id}/wallet`
* Creates a new wallet for the given user.
* Body (JSON):

```json
{
  "name": "string"
}
```

### - Reactivate Soft Deleted Wallet

* **PUT** `/users/api/v1/user/{user_id}/wallet/{wallet}/undelete`
* Restores a soft-deleted wallet.

### - Delete Wallet

* **DELETE** `/users/api/v1/user/{user_id}/wallet/{wallet}`
* First call soft-deletes the wallet.
* Second call permanently deletes it.

### - Manually Update Wallet Balance

* **PUT** `/users/api/v1/balance`
* Directly updates the balance for a wallet (admin-only).
* Body (JSON):

```json
{
  "wallet_id": "string",
  "amount": number
}
```

---

## Authentication

* Each request must use an HTTP Bearer Token with sufficient access rights (ACL) to the `/users` scope:

```
Authorization: Bearer <token>
```

---

## Notes

* `username` should be unique and under 20 characters.
* Wallet operations are scoped per user.
* All timestamps are in UTC.
* GET `/users/api/v1/user?search=...` is supported.
* Use hashed UUIDs (e.g. `mc_<sha256>`) for Minecraft integration.
* Most APIs return `{ "data": [...] }` format for lists.

---

For more details, refer to the [LNBits GitHub](https://github.com/lnbits/lnbits) or your own server’s OpenAPI docs if exposed.
