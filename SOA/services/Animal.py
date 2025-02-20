import json
import sys
import pika
from flask import Flask, request, jsonify
from flask_restful import Api, Resource
from mongoengine import Document, StringField, IntField, BooleanField, DateTimeField, connect
from config import Properties
from flask_jwt_extended import JWTManager, jwt_required, get_jwt_identity
from datetime import datetime
from flask_cors import CORS
# Connect to MongoDB (shelterDatabase) running locally
connect(db="shelterDatabase", host="localhost", port=27017)


# Flask application setup
app = Flask(__name__)
CORS(app)
api = Api(app)
app.config["JWT_SECRET_KEY"] = Properties.JWT_SECRET
jwt = JWTManager(app)


# Function to send a notification using RabbitMQ
def send_notification(data):
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()
    channel.queue_declare(queue='shelter_queue')
    message = json.dumps(data)
    channel.basic_publish(exchange='', routing_key='shelter_queue', body=message)
    connection.close()



class Animal(Document):
    name = StringField(required=True)
    species = StringField(required=True)
    breed = StringField(required=True)
    age = IntField(required=True)
    health_status = StringField(required=True)
    adoption_status = BooleanField(default=False)
    arrival_date = DateTimeField(default=datetime.utcnow)


class AnimalResource(Resource):
    @jwt_required()
    def get(self, animal_id=None):
        if animal_id:
            animal = Animal.objects(id=animal_id).first()
            return jsonify(animal.to_json() if animal else {"error": "Animal not found"})
        else:
            animals = Animal.objects()
            return jsonify([{"id": str(a.id), **json.loads(a.to_json())} for a in animals])

    @jwt_required()
    def post(self):
        data = request.json
        if 'arrival_date' in data:
            data['arrival_date'] = datetime.fromisoformat(data['arrival_date'])
        animal = Animal(**data).save()
        return jsonify({"message": "Animal added", "id": str(animal.id)})

    @jwt_required()
    def put(self, animal_id):
        data = request.json
        animal = Animal.objects(id=animal_id).first()
        if not animal:
            return jsonify({"error": "Animal not found"})

        # Update animal fields
        animal.update(**data)

        send_notification({
            "message": f"Animal {animal_id} was adopted",
            "subject": "Animal adopted"
        })

        # Send a notification after the animal is updated
        # send_notification({
        #     "message": f"Animal {animal_id} was updated",
        #     "subject": "Animal updated"
        # })

        return jsonify({"message": "Animal updated"})

    @jwt_required()
    def delete(self, animal_id):
        animal = Animal.objects(id=animal_id).first()
        if not animal:
            return jsonify({"error": "Animal not found"})
        animal.delete()
        return jsonify({"message": "Animal removed from shelter"})


# Add API routes
api.add_resource(AnimalResource, "/animals/", "/animals/<animal_id>")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        app.run(debug=True, host="0.0.0.0", port=int(sys.argv[1]))
    else:
        print("No port provided.")
