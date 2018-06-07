var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var gamesWaitingToStart = [];
var gamesRunning = [];
var Players = [];
'use strict';

var os = require('os');
var ifaces = os.networkInterfaces();

Object.keys(ifaces).forEach(function (ifname) {
  var alias = 0;

  ifaces[ifname].forEach(function (iface) {
    if ('IPv4' !== iface.family || iface.internal !== false) {
      // skip over internal (i.e. 127.0.0.1) and non-ipv4 addresses
      return;
    }

    if (alias >= 1) {
      // this single interface has multiple ipv4 addresses
      console.log(ifname + ':' + alias, iface.address);
    } else {
      // this interface has only one ipv4 adress
      console.log(ifname, iface.address);
    }
    ++alias;
  });
});

class Player {
	constructor(playerID, character, name) {
		this.playerID = playerID;
		this.x = -1;
		this.y = -1;
		this.room = "-1";
		this.character = character;
		this.name = name;
	}
}


class GameInstance {
    constructor(roomHost,roomName,maxPlayers,bet){
		this.players = [];
        this.game = {
            roomHost,
            roomName,
            maxPlayers,
			bet,
        };
    }
}
server.listen(8080,function() {
    console.log("Server is listening...");
});
io.on('connection', function(socket) {
    console.log("Player Connected !");

    socket.emit('requestPlayerInformation',{
        id:socket.id
    });
	socket.on('sendPlayerInformation',function(playerInformation) {
		var player = new Player(socket.id, playerInformation.character, playerInformation.playerName);
		Players.push(player);
		
		socket.emit('connected', {
			id:socket.id
		});
	});
	
    socket.on('disconnect',function() {
        console.log("Player Disconnected");
		for (var i = 0; i < gamesWaitingToStart.length; i++) {
			if (gamesWaitingToStart[i].game.roomHost == socket.id) {
				gamesWaitingToStart.splice(i, 1);
				socket.broadcast.emit('roomCanceled', {
					roomid:socket.id,
				});
			} else {
				for (var a = 0; a < gamesWaitingToStart[i].players.length; a++) {
					if (gamesWaitingToStart[i].players[a] == socket.id) {
						gamesWaitingToStart[i].players.splice(a, 1);
						console.log(gamesWaitingToStart[i]);
					}		
				}
			}
		}
		
		for (var i = 0; i < gamesRunning.length; i++) {
			for (var a = 0; a < gamesRunning[i].players.length; a++) {
				if (gamesRunning[i].players[a] == socket.id) {
					gamesRunning[i].players.splice(a, 1);
					socket.broadcast.emit('playerDisconnect', {
						id:socket.id
					});
				}
			}
		}
		
		
		var roomHost;
		for (var i = 0; i < Players.length; i++) {
			if (Players[i].playerID == socket.id) {
				roomHost = Players[i].room;
				Players.splice(i, 1);
			}
		}
		socket.broadcast.emit('lobbyUpdate', {
			id:roomHost,
		});
		
    });
	
	socket.on('createRoom', function(createRoom) {
		var roomName = createRoom.roomName;
		var maxPlayers = createRoom.maxPlayers;
		var roomHost = socket.id;
		var playerName = createRoom.playerName;
		var bet = createRoom.bet;
		var game = new GameInstance(roomHost, roomName, maxPlayers, bet);
		for (var i = 0; i < Players.length; i++) {
			if (Players[i].playerID == socket.id) {
				Players[i].room = socket.id;
				Players[i].name = playerName;
			}
		}

		game.players.push(socket.id);
		gamesWaitingToStart.push(game);
		console.log("Added room: " + roomName + ", " + roomHost);
	});
	
	socket.on('joinRoom', function(joinRoom) {
		for (var i = 0; i < gamesWaitingToStart.length; i++) {
			if (gamesWaitingToStart[i].game.roomHost == joinRoom.roomHost) {
				if (gamesWaitingToStart[i].players.length < gamesWaitingToStart[i].game.maxPlayers) {
					gamesWaitingToStart[i].players.push(socket.id);
					for (var a = 0; a < Players.length; a++) {
						if (Players[a].playerID == socket.id) {
							Players[a].room = joinRoom.roomHost;
							Players[a].name = joinRoom.name;
							socket.emit('join', {
								playerID:socket.id,
							});
						}
							
					}
				}
			}
		}
		
	});

    socket.on('startOfGame',function(roomHost) {
		for (var i = 0; i < gamesWaitingToStart.length; i++) {
			if (gamesWaitingToStart[i].game.roomHost == roomHost) {
				if (gamesWaitingToStart[i].game.maxPlayers == gamesWaitingToStart[i].players.length) {
				var temp = gamesWaitingToStart[i].players;
				gamesRunning.push(gamesWaitingToStart[i]);
				gamesWaitingToStart.splice(i,1)
				
				socket.emit('startGame', {
					room:roomHost
				});
				socket.broadcast.emit('startGame', {
					room:roomHost
				});
				console.log("starting game for room " + roomHost);
				} else {
					socket.emit('NotEnough', {
						id:socket.id
					});
				}
			}
			
		}
    });
	
	socket.on('updateLobby',function(roomHost) {
		for (var i = 0; i < gamesWaitingToStart.length; i++) {
			if (gamesWaitingToStart[i].game.roomHost == roomHost.roomHost) {
				var playerNames = new Array();
				
				for (var a = 0; a < gamesWaitingToStart[i].players.length; a++) {
					for (var j = 0; j < Players.length; j++) {
						if (Players[j].room == gamesWaitingToStart[i].players[a]) {
							playerNames.push(Players[j].name);
						}
					}
					
				}
				console.log("updating lobby");
				socket.emit('maxPlayers', {
					maxPlayers:gamesWaitingToStart[i].game.maxPlayers
				});
				
				socket.emit('playerNames',
					playerNames,
				);
				socket.broadcast.emit('playerNames',
					playerNames,
				);
			}
		}
	});
    
    socket.on('requestListOfAvailableGames',function() {
        socket.emit('showRooms',
            gamesWaitingToStart
        );
    });

	socket.on('updateServer', function(clientInformation) {
		for (var i = 0; i < Players.length; i++) {
				if (clientInformation.roomHost == Players[i].room) {
					Players[i].x = clientInformation.x;
					Players[i].y = clientInformation.y;
				}
		}
		
		var players = [];
		
		for (var i = 0; i < Players.length; i++) {
			if (clientInformation.roomHost == Players[i].room) {
				players.push(Players[i]);
			}
		}
		
		socket.emit('updateClient', 
			players
		);
		socket.broadcast.emit('updateClient', 
			players
		);
	});

    //returns the character image id for a certain player
    socket.on('requestPlayerInformation',function(playerRequestInformation) {
        var info = [];
		for (var i = 0; i < Players.length; i++) {
			if (Players[i].room == playerRequestInformation.roomHost) {
				info.push(Players[i]);
			}
		}
		console.log(info);
		socket.emit('getPlayers', 
			info
		);
		
    });

	socket.on('playerDied', function() {
		socket.broadcast.emit('playerDisconnect', {
			id:socket.id
		});
	});
    
});