import json
import os
from datetime import timedelta

import pika
from flask import Flask, request, jsonify
from flask_bcrypt import Bcrypt
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity, JWTManager
from pymongo import MongoClient
from flask_cors import CORS  # Import CORS

app = Flask(__name__)

# Enable CORS with specific options
CORS(app, origins="http://localhost:4200", methods=["GET", "POST", "PUT", "DELETE"], allow_headers=["Content-Type", "Authorization"])

bcrypt = Bcrypt(app)

client = MongoClient("mongodb://localhost:27017/")
db = client["authDatabase"]
users_collection = db["users"]

app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET", "p9JcX3e6sD9GyVQJ5O7nT8zP2bLqW1aKhX0MfYdR4vN6UwBZR5gCmYKsJQ8X5pG")
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(days=1)
jwt = JWTManager(app)


@app.route("/auth/register", methods=["POST"])
def register():
    data = request.json
    username = data.get("username")
    password = data.get("password")

    if not username or not password:
        return jsonify({"error": "Username and password are required"}), 400

    if users_collection.find_one({"username": username}):
        return jsonify({"error": "Username already exists"}), 400

    hashed_password = bcrypt.generate_password_hash(password).decode("utf-8")
    users_collection.insert_one({"username": username, "password": hashed_password})

    send_notification({"username": username, "message": "Your account was created successfully", "subject": "Welcome to our platform!"})

    return jsonify({"message": "User registered successfully"}), 201


@app.route("/auth/login", methods=["POST"])
def login():
    data = request.json
    username = data.get("username")
    password = data.get("password")

    user = users_collection.find_one({"username": username})
    if not user or not bcrypt.check_password_hash(user["password"], password):
        return jsonify({"error": "Invalid credentials"}), 401

    access_token = create_access_token(identity=username)
    return jsonify({"token": access_token}), 200


@app.route("/auth/update", methods=["PUT"])
@jwt_required()
def update_password():
    data = request.json
    new_password = data.get("new_password")

    if not new_password:
        return jsonify({"error": "New password is required"}), 400

    username = get_jwt_identity()
    hashed_password = bcrypt.generate_password_hash(new_password).decode("utf-8")

    users_collection.update_one({"username": username}, {"$set": {"password": hashed_password}})

    return jsonify({"message": "Password updated successfully"}), 200


def send_notification(data):
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()
    channel.queue_declare(queue='soa_queue')
    message = json.dumps(data)
    channel.basic_publish(exchange='', routing_key='soa_queue', body=message)
    connection.close()

if __name__ == "__main__":
    app.run(port=5001, debug=True)
