from threading import Thread
from threading import Timer
import time
import logging

from foos.ui.ui import registerMenu

logger = logging.getLogger(__name__)

from threading import Lock
import socket
import json

class Plugin():

    # Plugin Interface
    def __init__(self, bus):
        self.bus = bus
        self.bus.subscribe(self.process_event, thread=True)

        self.timeout = 10.0 #TODO 75.0
        self.backend_host = 'localhost'
        self.backend_port = 8642
        self.reset_exponential_backoff()
        self.mutex = Lock()
        self.users_near = {}

        Thread(target=self.__run, daemon=True).start()

    def process_event(self, ev):
        if ev.name == "users_reset":
            self.reset()

    # Class methods
    def reset_exponential_backoff(self):
         self.exponential_backoff = 0.1
         self.max_exponential_backoff = 1 #TODO

    def reset(self):
        for user, _value in self.users_near.items():
            self.userLeft(user)

    def userNear(self, user, beacons_power):
        toRaise = None
        self.mutex.acquire()
        try:
            if user in self.users_near:
                self.users_near.pop(user, None).cancel()
            
            storedUserTimer = Timer(self.timeout, self.userLeft, [user])
            self.users_near[user] = storedUserTimer
            storedUserTimer.start()
            self.bus.notify('user_near', (user, beacons_power))
        except Exception as inst:
            toRaise = inst
        self.mutex.release()
        if toRaise:
            raise toRaise

    def userLeft(self, user):
        toRaise = None
        self.mutex.acquire()
        try:
            if user in self.users_near:
                self.users_near.pop(user, None).cancel()
                self.bus.notify('user_left', user)
        except Exception as inst:
            toRaise = inst
        self.mutex.release()
        if toRaise:
            raise toRaise

    def __run(self):
        while True:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            logger.info('USERS NEAR CLIENT: Connecting to ' + self.backend_host + ':' + str(self.backend_port))

            try:
                self.sock.connect((self.backend_host, self.backend_port))
                self.reset_exponential_backoff()
                logger.info('USERS NEAR CLIENT: Connected to ' + self.backend_host + ':' + str(self.backend_port))
                while True:
                    data = self.sock.recv(1024)
                    if not data:
                        break
                    data = data.decode().strip()
                    logger.debug('USERS NEAR CLIENT: Recv: '+str(data))
                    self.handle(json.loads(data))
            except Exception as inst :
                logger.info('USERS NEAR CLIENT: Cannot connect to server ('+str(inst)+'), waiting '+str(self.exponential_backoff))
                time.sleep(self.exponential_backoff)
                self.exponential_backoff *= 2
                if self.exponential_backoff > self.max_exponential_backoff:
                    self.exponential_backoff = self.max_exponential_backoff
            self.sock.close()

    def handle(self, data):
        user = data['user']
        beacons_power = data['beacons']
        self.userNear(user, beacons_power)

