#!/usr/bin/env python
 
from http.server import BaseHTTPRequestHandler, HTTPServer
from threading import Lock, Thread
import socket
import select
import urllib

class RelayRequestHandler(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type','text/html')
        self.end_headers()

    def do_GET(self):
        self._set_headers()
        self.wfile.write(bytes("Not supported", "utf8"))
  
    def do_POST(self):
        self._set_headers()
        length = int(self.headers['Content-Length'])
        self.server.write_in_relay_socket(self.rfile.read(length))
        self.wfile.write("OK".encode("utf-8"))

class RelayHTTPServer(HTTPServer):
    def __init__(self, server_address, hander):
        self.mutex = Lock()
        HTTPServer.__init__(self, server_address, hander)
        Thread(target=self.start_relay, daemon=True).start()

    def write_in_relay_socket(self, data):
        self.mutex.acquire()
        try:
            for relay_socket in self.client_sockets:
                relay_socket.sendall(data)
        except Exception as inst:
            logger.error(str(inst))
        self.mutex.release()

    def start_relay(self):
        serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        serversocket.bind(('', 8642))
        serversocket.listen(5)
        monitored_sockets = [serversocket]
        self.client_sockets = []

        try:
            while True:
                ready_to_read_sockets = select.select(
                    monitored_sockets,
                    tuple(),
                    tuple()
                )[0]
                for ready_socket in ready_to_read_sockets:
                    if ready_socket == serversocket:
                        client_socket, client_address = serversocket.accept()
                        monitored_sockets.append(client_socket)
                        self.client_sockets.append(client_socket)
                    else:
                        message = ready_socket.recv(1024)
                        if not message:
                            self.client_sockets.remove(ready_socket)
                            monitored_sockets.remove(ready_socket)
        except KeyboardInterrupt:
            pass

        monitored_sockets.remove(server_socket)
        for client_socket in monitored_sockets:
            client_socket.close()
        server_socket.close()

print('Starting relay...')
server_address = ('', 8421)
RelayHTTPServer(server_address, RelayRequestHandler).serve_forever()
 
