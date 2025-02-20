# import json
# import requests
# import pika
# import logging
# from config import Properties
#
# logging.basicConfig(level=logging.INFO)
#
# def send_email(subject, body, to_email):
#     url = "https://api.mailersend.com/v1/email"
#     api_token = "mlsn.c30e9caa8664f481a32af1a61eb0f5c5a20f2f3b1e713026c2945edbac995846"
#
#     headers = {
#         "Content-Type": "application/json",
#         "X-Requested-With": "XMLHttpRequest",
#         "Authorization": f"Bearer {api_token}",
#     }
#
#     data = {
#         "from": {"email": "MS_0b9rX7@trial-yzkq34067ox4d796.mlsender.net"},
#         "to": [{"email": to_email}],
#         "subject": subject,
#         "text": body,
#         "html": body
#     }
#
#     if Properties.SEND_EMAILS:
#         try:
#             response = requests.post(url, json=data, headers=headers)
#             response.raise_for_status()
#             logging.info(f"Email sent: {subject} - {to_email}")
#         except requests.exceptions.RequestException as e:
#             logging.error(f"Failed to send email: {e}")
#     else:
#         logging.info(f"Email not sent: {subject} - {body} - {to_email}")
#
# def callback(ch, method, properties, body):
#     try:
#         data = json.loads(body)
#         message = data.get("message")
#         username = data.get("username")
#         subject = data.get("subject")
#
#         if message and username and subject:
#             send_email(f"{subject}", f"{message}", f"{username}")
#         else:
#             logging.warning(f"Invalid message received: {data}")
#     except json.JSONDecodeError as e:
#         logging.error(f"Failed to decode message: {e}")
#
# if __name__ == "__main__":
#     try:
#         connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
#         channel = connection.channel()
#         channel.queue_declare(queue='soa_queue')
#         channel.basic_consume(queue='soa_queue', on_message_callback=callback, auto_ack=True)
#         logging.info("Starting to consume messages")
#         channel.start_consuming()
#     except pika.exceptions.AMQPConnectionError as e:
#         logging.error(f"Failed to connect to RabbitMQ: {e}")
#     except OSError as e:
#         logging.error(f"Socket error: {e}")
#     finally:
#         if 'connection' in locals() and connection.is_open:
#             connection.close()
#             logging.info("Connection closed")