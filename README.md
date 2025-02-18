# Arhitecturi-orientate-pe-servicii-
Service Oriented Architecture
# Securing a REST API

Securing a REST API is essential to prevent unauthorized access, data breaches, and malicious attacks. This tutorial demonstrates methods to secure a REST API built using Flask and FastAPI, including authentication, authorization, and rate limiting.

## Installing Required Modules
Run the following commands in your terminal to install the necessary modules:

```sh
pip install flask pyjwt flask-jwt-extended flask-limiter secrets
```

## Introduction
Below is a simple Flask application that creates a web server running on port 1610. It defines a public endpoint (`/public`) that returns a JSON response.

```python
from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/public', methods=['GET'])
def public_route():
    return jsonify({"message": "This is a public endpoint"})

if __name__ == '__main__':
    app.run(debug=True, port=1610)
```

Save this code as `BasicApi.py` and run it using:

```sh
python BasicApi.py
```

## Authentication with API Keys
API keys provide a simple authentication mechanism. Use the following script (`Secret.py`) to generate an API key:

```python
import secrets
api_key = secrets.token_hex(32)
print("Generated API Key:", api_key)
```

### Protecting an Endpoint with API Key Authentication

The following Flask code secures an API endpoint (`/secure`) by requiring an API key in the request headers:

```python
from flask import Flask, request, jsonify

app = Flask(__name__)
API_KEY = "your_generated_api_key_here"

@app.route('/secure', methods=['GET'])
def secure_route():
    key = request.headers.get('x-api-key')
    if key != API_KEY:
        return jsonify({"error": "Unauthorized"}), 401
    return jsonify({"message": "Access granted"}), 200

if __name__ == '__main__':
    app.run(debug=True, port=1610)
```

## Authentication with JWT (JSON Web Token)
JWT is a widely used authentication method for securing APIs.

### Creating and Validating JWT Tokens
The following code (`JWTApi.py`) implements JWT-based authentication:

```python
import jwt
import datetime
from flask import Flask, jsonify
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity, get_jwt

SECRET_KEY = "your_secret_key_here"

app = Flask(__name__)
app.config["JWT_SECRET_KEY"] = SECRET_KEY
jwt = JWTManager(app)

@app.route('/token', methods=['GET'])
def get_token():
    token = create_access_token(identity="user", additional_claims={"role": "user"}, expires_delta=datetime.timedelta(hours=1))
    return jsonify({"token": token})

if __name__ == '__main__':
    app.run(debug=True, port=1620)
```

### Extracting the Current User
Authenticated users can retrieve their identity from a JWT token:

```python
@app.route('/protected', methods=['GET'])
@jwt_required()
def protected_route():
    current_user = get_jwt_identity()
    return jsonify({"message": "Token is valid", "user": current_user}), 200
```

## Role-Based Access Control (RBAC)
RBAC ensures that only authorized users can access certain resources.

```python
user_roles = {
    "admin": ["read", "write", "delete"],
    "user": ["read"]
}

@app.route('/admin', methods=['GET'])
@jwt_required()
def admin_route():
    current_user_role = get_jwt()['role']
    if "write" not in user_roles.get(current_user_role, []):
        return jsonify({"error": "Unauthorized access"}), 403
    return jsonify({"message": "Admin access granted"}), 200
```

## Conclusion
By implementing API key authentication, JWT authentication, role-based access control, and rate limiting, you can significantly enhance the security of your REST API. Always ensure that your API is protected against unauthorized access and potential threats.

