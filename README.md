# **NGINX Load Balancer Setup Guide**

This guide will walk you through installing, configuring, and testing an **NGINX Load Balancer** to distribute traffic across multiple backend servers.

## **Step 1: Install NGINX**

https://nginx.org/en/download.html

## Step 2: Configure NGINX as a Load Balancer

Go to nginx-version\conf and adit the file nginx.config

### **Basic Load Balancer Configuration**

```
worker_processes auto;

events {
    worker_connections 1024;
}

http {
    upstream backend_servers {
        server 192.168.1.101:5000;
        server 192.168.1.102:5000;
        server 192.168.1.103:5000;
    }

    server {
        listen 80;

        location / {
            proxy_pass http://backend_servers;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
    }
}

```

## **Step 3: Load-balancing Algorithms**

NGINX supports different **load-balancing methods**:

### **1. Round Robin (Default)**

```

upstream backend_servers {
    server 192.168.1.101:5000;
    server 192.168.1.102:5000;
}

```

**Requests are distributed evenly** among the servers.

### **2. Least Connections**

```

upstream backend_servers {
    least_conn;
    server 192.168.1.101:5000;
    server 192.168.1.102:5000;
}

```

**Requests go to the server with the fewest active connections.**

### **3. IP Hash (Sticky Sessions)**

```

upstream backend_servers {
    ip_hash;
    server 192.168.1.101:5000;
    server 192.168.1.102:5000;
}

```

**Requests from the same client IP go to the same server.**

---

## **Step 4: Apply and Restart NGINX**

It is important to close it and open it again for the changes to take effect

### **Check Configuration for Errors**

```powershell
PS D:\Programs\nginx-1.27.4> .\nginx -t
nginx: the configuration file D:\Programs\nginx-1.27.4/conf/nginx.conf syntax is ok
nginx: configuration file D:\Programs\nginx-1.27.4/conf/nginx.conf test is successful
PS D:\Programs\nginx-1.27.4>

```

### **Restart NGINX**

```bash
start nginx

```

## **Step 5: Enable Health Checks**

To remove failed servers automatically, add:

```
upstream backend_servers {
    server 192.168.1.101:5000 max_fails=3 fail_timeout=30s;
    server 192.168.1.102:5000 max_fails=3 fail_timeout=30s;
}

```

This prevents NGINX from forwarding traffic to unhealthy servers.

---

## **Step 6: Accessing from Python**

If you want to send requests to the load balancer from Python

```python
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

```

You now have a fully functioning **load-balanced system** with multiple services managed by **NGINX**.
