import subprocess
import time


def start_flask_auth_app():
    command = ['python', 'auth/AuthService.py']
    process = subprocess.Popen(command)
    print("Flask auth app started")
    return process


def start_flask_animal_app_on_port(port):
    command = ['python', 'services/Animal.py', str(port)]
    process = subprocess.Popen(command)
    print(f"Flask animal app started on port {port}")
    return process


def start_flask_book_app_on_port(port):
    command = ['python', 'services/Book.py', str(port)]
    process = subprocess.Popen(command)
    print(f"Flask book app started on port {port}")
    return process


def start_notification_app():
    command = ['python', 'services/Notification.py']
    process = subprocess.Popen(command)
    print("Notification app started")
    return process


def start_multiple_apps():
    animal_ports = [1807, 1808]
    book_ports = [1809, 1810]

    processes = []

    # Start authentication service
    processes.append(start_flask_auth_app())
    time.sleep(1)

    # Start notification service
    processes.append(start_notification_app())
    time.sleep(1)

    # Start animal services
    for port in animal_ports:
        processes.append(start_flask_animal_app_on_port(port))
        time.sleep(1)

    # Start book services
    for port in book_ports:
        processes.append(start_flask_book_app_on_port(port))
        time.sleep(1)

    try:
        while True:
            time.sleep(60)
    except KeyboardInterrupt:
        for process in processes:
            process.terminate()
        print("All Flask apps stopped.")


if __name__ == "__main__":
    start_multiple_apps()
