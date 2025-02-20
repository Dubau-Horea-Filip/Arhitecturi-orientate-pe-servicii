import asyncio
import websockets
import pika

connected_clients = set()

async def notify_clients(message):
    """Send a message to all connected WebSocket clients."""
    if connected_clients:
        await asyncio.gather(*(client.send(message) for client in connected_clients))

async def handler(websocket, path=None):  # Default path to None
    connected_clients.add(websocket)
    try:
        async for _ in websocket:
            pass  # Keep connection open
    finally:
        connected_clients.remove(websocket)


def rabbitmq_listener(loop):
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()
    channel.queue_declare(queue='shelter_queue')

    def callback(ch, method, properties, body):
        message = body.decode()
        print(f"Received message from RabbitMQ: {message}")
        asyncio.run_coroutine_threadsafe(notify_clients(message), loop)  # Ensure correct event loop

    channel.basic_consume(queue='shelter_queue', on_message_callback=callback, auto_ack=True)
    channel.start_consuming()

async def main():
    loop = asyncio.get_running_loop()

    asyncio.get_running_loop().run_in_executor(None, rabbitmq_listener, loop)

    async with websockets.serve(handler, "localhost", 6789):
        print("WebSocket server started on ws://localhost:6789")
        await asyncio.Future()

asyncio.run(main())
