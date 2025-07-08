## 1. Study the OAuth mechanism

- `OAuth` is an essential part of modern web development and consists of a series of steps by which public resources are accessed by third-party software without exposing or storing the user's private information. This increases security and prevents critical information leaks

### Key Characteristics of OAuth 2.0

- **Authorization and (partly) authentication**: OAuth is designed to grant access to resources, but only after the user has been recognized by providing his credentials
- **Token-based**: Instead of using usernames and passwords, OAuth issues **access tokens** that allow apps to access resources securely
- **Role-based flow**:
    - **Resource Owner**: The user who owns the data, if a person called an end-user
    - **Client**: The application requesting access to the user’s data
    - **Authorization Server**: Issues access tokens after user authorization
    - **Resource Server**: Hosts the protected data
- **Granular permissions**: The user can control exactly what data or actions the app can access (via scopes)
- **Temporary access**: Access tokens are short-lived by design, minimizing damage in case of compromise
- **Refresh tokens** (optional): Can be used to get new access tokens without user involvement
- **Redirect-based**: The flow usually involves redirecting the user’s browser to the authorization server and then back to the client app with a code
- **Standardized protocol**: OAuth 2.0 is a widely adopted open standard (RFC 6749) and supported by most modern identity providers, including Meta (Facebook), Google, GitHub, etc.

## 2. The OAuth 2.0 workflow 

- **Step 1** The **User** enters the URL of the Website and then chooses a login method 
- **Step 2** After a method was chosen, the user is redirected to the selected **Authorization Server** (Facebook, Google, etc.) where he securely enters his credentials
- **Step 3** When the **Resource Owner** is recognized and granted access, an authorization code is emitted and sent through the redirect URI provided
- **Step 4** **Client** then receives it and will call an API to get a temporal **Access Token** from the **Authorization Server**
- **Step 5** Once the **Access Token** reached the client, he can use it to access public information from the **Resource Server** 
- **Step 6 (optional)** Together with the **Access Token** , the **Client** may be provided with a **Refresh Token** which will be used in the case the token expires to get a new one 

![](/Images/oauth2.PNG)


## 3. Homemade OAuth 2.0 guideline

### 3.1 Build a Java Spring Application

- Create a new Java Spring project built on Maven and include the following dependencies for the ***Rest Template*** used for HTTP Requests:

```xml
<!-- Required for RestTemplate -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- JSON binding -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```
- Also, to hide precious information, the ***Dotenv*** library will be included to extract secret keys and tokens from the ***.env*** file 

```xml 
<!-- The Dotenv dependency -->
<dependency>
  <groupId>io.github.cdimascio</groupId>
  <artifactId>java-dotenv</artifactId>
  <version>5.2.2</version>
</dependency>
```
- Create a **Controller** class or even 2 to separate the logic of getting and storing the `Access Token` and the other to retrieve public information, **Business Service** and other entities helpful for data transfer

### 3.2 Create a Meta Application

- To use Facebook OAuth 2.0 in your application, we need to create a Meta Developer App:

#### Step 1: Create a Developer Account
- Go to [https://developers.facebook.com](https://developers.facebook.com)
- Log in with a Facebook account
- Accept the terms and create the developer account

#### Step 2: Create a New App
- In the top-right corner, click on **"My Apps"** → **"Create App"**
- Select **"Consumer"** as the app type (for Facebook Login)
- Click **"Next"**
- Enter the following:
  - **App name** (e.g., `MyBasicOAuthApp`)
  - **Contact email**
- Click **"Create App"**
- Complete the security check if prompted

#### Step 3: Add Facebook Login Product
- After creating the app, you will be taken to the App Dashboard
- In the left sidebar, scroll down and click **Use Cases**
- Find **"Facebook Login"** and click **"Set Up"**

#### Step 4: Get App Credentials
- In the sidebar, go to **Settings → Basic**
- Copy the **App ID** and **App Secret**
- Use these in your Spring Boot configuration (e.g., `.env` or `application.properties`)

#### Step 5: (Optional) Add Valid OAuth Redirect URI
- If prompted, or if moving to production:
  - Go to **Facebook Login → Settings**
  - Add the OAuth redirect URI:
    ```
    http://localhost:8080/login/oauth2/code/facebook
    ```
  - This step is optional in development mode for localhost, but **required** in live mode

### 3.3 Build a `Login Controller`

- This component will be used to grant the Client the **Access Token** to retrieve information about his public profile
- Here mainly two endpoints are necessary: 
  - `meta-redirect` which redirects the user to the facebook login form, containing parameters like `client_id`, `redirect_uri`, `scope` and `response_type`
  - `callback` which is the **redirect_uri** sent previously and which receives an **Authorization Code** if login successful and further performs an HTTP request to the 
  ```
  https://graph.facebook.com/v23.0/oauth/access_token
  ```
- These two APIs help us receive an **Access Token** which we will store in our browser as a `HTTP Cookie`, which is a special kind of cookie only available when performing requests is hidden from any malicious `JavaScript` that tries to find essential cookies

### 3.4 Build an `Application Controller`

- Here will be built the APIs that calls the public 
 ```
https://graph.facebook.com/me
 ```
- For each endpoint we will extract the `Cookies` from the `HttpServletRequest` and take the one containing the `Access Token` which will be used in the HTTP request to Meta

From now on we could imagine and build all kinds of functionalities once we got the Access Token
