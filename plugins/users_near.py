from threading import Thread
import time
import logging

from foos.ui.ui import registerMenu

logger = logging.getLogger(__name__)

class Plugin():
    def __init__(self, bus):
        self.bus = bus
        self.bus.subscribe(self.process_event, thread=True)

        self.game_ongoing = False
        self.users_near = {}
        self.users_playing = {}

        self.positions = { #TODO
                'a' : { 'limit' : 100, 'beacon' : "E9:A2:96:32:7D:A1"},
                'b' : { 'limit' : 100, 'beacon' : "DA:DD:A6:DE:22:8F"},
                'c' : { 'limit' : 100, 'beacon' : "DF:5A:57:36:FB:5B"},
                'd' : { 'limit' : 100, 'beacon' : "D2:73:A4:46:55:29"}
        }

        self.positions = { #TODO
                'a' : { 'limit' : 100, 'beacon' : "a"},
                'b' : { 'limit' : 100, 'beacon' : "b"},
                'c' : { 'limit' : 100, 'beacon' : "c"},
                'd' : { 'limit' : 100, 'beacon' : "d"}
        }


        for position in self.positions:
            self.users_playing[position] = None

    def process_event(self, ev):
        if ev.name == 'people_stop_playing':
            self.game_ongoing = False
            self.update_users_playing()

        if ev.name == 'people_start_playing':
            self.game_ongoing = False

        if ev.name == 'user_left':
            self.users_near.pop(ev.data, None)
            if not self.game_ongoing:
                self.update_users_playing()

        if ev.name == 'user_near':
            self.users_near[ev.data[0]] = ev.data[1]
            if not self.game_ongoing:
                self.update_users_playing()

    def update_users_playing(self):
        places = {}
        for user, beacons in self.users_near.items():
            nearest_beacon = None
            dist_to_nearest = None

            for beacon in beacons:
                b = beacon['beacon']
                dist = beacon['distance']
                if not dist_to_nearest or dist_to_nearest > dist:
                    nearest_beacon = b
                    dist_to_nearest = dist
            
            if nearest_beacon and (not nearest_beacon in places or dist_to_nearest < places[nearest_beacon][1]):
                places[nearest_beacon] = (user, dist_to_nearest)

        notify = False
        for position, position_info in self.positions.items():
            player = None
            beacon = position_info['beacon']
            if beacon and beacon in places and places[beacon][1] < position_info['limit']:
                player = places[beacon][0]

            notify = notify or self.users_playing[position] != player
            self.users_playing[position] = player

        if notify:
            logger.info(self.users_playing)
            yellowPlayers = []
            if self.users_playing['a']:
                yellowPlayers.append(self.users_playing['a'])

            if self.users_playing['b']:
                yellowPlayers.append(self.users_playing['b'])

            blackPlayers = []
            if self.users_playing['c']:
                blackPlayers.append(self.users_playing['c'])

            if self.users_playing['d']:
                blackPlayers.append(self.users_playing['d'])

            self.bus.notify('set_players',{"yellow": yellowPlayers, "black": blackPlayers, "auto": True}) #TODO
        #self.bus.notify('set_players',{"yellow": ["imartinez", "jmperez"], "black": [], "auto": True}) #TODO

