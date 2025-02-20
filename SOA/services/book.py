import json
import sys
import pika
from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_restful import Api, Resource
from mongoengine import Document, StringField, FloatField, IntField, connect
from config import Properties
from flask_jwt_extended import JWTManager, jwt_required, get_jwt_identity

# Connect to MongoDB (libraryDatabase) running locally
connect(db="libraryDatabase", host="localhost", port=27017)



# Function to send an asynchronous notification using RabbitMQ
def send_notification(data):
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()
    channel.queue_declare(queue='soa_queue')
    message = json.dumps(data)
    channel.basic_publish(exchange='', routing_key='soa_queue', body=message)
    connection.close()


# MongoDB model for the 'Book' document
class Book(Document):
    title = StringField(required=True)
    author = StringField(required=True)
    genre = StringField(required=True)
    price = FloatField(required=True)
    quantity_available = IntField(default=0)


# Flask application setup
app = Flask(__name__)
api = Api(app)
CORS(app)  # This enables CORS for all routes and origins
app.config["JWT_SECRET_KEY"] = Properties.JWT_SECRET
jwt = JWTManager(app)


# Resource for managing book data (GET, POST, PUT, DELETE methods)
class BookResource(Resource):
    # GET method to retrieve book details (fetch a specific book if ID is provided)
    @jwt_required()
    def get(self, book_id=None):
        if book_id:
            book = Book.objects(id=book_id).first()
            return jsonify(book.to_json() if book else {"error": "Book not found"})
        else:
            books = Book.objects()
            return jsonify([b.to_json() for b in books])

    # POST method to add a new book entry
    @jwt_required()
    def post(self):
        data = request.json
        if 'book_id' in data:  # Book purchase logic (buy)
            book_id = data.get('book_id')
            book = Book.objects(id=book_id).first()
            if not book:
                return jsonify({"error": "Book not found"}), 404
            if book.quantity_available > 0:
                book.update(dec__quantity_available=1)
                # send_notification({"message": f"Book {book.title} purchased"})
                return jsonify({"message": "Book purchased", "quantity_available": book.quantity_available - 1})
            else:
                return jsonify({"error": "Book out of stock"}), 400
        else:  # Book creation logic (add new book)
            book = Book(**data).save()
            return jsonify({"message": "Book added", "id": str(book.id)})

    # PUT method to update book details
    @jwt_required()
    def put(self, book_id):
        data = request.json
        book = Book.objects(id=book_id).first()
        if not book:
            return jsonify({"error": "Book not found"})
        book.update(**data)
        updated_book = Book.objects(id=book_id).first()  # Fetch updated book
        return jsonify({"message": "Book updated", "book": updated_book.to_json()})

    # DELETE method to remove a book from the database
    @jwt_required()
    def delete(self, book_id):
        book = Book.objects(id=book_id).first()
        if not book:
            return jsonify({"error": "Book not found"})
        book.delete()
        return jsonify({"message": "Book deleted"})


# Add the book resource to the API
api.add_resource(BookResource, "/books/", "/books/<book_id>", "/books/buy/")
# Main entry point for running the Flask application
if __name__ == "__main__":
    if len(sys.argv) > 1:
        app.run(debug=True, host="0.0.0.0", port = int(sys.argv[1]))
    else:
        print("No port provided.")
