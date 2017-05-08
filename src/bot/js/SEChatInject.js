window.chat_ex = {
	cmd_start: "/!",
	cmds: {
		"test": function(args){
			chat_ex.sendMessage("Response");
		},
		"help": function(args){
			var helptext="Commands: help";
			for(var cmdf in chat_ex.cmds){
				if(cmdf!="help"){
					helptext+=", "+cmdf;
				}
			}
			chat_ex.sendMessage(helptext);
		}
	},
	sendMessage: function(msgtext, roomid){
		if(!roomid){
			roomid=CHAT.CURRENT_ROOM_ID;
		}
		$.ajax({ "type": "POST",
			"url": ("http://"+document.domain+"/chats/"+roomid+"/messages/new"),
			"data": fkey({ "text": msgtext }),
			"dataType": "json", async:true
			});
	},
	getMessages: function(roomid){
		if(!roomid){
			roomid=CHAT.CURRENT_ROOM_ID;
		}
		return $.ajax({ "type": "POST",
			"url": ("http://"+document.domain+"/chats/"+roomid+"/events/"),
			"data": fkey({ "since": mn, "mode": "Messages", "msgCount": "5" }),
			"contentType":"application/json; charset=utf-8",
			"dataType": "json",
			async:true,
			'success': function(roomid){
				var evts = JSON.parse(msgsjson).events;
				for(var i=0;i<evts.length;++i){
					this.parseMessage(evts[i]);
				}
			}).responseText;
	},
	parseMessages: 
	},
	parseMessage: function(msgjson){
		var event_type = msgjson.event_type;
		var time_stamp = msgjson.time_stamp;
		var room_id    = msgjson.room_id;
		var user_id    = msgjson.user_id;
		var user_name  = msgjson.user_name;
		var msg_id     = msgjson.message_id;
		var msg_text   = msgjson.content;
		
		if(!msg_text || !msg_text.startsWith(this.cmd_start) || this.procash.arr.indexOf(msg_id)>=0 || user_id==CHAT.CURRENT_USER_ID){
			return;
		}
		// comd = [command, args]
		var comd=msg_text.match(/\/!\S*/g)[0].substring(2).split(" ",2);
		if(!this.cmds[comd[0]]){
			this.sendMessage(":"+msg_id+" Invalid command.");
		}
		else{
			this.cmds[comd]();
		}
		this.procash.add(msg_id);
	},
	procash: {
		cashpos: 0,
		arr:[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
		add: function(theval){
			if(this.cashpos>=this.arr.length){
				this.cashpos=0;
			}
			this.arr[this.cashpos++]=theval;
		}
	}
}
if (!String.prototype.startsWith) {
	String.prototype.startsWith = function( str ) {
		return str.length > 0 && this.substring( 0, str.length ) === str;
	}
}
window.setInterval(function(){chat_ex.parseMessages(chat_ex.getMessages())}, 1000)
