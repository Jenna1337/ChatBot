package chat.users;

import java.util.List;
import chat.ChatSite;
import utils.json.JsonList;

public class ChatUserList extends JsonList<ChatUser>
{
	public ChatUserList()
	{
		super();
	}
	public ChatUserList(List<String> userlists, ChatSite chatsite)
	{
		super();
		for(String rawuserarrayjson : userlists)
		{
			String[] userinfos = rawuserarrayjson.substring(1, 
					rawuserarrayjson.length()-1).split("\\},\\{(?=\"id\")");
			for(int i=0; i<userinfos.length; ++i)
				this.add(new ChatUser(userinfos[i], chatsite));
		}
		this.sort(null);
	}
	public ChatUserList(String rawuserarrayjson, ChatSite chatsite)
	{
		super();
		String[] userinfos = rawuserarrayjson.split("\\},\\{(?=\"id\")");
		for(int i=0; i<userinfos.length; ++i)
			this.add(new ChatUser(userinfos[i], chatsite));
		this.sort(null);
	}
	
	public ChatUserList getModerators(){
		ChatUserList filteredlist = new ChatUserList();
		for(ChatUser user : this)
			if(user.isModerator())
				filteredlist.add(user);
		filteredlist.sort(null);
		return filteredlist;
	}
	public ChatUserList getOwners(){
		ChatUserList filteredlist = new ChatUserList();
		for(ChatUser user : this)
			if(user.isOwner())
				filteredlist.add(user);
		filteredlist.sort(null);
		return filteredlist;
	}
}
