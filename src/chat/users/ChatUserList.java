package chat.users;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import chat.ChatSite;

public class ChatUserList extends LinkedList<ChatUser>
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
					rawuserarrayjson.length()-1).split("\\},\\{(?=\"event_type\")");
			for(int i=0; i<userinfos.length; ++i)
				this.add(new ChatUser(userinfos[i], chatsite));
		}
	}
	public ChatUserList(String rawuserarrayjson, ChatSite chatsite)
	{
		super();
		String[] userinfos = rawuserarrayjson.split("\\},\\{(?=\"event_type\")");
		for(int i=0; i<userinfos.length; ++i)
			this.add(new ChatUser(userinfos[i], chatsite));
	}
	
	public void sortByTimeStamp(){
		Collections.sort(this, compTimeStamp);
	}
	
	private static final Comparator<ChatUser> compTimeStamp = new Comparator<ChatUser>()
	{
		public int compare(ChatUser o1, ChatUser o2)
		{
			long t1=o1.getId();
			long t2=o2.getId();
			return t1>t2?1:(t1<t2?-1:0);
		}
	};
	
	public ChatUserList getModerators(){
		ChatUserList filteredlist = new ChatUserList();
		for(ChatUser user : this)
			if(user.isModerator())
				filteredlist.add(user);
		return filteredlist;
	}
	public ChatUserList getOwners(){
		ChatUserList filteredlist = new ChatUserList();
		for(ChatUser user : this)
			if(user.isOwner())
				filteredlist.add(user);
		return filteredlist;
	}
}
